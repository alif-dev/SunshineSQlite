package com.example.alif.sunshinesqlite.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by alif on 21/04/18.
 */

public class WeatherDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_FILE_NAME = "weather.db";
    public static final int DATABASE_VERSION = 2;

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_FILE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
/*
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " +
                WeatherContract.WeatherEntry.TABLE_NAME + " (" +
                WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WeatherContract.WeatherEntry.COLUMN_DATE + " INTEGER NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +
                "UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE)";
*/

        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " +
                WeatherContract.WeatherEntry.TABLE_NAME + " (" +
                WeatherContract.WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                WeatherContract.WeatherEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_WEATHER_DESCRIPTION + " TEXT NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
                "UNIQUE (" + WeatherContract.WeatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE)";
        db.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WeatherContract.WeatherEntry.TABLE_NAME);
        onCreate(db);
    }
}
