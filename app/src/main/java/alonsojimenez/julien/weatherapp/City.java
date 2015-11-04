package alonsojimenez.julien.weatherapp;

import android.database.Cursor;

import alonsojimenez.julien.weatherapp.data.DatabaseHelper;

/**
 * Created by julien on 25/09/15.
 */
public class City
{
    private String name;
    private String country;
    private String lastUpdate;
    private String windSpeed;
    private String windDirection;
    private String pressure;
    private String temperature;

    public City(String name, String country)
    {
        this.name = name;
        this.country = country;
        this.lastUpdate = null;
    }

    public City(Cursor cursor)
    {
        if(cursor == null)
            return;

        cursor.moveToFirst();

        this.name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME_COL));
        this.country = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COUNTRY_COL));

        String lastUpdt = cursor.getString(cursor.getColumnIndex(DatabaseHelper.DATE_COL));
        if(lastUpdt == null || lastUpdt.isEmpty())
            return;

        this.lastUpdate = lastUpdt;
        this.windSpeed = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WIND_SPEED_COL));
        this.windDirection = cursor.getString(cursor.getColumnIndex(DatabaseHelper.WIND_DIR_COL));
        this.pressure = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PRESSURE_COL));
        this.temperature = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TEMPERATURE_COL));
        cursor.close();
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getWindSpeed() {
        return windSpeed;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public String getPressure() {
        return pressure;
    }

    public String getTemperature() {
        return temperature;
    }

    @Override
    public String toString()
    {
        return name + " (" + country + ")";
    }
}
