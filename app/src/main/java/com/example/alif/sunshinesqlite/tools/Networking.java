package com.example.alif.sunshinesqlite.tools;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class Networking {
    public static URL buildUrl(String location, String units) {
        Uri uri = Uri.parse("https://andfun-weather.udacity.com/weather").buildUpon()
                .appendQueryParameter("q", location)
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("units", units)
                .appendQueryParameter("cnt", String.valueOf(14))
                .build();
        URL url = null;
        try {
            url = new URL(uri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Log.v("URL = ", url.toString());
        return url;
    }

    public static String getUrlResponse(URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = httpURLConnection.getInputStream();
            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();

            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            httpURLConnection.disconnect();
        }
    }
}
