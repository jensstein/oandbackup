package com.machiav3lli.backup.handler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import android.util.Log
import android.webkit.MimeTypeMap
import com.machiav3lli.backup.Constants.classTag

object DocumentContractApi {
    val TAG = classTag(".DocumentContractApi")
    private const val MIME_TYPE_PROPERTY_FILE = "bin"

    fun getName(context: Context, uri: Uri): String? = try {
        context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            cursor.run {
                if (moveToFirst()) getString(getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
                else null
            }.also { cursor.close() }
        }
    } catch (e: Exception) {
        null
    }

    private fun getRawType(context: Context, uri: Uri): String? = try {
        context.contentResolver.query(uri, null, null, null, null)?.let { cursor ->
            cursor.run {
                val mimeTypeMap: MimeTypeMap = MimeTypeMap.getSingleton()
                if (moveToFirst()) mimeTypeMap.getExtensionFromMimeType(context.contentResolver.getType(uri))
                else null
            }.also { cursor.close() }
        }
    } catch (e: Exception) {
        null
    }

    fun getFlags(context: Context, self: Uri): Long = queryForLong(context, self, DocumentsContract.Document.COLUMN_FLAGS)

    fun isDirectory(context: Context, self: Uri): Boolean = DocumentsContract.Document.MIME_TYPE_DIR == getRawType(context, self)

    fun isFile(context: Context, self: Uri): Boolean {
        val type = getRawType(context, self)
        return !(DocumentsContract.Document.MIME_TYPE_DIR == type || TextUtils.isEmpty(type))
    }

    fun isPropertyFile(context: Context, self: Uri): Boolean = MIME_TYPE_PROPERTY_FILE == getRawType(context, self)

    fun lastModified(context: Context, self: Uri): Long = queryForLong(context, self, DocumentsContract.Document.COLUMN_LAST_MODIFIED)

    fun length(context: Context, self: Uri): Long = queryForLong(context, self, DocumentsContract.Document.COLUMN_SIZE)

    fun canRead(context: Context, self: Uri): Boolean = if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_READ_URI_PERMISSION) // Ignore if grant doesn't allow read
            != PackageManager.PERMISSION_GRANTED) false
    else !TextUtils.isEmpty(getRawType(context, self)) // Ignore documents without MIME

    fun canWrite(context: Context, self: Uri): Boolean {
        // Ignore if grant doesn't allow write
        if (context.checkCallingOrSelfUriPermission(self, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        val type = getRawType(context, self)
        val flags = queryForLong(context, self, DocumentsContract.Document.COLUMN_FLAGS).toInt()
        // Ignore documents without MIME
        if (TextUtils.isEmpty(type)) {
            return false
        }
        // Deletable documents considered writable
        if (flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE != 0) {
            return true
        }
        return if (DocumentsContract.Document.MIME_TYPE_DIR == type && flags and DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE != 0) {
            // Directories that allow create considered writable
            true
        } else !TextUtils.isEmpty(type)
                && flags and DocumentsContract.Document.FLAG_SUPPORTS_WRITE != 0
        // Writable normal files considered writable
    }

    fun exists(context: Context, self: Uri?): Boolean {
        val resolver = context.contentResolver
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(self!!, arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null)
            cursor!!.count > 0
        } catch (e: Exception) {
            Log.w(TAG, "Failed query: $e")
            false
        } finally {
            closeQuietly(cursor)
        }
    }

    private fun queryForLong(context: Context, self: Uri, column: String): Long {
        val resolver = context.contentResolver
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(self, arrayOf(column), null, null, null)
            if (cursor!!.moveToFirst() && !cursor.isNull(0)) {
                cursor.getLong(0)
            } else 0
        } catch (e: Exception) {
            Log.w(TAG, "Failed query: $e")
            0
        } finally {
            closeQuietly(cursor)
        }
    }

    private fun closeQuietly(closeable: AutoCloseable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (ignored: Exception) {
            }
        }
    }
}