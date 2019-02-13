package group6.interactivehandwriting.activities.Video;

import android.Manifest;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.activities.Room.RoomActivity;

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
}
