package alonsojimenez.julien.weatherapp;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

import alonsojimenez.julien.weatherapp.data.DatabaseHelper;
import alonsojimenez.julien.weatherapp.data.Provider;
import alonsojimenez.julien.weatherapp.webservice.WebServiceHandler;

/**
 * Created by julien on 29/10/15.
 * Classe Question 1, avant partie facultative
 */
public class WeatherService extends IntentService
{
    public static final String SERVICE_NAME = "WeatherService";
    public static final String BROADCAST_ACTION = "updateOver";
    public static final String UPDATE_ALL = "updateAll";

    public WeatherService()
    {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        boolean updateAll = intent.getBooleanExtra(UPDATE_ALL, true);

        if(!updateAll)
        {
            String cityName = intent.getStringExtra(Constants.CITY_NAME);
            String cityCountry = intent.getStringExtra(Constants.CITY_COUNTRY);
            updateOne(cityName, cityCountry);

            Intent localIntent = new Intent(BROADCAST_ACTION);
            sendBroadcast(localIntent);

        }
        else
        {
            Cursor cursor = getContentResolver().query(Uri.parse(Provider.CONTENT_URI),
                    null, null, null, null);
            cursor.moveToFirst();
            while(!cursor.isAfterLast())
            {
                updateOne(cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_COL)),
                        cursor.getString(cursor.getColumnIndex(DatabaseHelper.COUNTRY_COL)));
                cursor.moveToNext();
            }
            cursor.close();
        }

    }

    private void updateOne(String name, String country)
    {
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

            getContentResolver().update(Provider.buildCityUri(name, country),
                    contentValues, null, null);
        }
    }
}
