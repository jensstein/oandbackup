package dk.jens.backup;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class BlacklistsDBHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
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

    public ArrayList<String> getBlacklistedPackages(SQLiteDatabase db, int id) {
        String[] projection = {BlacklistContract.BlacklistEntry.COLUMN_PACKAGENAME};
        String selection = String.format("%s = ?",
            BlacklistContract.BlacklistEntry.COLUMN_BLACKLISTID);
        String[] selectionArgs = {String.valueOf(id)};
        Cursor cursor = db.query(BlacklistContract.BlacklistEntry.TABLE_NAME,
            projection, selection, selectionArgs, null, null, null);
        ArrayList<String> packagenames = new ArrayList<>();
        while(cursor.moveToNext()) {
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
        db.execSQL(BlacklistContract.DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
