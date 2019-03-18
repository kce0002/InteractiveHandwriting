package group6.interactivehandwriting.activities.Video;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.activities.Room.RoomActivity;
import group6.interactivehandwriting.common.app.Permissions;
import group6.interactivehandwriting.common.network.NetworkLayer;
import group6.interactivehandwriting.common.network.NetworkLayerBinder;
import group6.interactivehandwriting.common.network.NetworkLayerService;
import group6.interactivehandwriting.common.network.nearby.connections.message.NetworkMessageType;

import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VideoMenuActivity extends AppCompatActivity {
    NetworkLayer networkLayer;
    ServiceConnection networkServiceConnection;

    ScreenShareService screenShareService;
    private static boolean screenShare;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_menu_layout);
        screenShare = false;
        networkServiceConnection = getNetworkServiceConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permissions.requestPermissions(this);
        NetworkLayerService.startNetworkService(this);
        NetworkLayerService.bindNetworkService(this, networkServiceConnection);
    }

    public void startStream(View view) {
        Intent stream_activity = new Intent(this, VideoStreamActivity.class);
        VideoMenuActivity.this.startActivity(stream_activity);
    }

    public void joinStream(View view) {
        Intent view_activity = new Intent(this, VideoViewActivity.class);
        VideoMenuActivity.this.startActivity(view_activity);
    }

    public void screenShare(View view) {
        screenShare = true;


//        AsyncTask.execute(new Runnable() {
//            @Override
//            public void run() {
//                while (screenShare) {
//                    View v = getWindow().getDecorView().getRootView();
//                    v.setDrawingCacheEnabled(true);
//                    Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache());
//                    ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
//                    bmp.compress(Bitmap.CompressFormat.JPEG, 5, bitmapStream);
//                    System.out.println("Streaming "  + bitmapStream.size());
//                    byte[] bitmapByteArray = bitmapStream.toByteArray();
//
//                    networkLayer.sendBytes(bitmapByteArray, NetworkMessageType.VIDEO_STREAM);
//                    Button b = findViewById(R.id.startStream);
//                    b.setText("test");
//                }
//            }
//        });
    }

    public void stopScreenShare(View view) {
        screenShare = false;
    }

    private ServiceConnection getNetworkServiceConnection() {
        return new ServiceConnection()
        {
            @Override
            public void onServiceConnected (ComponentName name, IBinder service){
                NetworkLayerBinder binder = (NetworkLayerBinder) service;
                networkLayer = binder.getNetworkLayer();
                VideoMenuActivity.this.screenShareService = new ScreenShareService(networkLayer);
            }

            @Override
            public void onServiceDisconnected (ComponentName name) {
            }
        };
    }
}
