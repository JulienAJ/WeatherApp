package alonsojimenez.julien.weatherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class AddCityActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_city);
    }

    public void onValidate(View v)
    {
        TextView cityName = (TextView)findViewById(R.id.newCityNameValue);
        TextView cityCountry = (TextView)findViewById(R.id.newCityCountryValue);

        String newName = cityName.getText().toString();
        String newCountry = cityCountry.getText().toString();

        if(newName == null || newCountry == null || newName.isEmpty() || newCountry.isEmpty())
            Toast.makeText(this, getString(R.string.emptyAddFormMessage), Toast.LENGTH_LONG).show();

        else
        {
            Intent intent = new Intent(this, CityListActivity.class);
            intent.putExtra(Constants.CITY_NAME, newName);
            intent.putExtra(Constants.CITY_COUNTRY, newCountry);
            setResult(RESULT_OK, intent);
            finish();
        }
    }
}
