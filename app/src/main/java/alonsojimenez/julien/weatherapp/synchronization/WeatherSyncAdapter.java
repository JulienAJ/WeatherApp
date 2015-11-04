package alonsojimenez.julien.weatherapp.synchronization;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import alonsojimenez.julien.weatherapp.Constants;
import alonsojimenez.julien.weatherapp.data.DatabaseHelper;
import alonsojimenez.julien.weatherapp.data.Provider;
import alonsojimenez.julien.weatherapp.R;
import alonsojimenez.julien.weatherapp.webservice.WebServiceHandler;

/**
 * Created by julien on 30/10/15.
 */
public class WeatherSyncAdapter extends AbstractThreadedSyncAdapter
{
    public static final String BROADCAST_ACTION = "updateOver";
    public static final String UPDATE_ALL = "updateAll";

    private ContentResolver contentResolver;
    private static final String SERVICE_TAG = "SERVICE";

    public WeatherSyncAdapter(Context context, boolean autoInitialize)
    {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult)
    {

        boolean updateAll = true;
        if(extras != null || extras == Bundle.EMPTY)
            updateAll = extras.getBoolean(UPDATE_ALL, true);

        if(!updateAll)
        {
            String cityName = extras.getString(Constants.CITY_NAME);
            String country = extras.getString(Constants.CITY_COUNTRY);

            updateOne(cityName, country);

            Intent localIntent = new Intent(BROADCAST_ACTION);
            getContext().sendBroadcast(localIntent);
        }
        else
        {
            Cursor cursor = contentResolver.query(Uri.parse(Provider.CONTENT_URI),
                    null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                updateOne(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_COL)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COUNTRY_COL)));
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    private void updateOne(String name, String country)
    {
        Log.i(SERVICE_TAG,
                getContext().getString(R.string.updating) + " " + name + " (" + country + ")");
        List<String> result = WebServiceHandler.getData(name, country);

        if(result == null || result.size() < 4)
            return;

        if(result.get(WebServiceHandler.TIME) == null)
            return;

        else
        {
            String tempWind = result.get(WebServiceHandler.WIND);
            String temperature = result.get(WebServiceHandler.TEMP);
            String pressure = result.get(WebServiceHandler.PRESS);
            String lastUpdate = result.get(WebServiceHandler.TIME);
            String splited[] = tempWind.split("\\(");
            String windSpeed = splited[0].trim();
            String windDirection = "";
            if(splited.length > 1)
                windDirection = splited[1].replace(")", "").trim();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.TEMPERATURE_COL, temperature);
            contentValues.put(DatabaseHelper.PRESSURE_COL, pressure);
            contentValues.put(DatabaseHelper.DATE_COL, lastUpdate);
            contentValues.put(DatabaseHelper.WIND_SPEED_COL, windSpeed);
            contentValues.put(DatabaseHelper.WIND_DIR_COL, windDirection);

            contentResolver.update(Provider.buildCityUri(name, country),
                    contentValues, null, null);

            Log.i(SERVICE_TAG,
                     name + " (" + country + ") " + getContext().getString(R.string.updated));
        }
    }
}
