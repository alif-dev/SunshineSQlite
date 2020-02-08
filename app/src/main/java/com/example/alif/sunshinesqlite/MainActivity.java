package com.example.alif.sunshinesqlite;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alif.sunshinesqlite.data.WeatherContract;
import com.example.alif.sunshinesqlite.data.WeatherDbAdapter;
import com.example.alif.sunshinesqlite.data.WeatherDbHelper;
import com.example.alif.sunshinesqlite.tools.Networking;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements
        WeatherAdapter.OnWeatherListClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<String[]> {

    RecyclerView recyclerView;
    WeatherAdapter adapter;
    long oneDayInMs = 60 * 60 * 24 * 1000; // 60 * 60 * 24 * 1000 ms
    String location = null;
    String units = null;
    TextView mErrorMessage;
    ProgressBar mProgressBar;
    int LOADER_ID = 1;
    Bundle preferenceBundle = new Bundle();
    String KEY_FOR_LOCATION_BUNDLE = "location";
    String KEY_FOR_UNITS_BUNDLE = "units";
    SQLiteDatabase mDatabase;
    ContentValues cv;
    WeatherDbAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recycler_view);
        mErrorMessage = findViewById(R.id.tv_error_message);
        mProgressBar = findViewById(R.id.progress_bar);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        adapter = new WeatherAdapter(this);
        /*recyclerView.setAdapter(adapter);*/

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);

        // get location and temperature in SharedPreferences
        getLocationInPreference(sharedPreferences);
        getTemperatureUnitsInPreference(sharedPreferences);

        LoaderManager.LoaderCallbacks<String[]> callbacks = MainActivity.this;
        preferenceBundle.putString(KEY_FOR_LOCATION_BUNDLE, location); // put location in Bundle
        preferenceBundle.putString(KEY_FOR_UNITS_BUNDLE, units);
        getSupportLoaderManager().initLoader(LOADER_ID, preferenceBundle, callbacks);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Caching weather data into database
        WeatherDbHelper dbHelper = new WeatherDbHelper(this);
        mDatabase = dbHelper.getWritableDatabase();
    }

    private void getTemperatureUnitsInPreference(SharedPreferences sharedPreferences) {
        units = sharedPreferences.getString(
                getString(R.string.pref_temperature_units_key),
                getString(R.string.pref_temperature_units_metric));
    }

    public void getLocationInPreference(SharedPreferences sharedPreferences) {
        location = sharedPreferences.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onWeatherDataClick(String weatherDataForToday) {
        Intent intent = new Intent(this, WeatherDetailsActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, weatherDataForToday);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_key))) {
            getLocationInPreference(sharedPreferences);
            preferenceBundle.putString(KEY_FOR_LOCATION_BUNDLE, location);
            getSupportLoaderManager().restartLoader(LOADER_ID, preferenceBundle, this);
        }
        if (key.equals(getString(R.string.pref_temperature_units_key))) {
            getTemperatureUnitsInPreference(sharedPreferences);
            preferenceBundle.putString(KEY_FOR_UNITS_BUNDLE, units);
            getSupportLoaderManager().restartLoader(LOADER_ID, preferenceBundle, this);
        }
    }

    @NonNull
    @Override
    public Loader<String[]> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {
            // Data yang sudah didapat dari API dicache ke array String bernama mWeatherData
            // Kenapa harus dicache? Karena supaya data tidak terus-menerus meload data dari API
            // setiap kali kita membuka Detail Cuaca atau membuka settings tanpa melakukan perubahan apa-apa.
            String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    Toast.makeText(getApplicationContext(), "Error! There is no preferences set!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mWeatherData != null) {
                    deliverResult(mWeatherData);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public String[] loadInBackground() {
                String preferredLocation = args.getString(KEY_FOR_LOCATION_BUNDLE);
                String preferredUnits = args.getString(KEY_FOR_UNITS_BUNDLE);
                URL url = Networking.buildUrl(preferredLocation, preferredUnits);
                try {
                    String jsonData = Networking.getUrlResponse(url);
                    String[] weatherData = null;
                    if (jsonData != null || !jsonData.equals("")) {
                        weatherData = getWeatherData(jsonData);
                    }
                    return weatherData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(@Nullable String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String[]> loader, String[] weatherData) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (weatherData != null) {
            recyclerView.setAdapter(adapter);
            showWeatherData();
            adapter.setWeatherData(weatherData);
        } else {
            //showErrorMessage();
            Cursor cursor = getAllWeatherData();
            dbAdapter = new WeatherDbAdapter(cursor);
            recyclerView.setAdapter(dbAdapter);
            showWeatherData();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String[]> loader) {

    }

    private String[] getWeatherData(String jsonData) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonData);
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        String[] weatherData = new String[jsonArray.length()];
        cv = new ContentValues(); // container to cache data into database
        mDatabase.delete(WeatherContract.WeatherEntry.TABLE_NAME, null, null);

        for (int i = 0; i < jsonArray.length(); i++) {
            // get day description
            long currentDayInMs = System.currentTimeMillis();
            long iterationDateMs = currentDayInMs + i * oneDayInMs;
            String dayDescription = "";
            if (iterationDateMs == currentDayInMs) {
                dayDescription = "Today, ";
            } else if (iterationDateMs == currentDayInMs + oneDayInMs) {
                dayDescription = "Tomorrow, ";
            }

            // get date
            String date = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US).format(iterationDateMs);
            // cache date
            cv.put(WeatherContract.WeatherEntry.COLUMN_DATE, iterationDateMs);

            // get weather
            JSONObject jsonObject1 = jsonArray.getJSONObject(i); // get all of data in "list" json
            JSONArray jsonArrayWeather = jsonObject1.getJSONArray("weather");
            JSONObject jsonObjectWeather0 = jsonArrayWeather.getJSONObject(0);
            String weather = jsonObjectWeather0.getString("description");
            // cache weather
            cv.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_DESCRIPTION, weather);

            // get min and max temperature
            JSONObject jsonObjectTemperature = jsonObject1.getJSONObject("temp");
            double minTemperature = Math.round(jsonObjectTemperature.getDouble("min"));
            String minTemperatureString = String.format(getString(R.string.celcius_format), minTemperature);
            double maxTemperature = Math.round(jsonObjectTemperature.getDouble("max"));
            String maxTemperatureString = String.format(getString(R.string.celcius_format), maxTemperature);
            // cache min and max temperature
            cv.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, minTemperatureString);
            cv.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, maxTemperatureString);

            weatherData[i] = dayDescription + date + "\n"
                    + "Weather: " + weather + "\n"
                    + "Temperature: " + minTemperatureString + " - " + maxTemperatureString;
            // cache all weather data into database
            mDatabase.insert(WeatherContract.WeatherEntry.TABLE_NAME, null, cv);
        }
        return weatherData;
    }

    private void openLocationInMap() {
        String locationAddress = location;
        Uri geoLocation = Uri.parse("geo:0,0?q=" + locationAddress);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Could not call " + geoLocation.toString()
                    + ", no receiving apps found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void invalidateData() {
        adapter.setWeatherData(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedItemId = item.getItemId();

        if (selectedItemId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        if (selectedItemId == R.id.action_refresh) {
            invalidateData();
            //preferenceBundle.putString(KEY_FOR_LOCATION_BUNDLE, location);
            //preferenceBundle.putString(KEY_FOR_UNITS_BUNDLE, units);
            getSupportLoaderManager().restartLoader(LOADER_ID, preferenceBundle, this);
        }

        if (selectedItemId == R.id.action_map) {
            openLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showWeatherData() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void showErrorMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    private Cursor getAllWeatherData() {
        return mDatabase.query(WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                WeatherContract.WeatherEntry.COLUMN_DATE);
    }
}
