package alonsojimenez.julien.weatherapp.synchronization;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by julien on 30/10/15.
 */
public class SyncService extends Service
{
    private static WeatherSyncAdapter adapter = null;
    private static final Object adapterLock = new Object();

    @Override
    public void onCreate()
    {
        synchronized(adapterLock)
        {
            if(adapter == null)
                adapter = new WeatherSyncAdapter(getApplicationContext(), true);
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return adapter.getSyncAdapterBinder();
    }
}
