package group6.interactivehandwriting.activities.Video;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
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

import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;

import java.io.IOException;

public class VideoMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_menu_layout);
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
        Activity a = VideoMenuActivity.this;

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        MediaRecorder mr = new MediaRecorder();
        mr.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mr.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mr.setVideoSize(dm.widthPixels, dm.heightPixels);
        mr.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mr.setVideoFrameRate(30);

        try {
            mr.prepare();
        } catch(IOException e) {
            e.printStackTrace();
        }
        mr.start();

        MediaProjectionManager mpm = (MediaProjectionManager) a.getSystemService(Context.MEDIA_PROJECTION_SERVICE);



    }
}
