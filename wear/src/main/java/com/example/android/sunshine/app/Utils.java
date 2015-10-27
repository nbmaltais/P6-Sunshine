package com.example.android.sunshine.app;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Nicolas on 2015-10-26.
 */
public class Utils {

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
}
