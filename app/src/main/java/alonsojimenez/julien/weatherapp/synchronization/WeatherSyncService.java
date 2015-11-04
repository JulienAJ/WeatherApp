package alonsojimenez.julien.weatherapp.synchronization;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by julien on 30/10/15.
 */
public class WeatherSyncService extends Service
{
    private WeatherAuthenticator authenticator;

    @Override
    public void onCreate()
    {
        authenticator = new WeatherAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return authenticator.getIBinder();
    }
}
