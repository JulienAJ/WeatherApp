package alonsojimenez.julien.weatherapp;

import android.accounts.Account;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import alonsojimenez.julien.weatherapp.data.Provider;
import alonsojimenez.julien.weatherapp.synchronization.WeatherSyncAdapter;


public class CityView extends Activity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private City city;
    private Receiver receiver;
    private Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_view);

        receiver = new Receiver();
        IntentFilter filter = new IntentFilter(WeatherService.BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(receiver, filter);

        String cityName = getIntent().getStringExtra(Constants.CITY_NAME);
        String cityCountry = getIntent().getStringExtra(Constants.CITY_COUNTRY);

        city = new City(cityName, cityCountry);

        account = CityListActivity.createSyncAccount(this);

        reLoadCity();
        updateUI();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_city_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh_city)
        {
            // PARTIE INTENT SERVICE
            /*Intent intent = new Intent(this, WeatherService.class);
            intent.putExtra(WeatherService.UPDATE_ALL, false);
            intent.putExtra(Constants.CITY_NAME, city.getName());
            intent.putExtra(Constants.CITY_COUNTRY, city.getCountry());
            startService(intent);
            return true;*/

            // PARTIE SYNC SERVICE

            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            bundle.putBoolean(WeatherSyncAdapter.UPDATE_ALL, false);
            bundle.putString(Constants.CITY_NAME, city.getName());
            bundle.putString(Constants.CITY_COUNTRY, city.getCountry());
            ContentResolver.requestSync(account, Provider.CONTENT_AUTHORITY, bundle);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop()
    {
        unregisterReceiver(receiver);
        super.onStop();
    }

    private void reLoadCity()
    {
        Cursor cursor = getContentResolver().query(Provider.buildCityUri(city.getName(),
                city.getCountry()),null, null, null, null);

        if(cursor == null)
            finish();

        city = new City(cursor);
    }

    private void updateUI()
    {
        TextView cityName = (TextView) findViewById(R.id.cityValue);
        TextView country = (TextView) findViewById(R.id.countryValue);
        TextView wind = (TextView) findViewById(R.id.windValue);
        TextView temperature = (TextView) findViewById(R.id.temperatureValue);
        TextView date = (TextView) findViewById(R.id.dateValue);
        TextView pressure = (TextView) findViewById(R.id.pressureValue);

        cityName.setText(city.getName());
        country.setText(city.getCountry());
        if(city.getWindDirection() != null)
            wind.setText(city.getWindSpeed() + " (" + city.getWindDirection() + ")");
        if(city.getTemperature() != null)
            temperature.setText(city.getTemperature());
        if(city.getLastUpdate() != null)
            date.setText(city.getLastUpdate());
        if(city.getPressure() != null)
            pressure.setText(city.getPressure());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        Uri uri = Uri.parse(Provider.CONTENT_URI + city.getName() + "/" + city.getCountry());
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        updateUI();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {}


    // PARTIE INTENT SERVICE
    private class Receiver extends BroadcastReceiver
    {
        public Receiver()
        {}

        @Override
        public void onReceive(Context context, Intent intent)
        {
            reLoadCity();
            updateUI();
        }
    }
}
