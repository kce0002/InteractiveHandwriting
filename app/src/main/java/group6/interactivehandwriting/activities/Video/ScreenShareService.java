package group6.interactivehandwriting.activities.Video;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ScreenShareService extends Service {
    public ScreenShareService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
