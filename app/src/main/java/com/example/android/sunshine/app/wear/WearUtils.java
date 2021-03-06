package com.example.android.sunshine.app.wear;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.example.sunhine.lib.DataLayer;
/**
 * Created by Nicolas on 2015-10-23.
 */
public class WearUtils {

    private static final String[] WEATHER_PROJECTION = new String[] {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;


    static public void sendWeatherToWearable(Context context, GoogleApiClient googleApiClient, boolean addTimestamp )
    {
        String locationQuery = Utility.getPreferredLocation(context);

        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());

        // we'll query our contentProvider, as always
        Cursor cursor = context.getContentResolver().query(weatherUri, WEATHER_PROJECTION, null, null, null);

        if (cursor.moveToFirst()) {
            int weatherId = cursor.getInt(INDEX_WEATHER_ID);
            double high = cursor.getDouble(INDEX_MAX_TEMP);
            double low = cursor.getDouble(INDEX_MIN_TEMP);
            String desc = cursor.getString(INDEX_SHORT_DESC);

            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataLayer.PATH_WEATHER);
            putDataMapRequest.getDataMap().putInt(DataLayer.KEY_WEATHER_ID, weatherId);
            putDataMapRequest.getDataMap().putString(DataLayer.KEY_MAX_TEMP, Utility.formatTemperature(context,high));
            putDataMapRequest.getDataMap().putString(DataLayer.KEY_MIN_TEMP, Utility.formatTemperature(context,low));
            putDataMapRequest.getDataMap().putString(DataLayer.KEY_SHORT_DESC, desc);

            if(addTimestamp)
            {
                putDataMapRequest.getDataMap().putLong(DataLayer.KEY_TIMESTAMP, System.currentTimeMillis());
            }

            PutDataRequest request = putDataMapRequest.asPutDataRequest();


            if (googleApiClient.isConnected()) {
                Wearable.DataApi.putDataItem(googleApiClient, request);
            }
            else
            {
                Log.w("WearUtils","sendWeatherToWearable google api client is not connected");
            }

        }

        cursor.close();
    }

    static public Bitmap createGrayBitmap(  Bitmap bm ) {
        Bitmap grayBitmap = Bitmap.createBitmap(
                bm.getWidth(),
                bm.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grayBitmap);
        Paint grayPaint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        grayPaint.setColorFilter(filter);
        canvas.drawBitmap(bm, 0, 0, grayPaint);

        canvas.save();

        return grayBitmap;
    }



}
