package group6.interactivehandwriting.activities.Video;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import group6.interactivehandwriting.common.network.NetworkLayer;

public class ScreenShareService extends Service {

    NetworkLayer networkLayer;

    public ScreenShareService(NetworkLayer networkLayer) {
        this.networkLayer = networkLayer;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
