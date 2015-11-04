package alonsojimenez.julien.weatherapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import alonsojimenez.julien.weatherapp.City;

/**
 * Created by julien on 07/10/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String DB_NAME = "weatherData.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "weatherData";
    private static final String ID_COL = "_id";
    public static final String NAME_COL = "cityName";
    public static final String COUNTRY_COL = "country";
    public static final String DATE_COL = "lastUpdate";
    public static final String WIND_SPEED_COL = "windSpeed";
    public static final String WIND_DIR_COL = "windDirection";
    public static final String PRESSURE_COL = "pressure";
    public static final String TEMPERATURE_COL = "temperature";

    public DatabaseHelper(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String statement = "create table if not exists " + TABLE_NAME + "("
                + ID_COL + " integer primary key autoincrement, "
                + NAME_COL + " text not null, "
                + COUNTRY_COL + " text not null, "
                + DATE_COL + " text, "
                + WIND_SPEED_COL + " text, "
                + WIND_DIR_COL + " text, "
                + PRESSURE_COL + " text, "
                + TEMPERATURE_COL + " text, "
                + "UNIQUE(" + NAME_COL + "," + COUNTRY_COL + "))";

        db.execSQL(statement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if(oldVersion != newVersion)
        {
            db.execSQL("drop table if exists " + TABLE_NAME);
            onCreate(db);
        }
    }

    public long addCity(String name, String country)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NAME_COL, name);
        values.put(COUNTRY_COL, country);

        long id = db.insert(TABLE_NAME, null, values);

        // Returns long (row id of the new city, -1 if fail)
        return id;
    }

    public long removeCity(String name, String country)
    {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.delete(TABLE_NAME, NAME_COL + "=? AND " + COUNTRY_COL + "=?",
                new String[]{name, country});
        return id;
    }

    public long update(String name, String country, ContentValues values)
    {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLE_NAME, values, NAME_COL + " =? AND " + COUNTRY_COL + " =? ",
                new String[]{name, country});
    }

    public List<City> getAllCities()
    {
        SQLiteDatabase db = getReadableDatabase();
        List<City> result = new ArrayList<>();

        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);

        if(!cursor.moveToFirst())
            return null;

        do
        {
            result.add(new City(cursor.getString(cursor.getColumnIndex(NAME_COL)),
                    cursor.getString(cursor.getColumnIndex(COUNTRY_COL))));
        }
        while(cursor.moveToNext());

        db.close();
        cursor.close();
        return result;
    }

    public Cursor getAllCitiesCursor()
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, null);
        return cursor;
    }
}