package group6.interactivehandwriting.activities.Video;

import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import group6.interactivehandwriting.R;

public class VideoStreamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_stream_layout);

        ActivityCompat.requestPermissions(VideoStreamActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
    }
}
