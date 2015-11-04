package alonsojimenez.julien.weatherapp.webservice;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by julien on 01/10/15.
 */
public class WebServiceHandler
{
    public static final int WIND = 0;
    public static final int TEMP = 1;
    public static final int PRESS = 2;
    public static final int TIME = 3;

    private static XMLResponseHandler xmlResponseHandler = null;
    private static final String SERVICE_URL =
            "http://www.webservicex.net/globalweather.asmx/GetWeather?CityName=%s&CountryName=%s";

    public static List<String> getData(String name, String country)
    {
        try
        {
            String charset = "UTF-8";
            URL urlObj = new URL(String.format(SERVICE_URL, URLEncoder.encode(name, charset),
                    URLEncoder.encode(country, charset)));
            URLConnection connection = urlObj.openConnection();
            InputStream stream = new BufferedInputStream(connection.getInputStream());

            if(xmlResponseHandler == null)
                xmlResponseHandler = new XMLResponseHandler();

            List<String> result = xmlResponseHandler.handleResponse(stream, charset);
            stream.close();
            return result;
        }
        catch(IOException e)
        {
            Log.e("URL", e.getMessage());
            return null;
        }
    }
}
