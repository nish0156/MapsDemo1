package dk.tec.googlemapdemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocationDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "location.db";
    private static final int DATABASE_VERSION = 1;

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " +
                LocationContract.LocationEntry.TABLE_NAME + " (" +
                LocationContract.LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                LocationContract.LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
                LocationContract.LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL);";

        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + LocationContract.LocationEntry.TABLE_NAME);
        onCreate(db);
    }
}
