package com.machiav3lli.backup.items

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.PREFS_ALLOWSHADOWINGDEFAULT
import com.machiav3lli.backup.PREFS_SHADOWROOTFILE
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.utils.*
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

// TODO MAYBE migrate at some point to FuckSAF

open class StorageFile {

    var parent: StorageFile? = null
    var context: Context? = null

    private var _uri: Uri? = null
    val uri: Uri?
        get() = _uri ?: file?.let { file ->
            context?.let {
                FileProvider.getUriForFile(
                    it, "${it.applicationContext.packageName}.provider", file
                )
            }
        } ?: Uri.fromFile(file?.absoluteFile)
    private var file: RootFile? = null
    private var parentFile: RootFile? = null

    constructor(
        parent: StorageFile?,
        context: Context?,
        uri: Uri?,
        allowShadowing: Boolean = OABX.prefFlag(PREFS_ALLOWSHADOWINGDEFAULT, false) // Storage files that should be shadowable should be explicitly decalred as such
    ) {
        this.parent = parent
        this.context = context
        this._uri = uri
        if (OABX.prefFlag(PREFS_SHADOWROOTFILE, false) && allowShadowing) {
            fun isValidPath(file: RootFile?): Boolean =
                file?.let { file.exists() && file.canRead() && file.canWrite() } ?: false
            parent ?: run {
                file ?: run {
                    uri?.let { uri ->
                        val last = uri.lastPathSegment!!
                        try {
                            Timber.i("SAF: last=$last uri=$uri")
                            if (last.startsWith('/')) {
                                val checkFile = RootFile(last)
                                if (isValidPath(checkFile)) {
                                    Timber.i("found direct RootFile shadow at $checkFile")
                                    file = checkFile
                                } else
                                    throw Exception("cannot use RootFile shadow at $last")
                            } else {
                                var (storage, subpath) = last.split(":")
                                val user = ShellCommands.currentUser
                                if (storage == "primary")
                                    storage = "emulated/$user"
                                // NOTE: lockups occur in emulator (or A12?) for certain paths
                                // e.g. /storage/emulated/$user
                                val possiblePaths = listOf(
                                    "/mnt/pass_through/$user/$storage/$subpath",
                                    "/mnt/media_rw/$storage/$subpath",
                                    "/mnt/runtime/full/$storage/$subpath",
                                    "/mnt/runtime/default/$storage/$subpath",

                                    // lockups! primary links to /storage/emulated/$user and all self etc.
                                    //"/storage/$storage/$subpath",
                                    //"/storage/self/$storage/$subpath",
                                    //"/mnt/runtime/default/self/$storage/$subpath"
                                    //"/mnt/user/$user/$storage/$subpath",
                                    //"/mnt/user/$user/self/$storage/$subpath",
                                    //"/mnt/androidwritable/$user/self/$storage/$subpath",
                                )
                                var checkFile: RootFile? = null
                                for (path in possiblePaths) {
                                    checkFile = RootFile(path)
                                    if (isValidPath(checkFile)) {   //TODO hg42 check with timeout in case of lockups
                                        Timber.i("found storage RootFile shadow at $checkFile")
                                        file = checkFile
                                        break
                                    }
                                    checkFile = null
                                }
                                if (checkFile == null)
                                    throw Exception(
                                        "cannot use RootFile shadow at one of ${
                                            possiblePaths.joinToString(
                                                " "
                                            )
                                        }"
                                    )
                            }
                        } catch (e: Throwable) {
                            file = null
                            Timber.i("using access via SAF")
                        }
                    }
                }
            }
        }
    }

    constructor(file: RootFile) {
        this.file = file
    }

    constructor(file: File) {
        this.file = RootFile(file)
    }

    constructor(parent: StorageFile, file: RootFile) {
        this.parent = parent
        this.file = file
    }

    constructor(parent: StorageFile, path: String) {
        this.parent = parent
        file = RootFile(parent.file, path)
    }

    var name: String? = null
        get() {
            if (field == null) {
                field = file?.name ?: let {
                    context?.let { context -> _uri?.getName(context) }
                }
            }
            return field
        }

    val path: String?
        get() = file?.path ?: _uri?.path

    override fun toString(): String {
        return path ?: "null"
    }

    val isFile: Boolean
        get() = file?.isFile ?: context?.let { context -> _uri?.isFile(context) } ?: false

    val isDirectory: Boolean
        get() = file?.isDirectory ?: context?.let { context -> _uri?.isDirectory(context) } ?: false

    fun exists(): Boolean =
        file?.exists() ?: context?.let { context -> _uri?.exists(context) } ?: false

    fun inputStream(): InputStream? {
        return file?.inputStream() ?: _uri?.let { uri ->
            context?.contentResolver?.openInputStream(uri)
        }
    }

    fun outputStream(): OutputStream? {
        return file?.outputStream() ?: _uri?.let { uri ->
            context?.contentResolver?.openOutputStream(uri, "w")
        }
    }

    fun createDirectory(displayName: String): StorageFile {
        return file?.let {
            val newDir = RootFile(it, displayName)
            newDir.mkdirs()
            return StorageFile(this, newDir)
        } ?: run {
            return StorageFile(
                this,
                context!!,
                createFile(context!!, _uri!!, DocumentsContract.Document.MIME_TYPE_DIR, displayName)
            )
        }
    }

    fun createFile(mimeType: String, displayName: String): StorageFile {
        return file?.let {
            if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                val newDir = RootFile(it, displayName)
                newDir.mkdirs()
                return StorageFile(this, newDir)
            } else {
                val newFile = RootFile(it, displayName)
                newFile.createNewFile()
                return StorageFile(this, newFile)
            }
        } ?: run {
            StorageFile(this, context, createFile(context!!, _uri!!, mimeType, displayName))
        }
    }

    fun delete(): Boolean {
        return try {
            file?.let {
                it.deleteRecursive()
            } ?: DocumentsContract.deleteDocument(context!!.contentResolver, _uri!!)
        } catch (e: FileNotFoundException) {
            false
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
            false
        }
    }

    fun renameTo(displayName: String): Boolean {
        var ok = false
        file?.let { oldFile ->
            val newFile = RootFile(oldFile.parent, displayName)
            ok = oldFile.renameTo(newFile)
            file = newFile
        } ?: try {
            val result =
                context?.let { context ->
                    _uri?.let { uri ->
                        DocumentsContract.renameDocument(
                            context.contentResolver, uri, displayName
                        )
                    }
                }
            if (result != null) {
                _uri = result
                ok = true
            }
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
            ok = false
        }
        return ok
    }

    fun findUri(displayName: String): Uri? {
        try {
            for (file in listFiles()) {
                if (displayName == file.name) {
                    return file._uri
                }
            }
        } catch (e: FileNotFoundException) {
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
        }
        return null
    }

    fun findFile(displayName: String): StorageFile? {
        try {
            file?.let {
                var found = StorageFile(this, displayName)
                return if (found.exists()) found else null
            }
            for (file in listFiles()) {
                if (displayName == file.name) {
                    return file
                }
            }
        } catch (e: FileNotFoundException) {
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
        }
        return null
    }

    fun recursiveCopyFiles(files: List<ShellHandler.FileInfo>) {
        suRecursiveCopyFilesToDocument(context!!, files, _uri!!)
    }

    @Throws(FileNotFoundException::class)
    fun listFiles(): List<StorageFile> {
        try {
            exists()
        } catch (e: Throwable) {
            throw FileNotFoundException("File $_uri does not exist")
        }
        checkCache()
        val id = path
        id ?: return listOf()

        fileListCache[id] ?: run {
            file?.let { dir ->
                fileListCache[id] = dir.listFiles()?.map { child ->
                    StorageFile(this, child)
                }?.toList()
            } ?: run {
                context?.contentResolver?.let { resolver ->
                    val childrenUri = try {
                        DocumentsContract.buildChildDocumentsUriUsingTree(
                            this._uri,
                            DocumentsContract.getDocumentId(this._uri)
                        )
                    } catch (e: IllegalArgumentException) {
                        return listOf()
                    }
                    val results = ArrayList<Uri>()
                    var cursor: Cursor? = null
                    try {
                        cursor = resolver.query(
                            childrenUri, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID),
                            null, null, null
                        )
                        var documentUri: Uri
                        while (cursor?.moveToNext() == true) {
                            documentUri =
                                DocumentsContract.buildDocumentUriUsingTree(
                                    this._uri,
                                    cursor.getString(0)
                                )
                            results.add(documentUri)
                        }
                    } catch (e: Throwable) {
                        LogsHandler.unhandledException(e, _uri)
                    } finally {
                        closeQuietly(cursor)
                    }
                    fileListCache[id] = results.map { uri ->
                        StorageFile(this, context, uri)
                    }
                }
            }
        }
        return fileListCache[id] ?: listOf()
    }

    fun ensureDirectory(dirName: String): StorageFile {
        return findFile(dirName)
            ?: createDirectory(dirName)
    }

    fun deleteRecursive(): Boolean = when {
        isFile ->
            delete()
        isDirectory -> try {
            val contents = listFiles()
            var result = true
            contents.forEach { file ->
                result = result && file.deleteRecursive()
            }
            if (result)
                delete()
            else
                result
        } catch (e: FileNotFoundException) {
            false
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, _uri)
            false
        }
        else -> false
    }

    companion object {
        val fileListCache: MutableMap<String, List<StorageFile>?> = mutableMapOf()
        val uriStorageFileCache: MutableMap<String, StorageFile> = mutableMapOf()
        var cacheDirty = true

        fun fromUri(context: Context, uri: Uri): StorageFile {
            // Todo: Figure out what's wrong with the Uris coming from the intent and why they need to be processed
            //  with DocumentsContract.buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value)) first
            checkCache()
            val id = uri.toString()
            return uriStorageFileCache[id] ?: StorageFile(
                null,
                context,
                uri
            ).also { uriStorageFileCache[id] = it }
        }

        fun createFile(context: Context, uri: Uri, mimeType: String, displayName: String): Uri? {
            return try {
                DocumentsContract.createDocument(
                    context.contentResolver,
                    uri,
                    mimeType,
                    displayName
                )
            } catch (e: FileNotFoundException) {
                null
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, uri)
                null
            }
        }

        fun invalidateCache() {
            cacheDirty = true
        }

        fun checkCache() {
            if (cacheDirty) {
                fileListCache.clear()
                uriStorageFileCache.clear()
                cacheDirty = false
            }
        }

        private fun closeQuietly(closeable: AutoCloseable?) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (rethrown: RuntimeException) {
                    // noinspection ProhibitedExceptionThrown
                    throw rethrown
                } catch (e: Throwable) {
                    LogsHandler.unhandledException(e)
                }
            }
        }
    }
}
