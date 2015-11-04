package alonsojimenez.julien.weatherapp.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by julien on 09/10/15.
 */
public class Provider extends ContentProvider
{
    public static final String CONTENT_AUTHORITY = "alonsojimenez.julien.weatherapp";
    public static final String BASE_URI = "weather";
    public static final String CONTENT_URI = "content://" + CONTENT_AUTHORITY + "/" + BASE_URI + "/";
    public static final int ALL_CITIES_CODE = 1;
    public static final int SINGLE_CITY_CODE = 2;
    public static final int NAME_POS_IN_URI = 1;
    public static final int COUNTRY_POS_IN_URI = 2;

    private DatabaseHelper database;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(CONTENT_AUTHORITY, BASE_URI, ALL_CITIES_CODE);
        uriMatcher.addURI(CONTENT_AUTHORITY, BASE_URI + "/*/*", SINGLE_CITY_CODE);
    }

    @Override
    public boolean onCreate()
    {
        database = new DatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(DatabaseHelper.TABLE_NAME);

        switch(uriMatcher.match(uri))
        {
            case SINGLE_CITY_CODE:
                queryBuilder.appendWhere(DatabaseHelper.NAME_COL + " = \""
                        + uri.getPathSegments().get(NAME_POS_IN_URI) + "\" AND "
                        + DatabaseHelper.COUNTRY_COL + " = \""
                        + uri.getPathSegments().get(COUNTRY_POS_IN_URI) + "\"");
                break;

            case ALL_CITIES_CODE:
                return database.getAllCitiesCursor();

            default:
                throw new IllegalArgumentException();
        }

        return queryBuilder.query(database.getReadableDatabase(), projection, selection,
                selectionArgs, null, null, sortOrder);
    }

    @Override
    public String getType(Uri uri)
    {
        switch(uriMatcher.match(uri))
        {
            case ALL_CITIES_CODE:
                return ContentResolver.CURSOR_DIR_BASE_TYPE;

            case SINGLE_CITY_CODE:
                return ContentResolver.CURSOR_ITEM_BASE_TYPE;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        switch(uriMatcher.match(uri))
        {
            case ALL_CITIES_CODE:
                String name = (String)values.get(DatabaseHelper.NAME_COL);
                String country = (String)values.get(DatabaseHelper.COUNTRY_COL);
                database.addCity(name, country); //returns long
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(BASE_URI + "/" + name + "/" + country);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        switch(uriMatcher.match(uri))
        {
            case SINGLE_CITY_CODE:
                String name = uri.getPathSegments().get(NAME_POS_IN_URI);
                String country = uri.getPathSegments().get(COUNTRY_POS_IN_URI);
                database.removeCity(name, country);
                getContext().getContentResolver().notifyChange(uri, null);
                return 1; // For 1 row removed
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        switch ((uriMatcher.match(uri)))
        {
            case SINGLE_CITY_CODE:
                String name = uri.getPathSegments().get(NAME_POS_IN_URI);
                String country = uri.getPathSegments().get(COUNTRY_POS_IN_URI);
                long temp = database.update(name, country, values);
                getContext().getContentResolver().notifyChange(uri, null);
                return (int)temp;

            default:
                return 0;
        }
    }

    public static Uri buildCityUri(String city, String country)
    {
        return Uri.parse(CONTENT_URI + city + "/" + country + "/");
    }
}
