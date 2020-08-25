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
package com.machiav3lli.backup.schedules;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class BlacklistsDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "blacklists.db";

    public BlacklistsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void deleteBlacklistFromId(SQLiteDatabase db, int id) {
        String selection = String.format("%s = ?",
                BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID);
        String[] selectionArgs = {String.valueOf(id)};
        db.delete(BlacklistContract.BlacklistEntry.TABLE_NAME, selection,
                selectionArgs);
    }

    public List<String> getBlacklistedPackages(SQLiteDatabase db, int id) {
        String[] projection = {BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME};
        String selection = String.format("%s = ?",
                BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID);
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(BlacklistContract.BlacklistEntry.TABLE_NAME,
                projection, selection, selectionArgs, null, null, null);
        ArrayList<String> packagenames = new ArrayList<>();
        while (cursor.moveToNext()) {
            int packagenameId = cursor.getColumnIndex(
                    BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME);
            packagenames.add(cursor.getString(packagenameId));
        }
        cursor.close();
        return packagenames;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BlacklistContract.CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no break since changes should be propagated
        if (oldVersion == 1) {
            changeBlacklistIdType(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void changeBlacklistIdType(SQLiteDatabase db) {
        String renameTable = "alter table blacklists rename to blacklists_old";
        String moveData = String.format(
                "insert into %s(%s, %s, %s)" +
                        "select _id, packagename, blacklistId from blacklists_old",
                BlacklistContract.BlacklistEntry.TABLE_NAME,
                BlacklistContract.BlacklistEntry._ID,
                BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME,
                BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID);
        String deleteTmpTable = "drop table blacklists_old";
        db.execSQL(renameTable);
        db.execSQL(BlacklistContract.CREATE_DB);
        db.execSQL(moveData);
        db.execSQL(deleteTmpTable);
    }
}
