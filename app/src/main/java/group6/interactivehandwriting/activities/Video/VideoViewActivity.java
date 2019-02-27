package group6.interactivehandwriting.activities.Video;

import android.content.ComponentName;
import android.content.ServiceConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.common.app.Permissions;
import group6.interactivehandwriting.common.network.NetworkLayer;
import group6.interactivehandwriting.common.network.NetworkLayerBinder;
import group6.interactivehandwriting.common.network.NetworkLayerService;
import group6.interactivehandwriting.common.network.nearby.connections.message.serial.SerialMessageHeader;


public class VideoViewActivity extends AppCompatActivity {

    NetworkLayer networkLayer;
    ServiceConnection networkServiceConnection;
    public ImageView imageView;
    ArrayList<Byte> byteArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_view_layout);

        imageView = findViewById(R.id.imageView);

        networkServiceConnection = getNetworkServiceConnection();
        byteArrayList = new ArrayList<>();
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

    public void showVideo(SerialMessageHeader header, byte[] frameBytes, String username) {

        this.setTitle(username + "'s Stream");
        if (header.getBigData() == (byte) 0) {
            Bitmap bmp = BitmapFactory.decodeByteArray(frameBytes, 0, frameBytes.length);
            imageView.setImageBitmap(bmp);
        }
        else if (header.getBigData() == (byte) 1) {
            for (int i = 0; i < frameBytes.length; i++) {
                byteArrayList.add(frameBytes[i]);
            }
        }
        else if (header.getBigData() == (byte) 2) {
            for (int i = 0; i < frameBytes.length; i++) {
                byteArrayList.add(frameBytes[i]);
            }
            byte[] b = new byte[byteArrayList.size()];
            for (int i = 0; i < b.length; i++) {
                b[i] = byteArrayList.get(i);
            }
            Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
            imageView.setImageBitmap(bmp);
            byteArrayList.clear();
        }

    }

}