package com.machiav3lli.backup.items

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import androidx.core.content.FileProvider
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.machiav3lli.backup.OABX
import com.machiav3lli.backup.preferences.pref_cacheFileLists
import com.machiav3lli.backup.preferences.pref_cacheUris
import com.machiav3lli.backup.preferences.pref_useColumnNameSAF
import com.machiav3lli.backup.preferences.pref_invalidateSelective
import com.machiav3lli.backup.handler.LogsHandler.Companion.logException
import com.machiav3lli.backup.handler.LogsHandler.Companion.unhandledException
import com.machiav3lli.backup.handler.ShellCommands
import com.machiav3lli.backup.handler.ShellHandler
import com.machiav3lli.backup.preferences.pref_allowShadowingDefault
import com.machiav3lli.backup.preferences.pref_shadowRootFile
import com.machiav3lli.backup.utils.suRecursiveCopyFilesToDocument
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set


fun getCursorString(cursor: Cursor, columnName: String): String? {
    val index = cursor.getColumnIndex(columnName)
    return cursor.getStringOrNull(index)
}

fun getCursorLong(cursor: Cursor, columnName: String): Long? {
    val index = cursor.getColumnIndex(columnName)
    return cursor.getLongOrNull(index)
}

fun getCursorInt(cursor: Cursor, columnName: String): Int? {
    val index = cursor.getColumnIndex(columnName)
    return cursor.getIntOrNull(index)
}

/*

private fun Uri.getRawType(context: Context): String? = try {
    context.contentResolver.query(this, null, null, null, null)?.let { cursor ->
        cursor.run {
            if (moveToFirst())
                //context.contentResolver.getType(this@getRawType)
                getCursorString(this, DocumentsContract.Document.COLUMN_MIME_TYPE)
            else
                null
        }.also { cursor.close() }
    }
} catch (e: Throwable) {
    unhandledException(e, this)
    null
}

fun Uri.canRead(context: Context): Boolean = when {
    context.checkCallingOrSelfUriPermission(
        this,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    ) // Ignore if grant doesn't allow read
            != PackageManager.PERMISSION_GRANTED -> false
    else                                         -> !TextUtils.isEmpty(getRawType(context))
} // Ignore documents without MIME

fun Uri.canWrite(context: Context): Boolean {
    // Ignore if grant doesn't allow write
    if (context.checkCallingOrSelfUriPermission(this, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }
    val type = getRawType(context)
    val flags = queryForLong(context, DocumentsContract.Document.COLUMN_FLAGS)?.toInt() ?: 0
    // Ignore documents without MIME
    if (TextUtils.isEmpty(type)) {
        return false
    }
    // Deletable documents considered writable
    if (flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0) {
        return true
    }
    //return if (DocumentsContract.Document.MIME_TYPE_DIR == type && flags and DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE != 0) {
    return if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type) && flags and DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE != 0) {
        // Directories that allow create considered writable
        true
    } else !TextUtils.isEmpty(type)
            && flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0
    // Writable normal files considered writable
}

*/

fun Uri.exists(context: Context): Boolean {
    val resolver = context.contentResolver
    var cursor: Cursor? = null
    return try {
        cursor = resolver.query(
            this, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID
            ), null, null, null
        )
        (cursor?.count ?: 0) > 0
    } catch (e: IllegalArgumentException) {
        false
    } catch (e: Throwable) {
        unhandledException(e, this)
        false
    } finally {
        closeQuietly(cursor)
    }
}

private fun Uri.queryForLong(context: Context, column: String): Long? {
    val resolver = context.contentResolver
    var cursor: Cursor? = null
    try {
        cursor = resolver.query(this, null, null, null, null)
        if (cursor!!.moveToFirst()) {
            return getCursorLong(cursor, column)
        }
    } catch (e: Throwable) {
        unhandledException(e, "$this column: $column")
    } finally {
        closeQuietly(cursor)
    }
    return 0
}

private fun closeQuietly(closeable: AutoCloseable?) {
    if (closeable != null) {
        try {
            closeable.close()
        } catch (rethrown: RuntimeException) {
            throw rethrown
        } catch (e: Throwable) {
            unhandledException(e)
        }
    }
}


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

    data class DocumentInfo(
        val id: String,
        val name: String,
        val mimeType: String,
        val size: Long,
        val lastModified: Long
    )

    private var documentInfo: DocumentInfo? = null
        get() {
            if (field == null)
                field = retrieveDocumentInfo()
            return field
        }
        private set(value) {
            field = value
        }

    private fun retrieveDocumentInfo(cursor: Cursor): DocumentInfo {
        val id = getCursorString(
            cursor,
            DocumentsContract.Document.COLUMN_DOCUMENT_ID
        ) ?: "???"
        val name = getCursorString(
            cursor,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME
        ) ?: "???"
        val mimeType = getCursorString(
            cursor,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        ) ?: ""
        var size = getCursorLong(
            cursor,
            DocumentsContract.Document.COLUMN_SIZE
        ) ?: 0
        if (size < 0)
            size = 0
        //Timber.d("size: $size file: $id")
        var lastModified = getCursorLong(
            cursor,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        ) ?: 0
        if (lastModified < 0)
            lastModified = 0
        //flags = getCursorInt(cursor, Document.COLUMN_FLAGS),
        //summary = getCursorString(cursor, Document.COLUMN_SUMMARY),
        //icon = getCursorInt(cursor, Document.COLUMN_ICON),
        return DocumentInfo(
            id = id,
            name = name,
            mimeType = mimeType,
            size = size,
            lastModified = lastModified
        )
    }

    private fun retrieveDocumentInfo(): DocumentInfo? {
        context?.let { context ->
            val resolver = context.contentResolver
            var cursor: Cursor? = null
            try {
                resolver.query(
                    _uri!!,
                    null /*documentColumns*/, null, null, null
                )?.let { cursor ->
                    if (cursor.moveToFirst()) {
                        return retrieveDocumentInfo(cursor)
                    }
                }
            } catch (e: Throwable) {
                unhandledException(e, "$this")
            } finally {
                closeQuietly(cursor)
            }
        }
        return null
    }

    constructor(
        parent: StorageFile?,
        context: Context?,
        uri: Uri?,
        name: String? = null,
        allowShadowing: Boolean = pref_allowShadowingDefault.value // Storage files that should be shadowable should be explicitly declared as such
    ) {
        this.parent = parent
        this.context = context
        this._uri = uri
        name?.let { this.name = it }
        if (pref_shadowRootFile.value && allowShadowing) {
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
                                    "/mnt/media_rw/$storage/$subpath",
                                    "/mnt/pass_through/$user/$storage/$subpath",
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
                    documentInfo?.name
                }
            }
            return field
        }
        private set(name) {
            field = name
        }

    val path: String?
        get() = file?.path ?: _uri?.path

    override fun toString(): String {
        return path ?: "null"
    }

    val isFile: Boolean
        get() = !isDirectory

    val isDirectory: Boolean
        get() = file?.isDirectory
            ?: (documentInfo?.mimeType == DocumentsContract.Document.MIME_TYPE_DIR)

    val isPropertyFile: Boolean
        get() = name?.endsWith(".properties") ?: false

    fun exists(): Boolean =
        file?.exists() ?: !documentInfo?.mimeType.isNullOrEmpty()

    val size: Long
        get() = (
                if (file != null)
                    (file?.length() ?: 0L)
                else
                    (documentInfo?.size ?: 0)
                )

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
        val newFile =
            file?.let {
                val newDir = RootFile(it, displayName)
                newDir.mkdirs()
                StorageFile(this, newDir)
            } ?: run {
                StorageFile(
                    this, context!!,
                    createFile(
                        context!!, _uri!!,
                        DocumentsContract.Document.MIME_TYPE_DIR, displayName
                    )
                )
            }
        path?.let { cacheFilesAdd(it, newFile) }
        return newFile
    }

    fun createFile(mimeType: String, displayName: String): StorageFile {
        val newFile =
            file?.let {
                if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                    val newDir = RootFile(it, displayName)
                    newDir.mkdirs()
                    StorageFile(this, newDir)
                } else {
                    val newFile = RootFile(it, displayName)
                    newFile.createNewFile()
                    StorageFile(this, newFile)
                }
            } ?: run {
                StorageFile(
                    this, context,
                    createFile(context!!, _uri!!, mimeType, displayName)
                )
            }
        path?.let { cacheFilesAdd(it, newFile) }
        return newFile
    }

    fun delete(): Boolean {
        path?.let { cacheFilesRemove(it, this) }
        return try {
            file?.deleteRecursive()
                ?: DocumentsContract.deleteDocument(context!!.contentResolver, _uri!!)
        } catch (e: FileNotFoundException) {
            false
        } catch (e: Throwable) {
            unhandledException(e, _uri)
            false
        }
    }

    fun renameTo(displayName: String): Boolean {
        path?.let { cacheFilesRemove(it, this) }
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
            unhandledException(e, _uri)
            ok = false
        }
        path?.let { cacheFilesAdd(it, this) }
        return ok
    }

    fun readText(): String {
        return try {
            file?.readText()
                ?: run { inputStream()?.reader()?.readText() ?: "" }
        } catch (e: FileNotFoundException) {
            logException(e, _uri)
            ""
        } catch (e: Throwable) {
            unhandledException(e, _uri)
            ""
        }
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
            unhandledException(e, _uri)
        }
        return null
    }

    fun findFile(displayName: String): StorageFile? {
        try {
            file?.let {
                val found = StorageFile(this, displayName)
                return if (found.exists()) found else null
            }
            for (file in listFiles()) {
                if (displayName == file.name) {
                    return file
                }
            }
        } catch (e: FileNotFoundException) {
        } catch (e: Throwable) {
            unhandledException(e, _uri)
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

        path?.let { path ->

            val files = cacheGetFiles(path) ?: run {
                val results = mutableListOf<StorageFile>()
                file?.let { dir ->
                    dir.listFiles()?.forEach { child ->
                        results.add(StorageFile(this, child))
                    }
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
                        var cursor: Cursor? = null
                        try {
                            cursor = resolver.query(
                                // "For type TreeDocumentFile, getName() is implemented as
                                // a query to DocumentsProvider which is.... slow."
                                // so avoid getName by using COLUMN_DISPLAY_NAME
                                // (also add name to constructor and allow setting the name field)
                                childrenUri, null /*documentColumns*/,
                                null, null, null
                            )
                            var documentUri: Uri
                            while (cursor?.moveToNext() == true) {
                                try {
                                    val docInfo = retrieveDocumentInfo(cursor)
                                    documentUri =
                                        DocumentsContract.buildDocumentUriUsingTree(
                                            this._uri,
                                            docInfo.id
                                        )
                                    val file =
                                        if (pref_useColumnNameSAF.value)
                                            StorageFile(this, context, documentUri, docInfo.name)
                                        else
                                            StorageFile(this, context, documentUri)
                                    file.documentInfo = docInfo
                                    results.add(file)
                                } catch (e: Throwable) {
                                    unhandledException(e, _uri)
                                }
                            }
                        } catch (e: Throwable) {
                            unhandledException(e, _uri)
                        } finally {
                            closeQuietly(cursor)
                        }
                    }
                }
                cacheSetFiles(path, results)
                results
            }
            return files
        } ?: return listOf()
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
            unhandledException(e, _uri)
            false
        }
        else -> false
    }

    companion object {
        //TODO hg42 manage file trees instead of single files and let StorageFile and caches use them
        private var fileListCache =
            mutableMapOf<String, MutableList<StorageFile>>() //TODO hg42 access should automatically checkCache
        private var uriStorageFileCache = mutableMapOf<String, StorageFile>()
        private var invalidateFilters = mutableListOf<(String) -> Boolean>()

        val documentColumns = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED
        )

        fun fromUri(context: Context, uri: Uri): StorageFile {
            // Todo: Figure out what's wrong with the Uris coming from the intent and why they need to be processed
            //  with DocumentsContract.buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value)) first
            if (pref_cacheUris.value) {
                cacheCheck()
                val id = uri.toString()
                return cacheGetUri(id)
                    ?: StorageFile(
                        null,
                        context,
                        uri
                    ).also { cacheSetUri(id, it) }
            } else {
                return StorageFile(
                    null,
                    context,
                    uri
                )
            }
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
                unhandledException(e, uri)
                null
            }
        }

        fun invalidateCache(filter: (String) -> Boolean) {
            if (pref_invalidateSelective.value) {
                try {
                    invalidateFilters.add(filter)
                } catch (e: ArrayIndexOutOfBoundsException) {
                }
                cacheCheck() //TODO
            } else {
                invalidateCache()
            }
        }

        fun invalidateCache() {
            invalidateFilters = mutableListOf({ true })
            cacheCheck() //TODO
        }

        fun cacheInvalidate(storageFile: StorageFile) {
            storageFile.path?.let { path -> invalidateCache { it.startsWith(path) } }
        }

        fun cacheGetFiles(id: String): List<StorageFile>? {
            if (pref_cacheFileLists.value) {
                cacheCheck()
                return fileListCache[id]
            }
            return null
        }

        fun cacheSetFiles(id: String, files: List<StorageFile>) {
            fileListCache[id] = files.toMutableList()
        }

        private fun cacheGetUri(id: String): StorageFile? {
            cacheCheck()
            return uriStorageFileCache[id]
        }

        fun cacheSetUri(id: String, file: StorageFile) {
            uriStorageFileCache[id] = file
        }

        fun cacheFilesAdd(path: String, file: StorageFile) {
            fileListCache[path]?.run {
                add(file)
            } ?: run {
                fileListCache[path] = mutableListOf(file)
            }
        }

        fun cacheFilesRemove(path: String, file: StorageFile?) {
            file?.let {
                fileListCache[path]?.run {
                    remove(file)
                } ?: run {
                    fileListCache.remove(path)
                }
            } ?: fileListCache.remove(path)
        }

        private fun cacheCheck() {
            try {
                while (invalidateFilters.size > 0) {
                    invalidateFilters.removeFirst().let { isInvalid ->
                        fileListCache =
                            fileListCache.toMap().filterNot { isInvalid(it.key) }.toMutableMap()
                        uriStorageFileCache =
                            uriStorageFileCache.toMap().filterNot { isInvalid(it.key) }
                                .toMutableMap()
                    }
                }
            } catch (e: Throwable) {
                logException(e)
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
                    logException(e)
                }
            }
        }
    }
}
