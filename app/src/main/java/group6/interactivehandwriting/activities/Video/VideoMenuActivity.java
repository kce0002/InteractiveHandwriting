package group6.interactivehandwriting.activities.Video;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.common.app.Permissions;
import group6.interactivehandwriting.common.network.NetworkLayer;
import group6.interactivehandwriting.common.network.NetworkLayerBinder;
import group6.interactivehandwriting.common.network.NetworkLayerService;
import group6.interactivehandwriting.common.network.nearby.connections.message.NetworkMessageType;

import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class VideoMenuActivity extends AppCompatActivity {
    NetworkLayer networkLayer;
    ServiceConnection networkServiceConnection;

    MediaProjectionManager mediaProjectionManager;
    MediaProjection mediaProjection;

    static final int REQUEST_CODE_SCREEN_RECORDING = 1;

    static Intent screenShareIntent;

    ToggleButton screenShareBtn;
    Button streamBtn;
    Button viewStreamBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_menu_layout);
        networkServiceConnection = getNetworkServiceConnection();
        screenShareIntent = new Intent(this, ScreenShareService.class);

        screenShareBtn = findViewById(R.id.screenShare);
        streamBtn = findViewById(R.id.startStream);
        viewStreamBtn = findViewById(R.id.joinStream);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Permissions.requestPermissions(this);
        NetworkLayerService.startNetworkService(this);
        NetworkLayerService.bindNetworkService(this, networkServiceConnection);
        setButtons();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(networkServiceConnection);
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
        mediaProjectionManager = (MediaProjectionManager)getApplicationContext().getSystemService(MEDIA_PROJECTION_SERVICE);
        ToggleButton screenShareBtn = findViewById(R.id.screenShare);
        if (screenShareBtn.isChecked()) {
            this.startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_SCREEN_RECORDING);
        }
        else {
            stopScreenShare(view);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                ScreenShareService.mediaProjection = mediaProjection;
                startForegroundService(screenShareIntent);
                networkLayer.sendBytes(new byte[] {}, NetworkMessageType.STREAM_STARTED);
                ScreenShareService.setCurStartTime(java.lang.System.currentTimeMillis());
                Toast.makeText(VideoMenuActivity.this, "Screen share starting", Toast.LENGTH_LONG).show();

                findViewById(R.id.startStream).setEnabled(false);
                findViewById(R.id.joinStream).setEnabled(false);
                ScreenShareService.isStreaming = true;
            }
            else if (Activity.RESULT_CANCELED == resultCode) {
                ToggleButton screenShareBtn = findViewById(R.id.screenShare);
                screenShareBtn.setChecked(false);
            }
        }
    }

    public void stopScreenShare(View view) {
        stopService(screenShareIntent);
        networkLayer.sendBytes(new byte[] {}, NetworkMessageType.STREAM_ENDED);
        Toast.makeText(VideoMenuActivity.this, "Screen share ending", Toast.LENGTH_LONG).show();
        findViewById(R.id.startStream).setEnabled(true);
        findViewById(R.id.joinStream).setEnabled(true);
        ScreenShareService.isStreaming = false;
    }

    private ServiceConnection getNetworkServiceConnection() {
        return new ServiceConnection()
        {
            @Override
            public void onServiceConnected (ComponentName name, IBinder service){
                NetworkLayerBinder binder = (NetworkLayerBinder) service;
                networkLayer = binder.getNetworkLayer();
                ScreenShareService.networkLayer = networkLayer;
                networkLayer.setVideoMenuActivity(VideoMenuActivity.this);
            }

            @Override
            public void onServiceDisconnected (ComponentName name) {
            }
        };
    }

    public void setButtons() {
        if (ScreenShareService.otherUserStreaming) {
            screenShareBtn.setChecked(false);
            screenShareBtn.setEnabled(false);
            streamBtn.setEnabled(false);
            viewStreamBtn.setEnabled(true);
        }
        else if (ScreenShareService.isStreaming) {
            screenShareBtn.setChecked(true);
            streamBtn.setEnabled(false);
            viewStreamBtn.setEnabled(false);
        }
        else {
            screenShareBtn.setEnabled(true);
            screenShareBtn.setChecked(false);
            streamBtn.setEnabled(true);
            viewStreamBtn.setEnabled(true);
        }
    }
}
