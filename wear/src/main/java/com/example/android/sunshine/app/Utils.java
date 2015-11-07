package com.example.android.sunshine.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.net.Uri;
import android.util.Log;

import com.example.sunhine.lib.DataLayer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Nicolas on 2015-10-26.
 */
public class Utils {
    static public final String TAG = Utils.class.getSimpleName();
    /**
     * Send an emtpy message to all connected nodes
     * @param googleApiClient
     * @param path
     */
    public static void sendSimpleMessage( final GoogleApiClient googleApiClient,  final String path  ) {
        // Run this on a thread!
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( googleApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            googleApiClient, node.getId(), path, null).await();
                }
            }
        }).start();
    }

    interface GetDataItemCallback
    {
        void onResult(DataMap data);
    }

    public static void getDataItemForLocalNode( final GoogleApiClient googleApiClient,
                                    final String path,
                                    final GetDataItemCallback callback)
    {
        final ResultCallback<DataApi.DataItemResult> dataApiResultCallback = new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult.getStatus().isSuccess()) {

                    Log.d(TAG, "getDataItemForLocalNode : received dataItemResult");

                    if (dataItemResult.getDataItem() != null) {
                        DataItem configDataItem = dataItemResult.getDataItem();
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                        DataMap config = dataMapItem.getDataMap();
                        callback.onResult(config);
                    }
                    else
                    {
                        Log.d(TAG,"getDataItemForLocalNode received null");
                    }
                }
            }
        };


        ResultCallback<NodeApi.GetLocalNodeResult> nodeResultCallback = new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(NodeApi.GetLocalNodeResult result) {
                Log.d(TAG,"getDataItemForLocalNode : received node results");
                String localNode = result.getNode().getId();
                Uri uri = new Uri.Builder()
                        .scheme("wear")
                        .path(path)
                        .authority(localNode)
                        .build();
                Log.d(TAG,"getDataItemForLocalNode uri = " + uri);
                Wearable.DataApi.getDataItem(googleApiClient, uri)
                        .setResultCallback(dataApiResultCallback);
            }
        };


        Wearable.NodeApi.getLocalNode(googleApiClient).setResultCallback(nodeResultCallback);
    }

    public static int getIconResourceForWeatherCondition(int weatherId, boolean ambiantMode) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return !ambiantMode ? R.drawable.ic_storm : R.drawable.ic_storm_ambiant;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return !ambiantMode ? R.drawable.ic_light_rain :  R.drawable.ic_light_rain_ambiant;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return !ambiantMode ? R.drawable.ic_rain : R.drawable.ic_rain_ambiant;
        } else if (weatherId == 511) {
            return !ambiantMode ? R.drawable.ic_snow : R.drawable.ic_snow_ambiant;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return !ambiantMode ? R.drawable.ic_rain : R.drawable.ic_rain_ambiant;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return !ambiantMode ? R.drawable.ic_snow : R.drawable.ic_snow_ambiant;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return !ambiantMode ? R.drawable.ic_fog : R.drawable.ic_fog_ambiant;
        } else if (weatherId == 761 || weatherId == 781) {
            return !ambiantMode ? R.drawable.ic_storm : R.drawable.ic_storm_ambiant;
        } else if (weatherId == 800) {
            return !ambiantMode ? R.drawable.ic_clear : R.drawable.ic_clear_ambiant;
        } else if (weatherId == 801) {
            return !ambiantMode ? R.drawable.ic_light_clouds : R.drawable.ic_light_clouds_ambiant;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return !ambiantMode ? R.drawable.ic_cloudy : R.drawable.ic_cloudy_ambiant;
        }

        return -1;
    }

    static public Bitmap createGrayScaleBitmap(Bitmap colorBitmap) {
        Bitmap grayBitmap = Bitmap.createBitmap(
                colorBitmap.getWidth(),
                colorBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(colorBitmap);
        Paint grayPaint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
        grayPaint.setColorFilter(filter);
        canvas.drawBitmap(colorBitmap, 0, 0, grayPaint);

        return grayBitmap;
    }
}
