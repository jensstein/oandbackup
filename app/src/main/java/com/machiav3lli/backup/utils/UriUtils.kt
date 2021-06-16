/*
 * OAndBackupX: open-source apps backup and restore app.
 * Copyright (C) 2020  Antonios Hazim
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.machiav3lli.backup.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import com.machiav3lli.backup.handler.LogsHandler
import java.io.*
import java.nio.charset.StandardCharsets


@Throws(FileNotFoundException::class)
fun Uri.openFileForReading(context: Context): BufferedReader {
    return BufferedReader(
        InputStreamReader(context.contentResolver.openInputStream(this), StandardCharsets.UTF_8)
    )
}

@Throws(FileNotFoundException::class)
fun Uri.openFileForWriting(context: Context, mode: String?): BufferedWriter {
    return BufferedWriter(
        OutputStreamWriter(
            context.contentResolver.openOutputStream(this, mode!!),
            StandardCharsets.UTF_8
        )
    )
}

fun Uri.getName(context: Context): String? = try {
    context.contentResolver.query(this, null, null, null, null)?.let { cursor ->
        cursor.run {
            if (moveToFirst()) getString(getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME))
            else null
        }.also { cursor.close() }
    }
} catch (e: Throwable) {
    LogsHandler.unhandledException(e, this)
    null
}

private fun Uri.getRawType(context: Context): String? = try {
    context.contentResolver.query(this, null, null, null, null)?.let { cursor ->
        cursor.run {
            if (moveToFirst())
                context.contentResolver.getType(this@getRawType)
            else
                null
        }.also { cursor.close() }
    }
} catch (e: Throwable) {
    LogsHandler.unhandledException(e, this)
    null
}

fun Uri.getFlags(context: Context): Long =
    queryForLong(context, DocumentsContract.Document.COLUMN_FLAGS)

fun Uri.isDirectory(context: Context): Boolean =
    DocumentsContract.Document.MIME_TYPE_DIR == getRawType(context)

fun Uri.isFile(context: Context): Boolean {
    val type = getRawType(context)
    return !TextUtils.isEmpty(type) && type != DocumentsContract.Document.MIME_TYPE_DIR
}

fun Uri.lastModified(context: Context): Long =
    queryForLong(context, DocumentsContract.Document.COLUMN_LAST_MODIFIED)

fun Uri.length(context: Context): Long =
    queryForLong(context, DocumentsContract.Document.COLUMN_SIZE)

fun Uri.canRead(context: Context): Boolean = when {
    context.checkCallingOrSelfUriPermission(
        this,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    ) // Ignore if grant doesn't allow read
            != PackageManager.PERMISSION_GRANTED -> false
    else -> !TextUtils.isEmpty(getRawType(context))
} // Ignore documents without MIME

fun Uri.canWrite(context: Context): Boolean {
    // Ignore if grant doesn't allow write
    if (context.checkCallingOrSelfUriPermission(this, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return false
    }
    val type = getRawType(context)
    val flags = queryForLong(context, DocumentsContract.Document.COLUMN_FLAGS).toInt()
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

fun Uri.exists(context: Context): Boolean {
    val resolver = context.contentResolver
    var cursor: Cursor? = null
    return try {
        cursor = resolver.query(
            this, arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID
            ), null, null, null
        )
        cursor?.count ?: 0 > 0
    } catch (e: Throwable) {
        LogsHandler.unhandledException(e, this)
        false
    } finally {
        closeQuietly(cursor)
    }
}

private fun Uri.queryForLong(context: Context, column: String): Long {
    val resolver = context.contentResolver
    var cursor: Cursor? = null
    return try {
        cursor = resolver.query(this, arrayOf(column), null, null, null)
        if (cursor!!.moveToFirst() && !cursor.isNull(0)) {
            cursor.getLong(0)
        } else 0
    } catch (e: Throwable) {
        LogsHandler.unhandledException(e, "$this column: $column")
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
        } catch (e: Throwable) {
            LogsHandler.unhandledException(e)
        }
    }
}