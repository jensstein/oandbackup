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
package com.machiav3lli.backup.schedules

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class BlacklistsDBHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    fun deleteBlacklistFromId(db: SQLiteDatabase, id: Int) {
        val selection = String.format("%s = ?",
                BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID)
        val selectionArgs = arrayOf(id.toString())
        db.delete(BlacklistContract.BlacklistEntry.TABLE_NAME, selection,
                selectionArgs)
    }

    fun getBlacklistedPackages(db: SQLiteDatabase, id: Int): List<String> {
        val projection = arrayOf(BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME)
        val selection = String.format("%s = ?",
                BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID)
        val selectionArgs = arrayOf(id.toString())
        val cursor = db.query(BlacklistContract.BlacklistEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, null)
        val packageNames = ArrayList<String>()
        while (cursor.moveToNext()) {
            val packageNameId = cursor.getColumnIndex(BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME)
            packageNames.add(cursor.getString(packageNameId))
        }
        cursor.close()
        return packageNames
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(BlacklistContract.CREATE_DB)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // no break since changes should be propagated
        if (oldVersion == 1) {
            changeBlacklistIdType(db)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    private fun changeBlacklistIdType(db: SQLiteDatabase) {
        val renameTable = "alter table blacklists rename to blacklists_old"
        val moveData = String.format(
                "insert into %s(%s, %s, %s)" +
                        "select _id, packagename, blacklistId from blacklists_old",
                BlacklistContract.BlacklistEntry.TABLE_NAME,
                BlacklistContract.BlacklistEntry._ID,
                BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME,
                BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID)
        val deleteTmpTable = "drop table blacklists_old"
        db.execSQL(renameTable)
        db.execSQL(BlacklistContract.CREATE_DB)
        db.execSQL(moveData)
        db.execSQL(deleteTmpTable)
    }

    companion object {
        const val DATABASE_VERSION = 2
        const val DATABASE_NAME = "blacklists.db"
    }
}