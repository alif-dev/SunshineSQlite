package com.example.alif.sunshinesqlite;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by alif on 27/03/18.
 */

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {
    String[] weatherData;
    OnWeatherListClickListener mClickListener;

    public WeatherAdapter(OnWeatherListClickListener weatherListClickListener) {
        mClickListener = weatherListClickListener;
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
        holder.mWeatherDataTextView.setText(weatherData[position]);
    }

    @Override
    public int getItemCount() {
        if (weatherData == null) {
            return 0;
        }
        return weatherData.length;
    }

    class WeatherViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        TextView mWeatherDataTextView;
        FrameLayout mRootLayout;

        public WeatherViewHolder(View itemView) {
            super(itemView);
            mWeatherDataTextView = itemView.findViewById(R.id.tv_weather_data);
            mRootLayout = itemView.findViewById(R.id.weather_list_root_layout);
            mRootLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String weatherForToday = weatherData[adapterPosition];
            mClickListener.onWeatherDataClick(weatherForToday);
        }
    }

    public void setWeatherData(String[] weatherDataFromJson) {
        weatherData = weatherDataFromJson;
        notifyDataSetChanged();
    }
}
