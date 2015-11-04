package alonsojimenez.julien.weatherapp;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import alonsojimenez.julien.weatherapp.data.DatabaseHelper;
import alonsojimenez.julien.weatherapp.data.Provider;
import alonsojimenez.julien.weatherapp.synchronization.WeatherSyncAdapter;

public class CityListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int ADD_CITY_REQUEST_CODE = 0;
    public static final int LOADER_MANAGER_ID = 0;
    private static final String ACCOUNT_LOG_TAG = "ACC";
    private static final String CITY_LIST_TAG = "CITY_LIST";

    // PART SYNC SERVICE
    private static final String ACCOUNT_NAME = "dummyAccount";
    private static final String ACCOUNT_TYPE = "alonsojimenez.julien.weatherSyncService";
    private Account account;
    // /PART SYNC SERVICE


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_2,
                null, new String[]{DatabaseHelper.NAME_COL, DatabaseHelper.COUNTRY_COL},
                new int[] { android.R.id.text1, android.R.id.text2 }, 0);

        getLoaderManager().initLoader(LOADER_MANAGER_ID, null, this);

        setListAdapter(cursorAdapter);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(CityListActivity.this);

                final String selectedCity = ((TextView)view.findViewById(android.R.id.text1))
                        .getText().toString();
                final String selectedCountry = ((TextView)view.findViewById(android.R.id.text2))
                        .getText().toString();

                builder.setMessage(getString(R.string.remove) + " " + selectedCity + " ?")
                        .setTitle(R.string.removeTitle)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getContentResolver()
                                        .delete(
                                                Provider.buildCityUri(selectedCity, selectedCountry)
                                                , null, null);
                                getLoaderManager().restartLoader(LOADER_MANAGER_ID, null,
                                        CityListActivity.this);
                                Log.i(CITY_LIST_TAG, selectedCity + " (" + selectedCountry + ") "
                                        + getString(R.string.removed));
                                Toast.makeText(CityListActivity.this, selectedCity
                                        + " " + getString(R.string.removed), Toast.LENGTH_LONG).show();
                            }
                        }).show();

                return true;
            }
        });
        // PART SYNC SERVICE
        account = createSyncAccount(this);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String intervalString = sharedPreferences
                .getString(Constants.INTERVAL_KEY, Constants.DEFAULT_INTERVAL);
        Long interval = new Long(intervalString);

        ContentResolver resolver = getContentResolver();
        resolver.removePeriodicSync(account, Provider.CONTENT_AUTHORITY, Bundle.EMPTY);
        resolver.setIsSyncable(account, Provider.CONTENT_AUTHORITY, 1);
        resolver.addPeriodicSync(account, Provider.CONTENT_AUTHORITY, Bundle.EMPTY, interval);
        // /PART SYNC SERVICE

        // FILL DB ON FIRST LAUNCH
        boolean isFirstLaunch = sharedPreferences.getBoolean(Constants.FIRST_LAUNCH_KEY, true);
        if(isFirstLaunch)
        {
            for(int i = 0; i < Constants.DEFAULT_DATA.length; i++)
            {
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.NAME_COL,
                        Constants.DEFAULT_DATA[i][Constants.CITY_POS_IN_DATA]);
                values.put(DatabaseHelper.COUNTRY_COL,
                        Constants.DEFAULT_DATA[i][Constants.COUNTRY_POS_IN_DATA]);
                getContentResolver()
                        .insert(Uri.parse(Provider.CONTENT_URI), values);
                getLoaderManager().restartLoader(LOADER_MANAGER_ID, null, CityListActivity.this);
            }
            sharedPreferences.edit().putBoolean(Constants.FIRST_LAUNCH_KEY, false).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_city_list, menu);
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
        if(id == R.id.action_refresh)
        {
            // PARTIE INTENT SERVICE
            /*Intent intent = new Intent(this, WeatherService.class);
            intent.putExtra(WeatherService.UPDATE_ALL, true);
            startService(intent);
            return true;*/

            // PARTIE SYNC
            Bundle bundle = new Bundle();
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
            bundle.putBoolean(WeatherSyncAdapter.UPDATE_ALL, true);
            ContentResolver.requestSync(account, Provider.CONTENT_AUTHORITY, bundle);
        }
        else if(id == R.id.action_add)
        {
            Intent intent = new Intent(this, AddCityActivity.class);
            startActivityForResult(intent, ADD_CITY_REQUEST_CODE);
        }
        else if(id == R.id.action_settings)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode == RESULT_OK)
        {
            if(requestCode == ADD_CITY_REQUEST_CODE)
            {
                String newName = data.getStringExtra(Constants.CITY_NAME);
                String newCountry = data.getStringExtra(Constants.CITY_COUNTRY);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.NAME_COL, newName);
                values.put(DatabaseHelper.COUNTRY_COL, newCountry);
                getContentResolver()
                        .insert(Uri.parse(Provider.CONTENT_URI), values);
                getLoaderManager().restartLoader(LOADER_MANAGER_ID, null, CityListActivity.this);
                Log.i(CITY_LIST_TAG, newName + " (" + newCountry + ") "
                        + getString(R.string.added));
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        String selectedCity = ((TextView)v.findViewById(android.R.id.text1)).getText().toString();
        String selectedCountry = ((TextView)v.findViewById(android.R.id.text2)).getText().toString();

        Intent intent = new Intent(this ,CityView.class);
        intent.putExtra(Constants.CITY_NAME, selectedCity);
        intent.putExtra(Constants.CITY_COUNTRY, selectedCountry);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args)
    {
        return new CursorLoader(this, Uri.parse(Provider.CONTENT_URI), null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        ((SimpleCursorAdapter)getListAdapter()).changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        ((SimpleCursorAdapter)getListAdapter()).changeCursor(null);
    }

    // PART SYNC SERVICE
    public static Account createSyncAccount(Context context)
    {
        Account newAccount = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
        AccountManager accountManager = (AccountManager)context.getSystemService(ACCOUNT_SERVICE);

        Log.i(ACCOUNT_LOG_TAG, context.getString(R.string.creatingAccount));
        boolean result = accountManager.addAccountExplicitly(newAccount, null, null);

        if(result)
        {
            Log.i(ACCOUNT_LOG_TAG, context.getString(R.string.accountSuccess));
            return newAccount;
        }
        else if(!result)
        {
            Log.i(ACCOUNT_LOG_TAG, context.getString(R.string.accountExists));
            return newAccount;
        }
        else
        {
            Toast.makeText(context, context.getString(R.string.accountError), Toast.LENGTH_LONG)
                    .show();
            Log.i(ACCOUNT_LOG_TAG, context.getString(R.string.accountError));
            return null;
        }
    }
}
