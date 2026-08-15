package com.deskmint.dashboard.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Lightweight offline-first weather fetcher. Uses Open-Meteo (no API key
 * required). If there is no network, the last cached reading is shown instead
 * so the dashboard never looks broken while offline.
 */
public class WeatherHelper {

    private static final String PREFS = "weather_cache";

    public interface Callback {
        void onWeather(double tempC, String condition, boolean fromCache);
    }

    public static void fetch(final Context context, final double lat, final double lon, final Callback callback) {
        if (!isOnline(context)) {
            loadFromCache(context, callback);
            return;
        }

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... voids) {
                try {
                    String urlStr = "https://api.open-meteo.com/v1/forecast?latitude=" + lat +
                            "&longitude=" + lon + "&current_weather=true";
                    URL url = new URL(urlStr);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(8000);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();
                    return new JSONObject(sb.toString());
                } catch (Exception e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(JSONObject result) {
                if (result == null) {
                    loadFromCache(context, callback);
                    return;
                }
                try {
                    JSONObject current = result.getJSONObject("current_weather");
                    double temp = current.getDouble("temperature");
                    int code = current.getInt("weathercode");
                    String condition = codeToCondition(code);

                    // cache for offline viewing later
                    SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
                    editor.putFloat("temp", (float) temp);
                    editor.putString("condition", condition);
                    editor.apply();

                    callback.onWeather(temp, condition, false);
                } catch (Exception e) {
                    loadFromCache(context, callback);
                }
            }
        }.execute();
    }

    private static void loadFromCache(Context context, Callback callback) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        float temp = prefs.getFloat("temp", Float.NaN);
        String condition = prefs.getString("condition", "Unknown");
        callback.onWeather(temp, condition, true);
    }

    private static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    private static String codeToCondition(int code) {
        if (code == 0) return "Clear";
        if (code <= 3) return "Cloudy";
        if (code <= 48) return "Fog";
        if (code <= 67) return "Rain";
        if (code <= 77) return "Snow";
        if (code <= 82) return "Showers";
        if (code <= 99) return "Storm";
        return "Unknown";
    }
}
