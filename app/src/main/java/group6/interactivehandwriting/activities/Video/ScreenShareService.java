package group6.interactivehandwriting.activities.Video;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import group6.interactivehandwriting.common.network.NetworkLayer;

public class ScreenShareService extends Service {

    public static NetworkLayer networkLayer;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        System.out.println("Screen Share Service Started");



        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        System.out.println("Screen Share Service Stopped");
    }


}
