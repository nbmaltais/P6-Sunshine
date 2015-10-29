package com.example.android.sunshine.app;

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

    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }
}
