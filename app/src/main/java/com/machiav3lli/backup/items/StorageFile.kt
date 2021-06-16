package com.machiav3lli.backup.items

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import com.machiav3lli.backup.handler.LogsHandler
import com.machiav3lli.backup.utils.exists
import com.machiav3lli.backup.utils.getName
import com.machiav3lli.backup.utils.isDirectory
import com.machiav3lli.backup.utils.isFile
import java.io.FileNotFoundException
import java.util.*

// TODO MAYBE migrate at some point to FuckSAF
open class StorageFile protected constructor(
    val parentFile: StorageFile?,
    private val context: Context,
    var uri: Uri
) {
    var name: String? = null
        get() {
            if (field == null) field = uri.getName(context)
            return field
        }
        private set

    val isFile: Boolean
        get() = uri.isFile(context)

    val isPropertyFile: Boolean
        get() = uri.isFile(context)

    val isDirectory: Boolean
        get() = uri.isDirectory(context)

    fun createDirectory(displayName: String): StorageFile? {
        val result = createFile(context, uri, DocumentsContract.Document.MIME_TYPE_DIR, displayName)
        return if (result != null) StorageFile(this, context, result) else null
    }

    fun createFile(mimeType: String, displayName: String): StorageFile? {
        val result = createFile(context, uri, mimeType, displayName)
        return if (result != null) StorageFile(this, context, result) else null
    }

    fun delete(): Boolean {
        return try {
            DocumentsContract.deleteDocument(context.contentResolver, uri)
        } catch (e: FileNotFoundException) {
            false
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, uri)
            false
        }
    }

    fun findFile(displayName: String): StorageFile? {
        try {
            for (doc in listFiles()) {
                if (displayName == doc.name) {
                    return doc
                }
            }
        } catch (e: FileNotFoundException) {
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, uri)
        }
        return null
    }

    @Throws(FileNotFoundException::class)
    fun listFiles(): Array<StorageFile> {
        try {
            exists()
        } catch (e: Throwable) {
            throw FileNotFoundException("File $uri does not exist")
        }
        val uriString = this.uri.toString()
        if (cacheDirty) {
            cacheDirty = false
            cache.clear()
        }
        if (cache[uriString].isNullOrEmpty()) {
            val resolver = context.contentResolver
            val childrenUri = try {
                DocumentsContract.buildChildDocumentsUriUsingTree(
                    this.uri,
                    DocumentsContract.getDocumentId(this.uri)
                )
            } catch (e: IllegalArgumentException) {
                return arrayOf()
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
                        DocumentsContract.buildDocumentUriUsingTree(this.uri, cursor.getString(0))
                    results.add(documentUri)
                }
            } catch (e: Throwable) {
                LogsHandler.unhandledException(e, uri)
            } finally {
                closeQuietly(cursor)
            }
            cache[uriString] = results.map { uri ->
                StorageFile(this, context, uri)
            }.toTypedArray()
        }
        return cache[uriString] ?: arrayOf()
    }

    fun renameTo(displayName: String): Boolean {
        // noinspection OverlyBroadCatchBlock
        return try {
            val result = DocumentsContract.renameDocument(
                context.contentResolver, uri, displayName
            )
            if (result != null) {
                uri = result
                return true
            }
            false
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e, uri)
            false
        }
    }

    fun exists(): Boolean {
        return uri.exists(context)
    }

    override fun toString(): String {
        return DocumentsContract.getDocumentId(uri)
    }

    companion object {
        val cache: MutableMap<String, Array<StorageFile>> = mutableMapOf()
        var cacheDirty = true

        fun fromUri(context: Context, uri: Uri): StorageFile {
            // Todo: Figure out what's wrong with the Uris coming from the intent and why they need to be processed
            //  with DocumentsContract.buildDocumentUriUsingTree(value, DocumentsContract.getTreeDocumentId(value)) first
            return StorageFile(null, context, uri)
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