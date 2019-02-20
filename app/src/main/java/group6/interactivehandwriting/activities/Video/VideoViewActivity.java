package group6.interactivehandwriting.activities.Video;

import android.content.ComponentName;
import android.content.ServiceConnection;

import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.common.app.Permissions;
import group6.interactivehandwriting.common.network.NetworkLayer;
import group6.interactivehandwriting.common.network.NetworkLayerBinder;
import group6.interactivehandwriting.common.network.NetworkLayerService;


public class VideoViewActivity extends AppCompatActivity {

    NetworkLayer networkLayer;
    ServiceConnection networkServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view_layout);


        networkServiceConnection = getNetworkServiceConnection();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permissions.requestPermissions(this);
        NetworkLayerService.startNetworkService(this);
        NetworkLayerService.bindNetworkService(this, networkServiceConnection);
    }

    private ServiceConnection getNetworkServiceConnection() {
        return new ServiceConnection()
        {
            @Override
            public void onServiceConnected (ComponentName name, IBinder service){
                NetworkLayerBinder binder = (NetworkLayerBinder) service;
                networkLayer = binder.getNetworkLayer();
                handleNetworkStarted();
            }

            @Override
            public void onServiceDisconnected (ComponentName name) {
            }
        };
    }

    private void handleNetworkStarted() {
        networkLayer.setVideoViewActivity(this);
    }

    public void showVideo(InputStream inputStream) {
        try {
            while (inputStream.available() > 0) {
                int buffer = inputStream.read();
//                byte[] bytes = IOUtils.toByteArray(inputStream);
                System.out.println(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}