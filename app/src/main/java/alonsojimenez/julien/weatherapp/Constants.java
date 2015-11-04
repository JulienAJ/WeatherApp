package alonsojimenez.julien.weatherapp;

/**
 * Created by julien on 07/10/15.
 */
public class Constants
{
    public static final String CITY_NAME = "cityName";
    public static final String CITY_COUNTRY = "cityCountry";
    public static final String INTERVAL_KEY = "pref_freq_key";
    public static final String FIRST_LAUNCH_KEY = "first_launch_key";
    public static final String DEFAULT_INTERVAL = "1800"; //seconds

    public static final int CITY_POS_IN_DATA = 0;
    public static final int COUNTRY_POS_IN_DATA = 1;
    public static final String[][] DEFAULT_DATA  = {
            {"Brest", "France"},
            {"Dublin", "Ireland"},
            {"Istanbul", "Turkey"},
            {"Marseille", "France"},
            {"Montreal", "Canada"},
            {"Seoul", "Korea"}};
}
