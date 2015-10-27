package com.example.android.sunshine.app.wear;

import android.util.Log;

import com.example.sunhine.lib.DataLayer;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Nicolas on 2015-10-23.
 */
public class DataLayerListenerService extends WearableListenerService {
    private static final String TAG = DataLayerListenerService.class.getSimpleName();

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);

        Log.d(TAG,"Node connected: " + peer);

        // TODO: send the current weather to the wearable

        WearUtils.sendWeatherToWearable(this,mGoogleApiClient);

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        Log.d(TAG, "Message received, path = " + messageEvent.getPath());

        // The watch face sends this message when it is started.
        if( messageEvent.getPath().equals(DataLayer.PATH_REQUEST_WEATHER) )
        {
            WearUtils.sendWeatherToWearable(this,mGoogleApiClient);
        }

    }
}
