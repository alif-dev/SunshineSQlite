package com.example.alif.sunshinesqlite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Created by alif on 04/04/18.
 */

public class WeatherDetailsActivity extends AppCompatActivity {
    TextView weatherDetailsTextView;
    String weatherDetailsString;
    String SUNSHINE_HASHTAG = " #SunshineApp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_details);

        weatherDetailsTextView = findViewById(R.id.weather_details);

        Intent intent = getIntent();

        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            weatherDetailsString = intent.getStringExtra(Intent.EXTRA_TEXT);
            weatherDetailsTextView.setText(weatherDetailsString);
        }

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        // code for share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(shareWeatherDataToOtherApps());
        return super.onCreateOptionsMenu(menu);
    }

    private Intent shareWeatherDataToOtherApps() {
        Intent intent = ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText(weatherDetailsString + SUNSHINE_HASHTAG)
                .getIntent();
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedItemId = item.getItemId();
        if (selectedItemId == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
