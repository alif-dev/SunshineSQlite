package com.example.alif.sunshinesqlite.data;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.alif.sunshinesqlite.R;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by alif on 27/03/18.
 */

public class WeatherDbAdapter extends RecyclerView.Adapter<WeatherDbAdapter.WeatherViewHolder> {
    String[] weatherData;
    OnWeatherListClickListener mClickListener;
    Cursor mCursor;
    long oneDayInMs = 60 * 60 * 24 * 1000; // 60 * 60 * 24 * 1000 ms

    public WeatherDbAdapter(/*OnWeatherListClickListener weatherListClickListener, */Cursor cursor) {
        /*mClickListener = weatherListClickListener;*/
        mCursor = cursor;
    }

    public interface OnWeatherListClickListener{
        void onWeatherDataClick(String weatherData);
    }

    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        Context context = parentViewGroup.getContext();
        int layoutId = R.layout.weather_list;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean attachToParentImmediately = false;

        View itemView = inflater.inflate(layoutId, parentViewGroup, attachToParentImmediately);
        return new WeatherViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        long date = mCursor.getLong(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE));
        String dateString = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US).format(date);
        // LOG
        /*long currentDayInMs = System.currentTimeMillis();
        String ItDate = new SimpleDateFormat("EEE, MMM dd, yyyy, hh:mm:ss", Locale.US).format(date);
        String CuDate = new SimpleDateFormat("EEE, MMM dd, yyyy, hh:mm:ss", Locale.US).format(date);
        Log.v("ItDate = ", ItDate + " " + String.valueOf(date));
        Log.v("CuDate = ", CuDate + " " + String.valueOf(currentDayInMs));*/
        String dayDescription = "";
        if (position == 0) {
            dayDescription = "Today, ";
        } else if (position == 1) {
            dayDescription = "Tomorrow, ";
        }

        String weatherDescription = mCursor.getString(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_DESCRIPTION));
        String min = mCursor.getString(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP));
        String max = mCursor.getString(mCursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP));

        holder.mWeatherDataTextView.setText(dayDescription + dateString + "\n"
                + "Weather: " + weatherDescription + "\n"
                + "Temperature: " + min + " - " + max);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder /*implements
            View.OnClickListener */{
        TextView mWeatherDataTextView;
        FrameLayout mRootLayout;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            mWeatherDataTextView = itemView.findViewById(R.id.tv_weather_data);
            mRootLayout = itemView.findViewById(R.id.weather_list_root_layout);
            //mRootLayout.setOnClickListener(this);
        }
/*
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            int weatherForToday = weatherData[adapterPosition];
            mClickListener.onWeatherDataClick(weatherForToday);
        }*/
    }

    public void setWeatherData(String[] weatherDataFromJson) {
        weatherData = weatherDataFromJson;
        notifyDataSetChanged();
    }
}
