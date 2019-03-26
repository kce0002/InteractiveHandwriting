package group6.interactivehandwriting.activities.Room;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import group6.interactivehandwriting.R;

import android.widget.SeekBar;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.sliders.AlphaSlideBar;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import group6.interactivehandwriting.activities.Room.DrawingBoard.DrawingBoardActivity;
import group6.interactivehandwriting.activities.Room.views.DocumentView;
import group6.interactivehandwriting.activities.Room.views.RoomView;
import group6.interactivehandwriting.activities.Video.VideoMenuActivity;
import group6.interactivehandwriting.common.app.Permissions;
import group6.interactivehandwriting.common.network.NetworkLayer;
import group6.interactivehandwriting.common.network.NetworkLayerBinder;
import group6.interactivehandwriting.common.network.NetworkLayerService;
import group6.interactivehandwriting.common.network.nearby.connections.NCNetworkConnection;
import group6.interactivehandwriting.common.network.nearby.connections.NCNetworkLayerService;
import group6.interactivehandwriting.activities.Room.PDF.PDFActivity;

public class RoomActivity extends AppCompatActivity {
    private RoomView roomView;
    private SeekBar seekbar;
    private ColorPickerView color_picker_view;
    private Context context;
    private DocumentView documentView;
    private ConstraintLayout roomLayout;
    private PDFActivity pdfActivity;

    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    NetworkLayer networkLayer;
    ServiceConnection networkServiceConnection;
    private NCNetworkConnection ncNetworkConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = getApplicationContext();
        getRoomName(savedInstanceState);

        setContentView(R.layout.tabs_layout);

        pdfActivity = new PDFActivity();

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new DrawingBoardActivity(), "Drawing Board");
        adapter.addFragment(new PDFActivity(), "PDF");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);


    }

    private void getRoomName(Bundle savedInstanceState) {
        String roomName;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                roomName = null;
            } else {
                roomName = extras.getString("ROOM_NAME");
            }
        } else {
            roomName = (String) savedInstanceState.getSerializable("ROOM_NAME");
        }
        if (roomName != null) {
            Toast.makeText(getApplicationContext(), "Joined " + roomName, Toast.LENGTH_LONG).show();
        } else {
            Log.e("RoomActivity", "room name was null");
        }
    }

    public void showDocument(View view) {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(Permissions.REQUEST_CODE_FILEPICKER)
                .withHiddenFiles(true)
                .start();
    }

    // Modified by Kyle Ehlers on 1/17/19
    // Added the try/catch to handle the NullPointerException
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Permissions.REQUEST_CODE_FILEPICKER) {
            try {
                String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                pdfActivity.showPDF(new File(filePath), context);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    }

    public void openStreamView(View view) {
        // Go to stream view to either start streaming or view streaming
        Intent video_activity = new Intent(this, VideoMenuActivity.class);
        RoomActivity.this.startActivity(video_activity);
    }


    public void toggleToolbox(View view) {
        ConstraintLayout toolboxLayout = findViewById(R.id.toolbox_view);

        if (toolboxLayout.getVisibility() == View.VISIBLE) {
            toolboxLayout.setVisibility(View.GONE);
        }
        else {
            toolboxLayout.setVisibility(View.VISIBLE);
        }
    }

    public void undo(View view) {
        roomView.undo();
    }

    public void incPDFPage(View view) {
        documentView.incPDFPage();
    }

    public void decPDFPage(View view) {
        documentView.decPDFPage();
    }

    public void toggleColorPickerView(View view) {

        ConstraintLayout colorPickerLayout = findViewById(R.id.color_picker_view);
        ConstraintLayout toolboxLayout = findViewById(R.id.toolbox_view);

        if (colorPickerLayout.getVisibility() == View.VISIBLE) {
            colorPickerLayout.setVisibility(View.GONE);
            toolboxLayout.setVisibility(View.VISIBLE);
        }
        else {
            colorPickerLayout.setVisibility(View.VISIBLE);
            toolboxLayout.setVisibility(View.INVISIBLE);
        }


    }

    public void changeColor(View view) {
        RoomViewActionUtility.ChangeColorHex(view.getTag().toString());
    }

    public void colorErase(View view) {
        RoomViewActionUtility.setEraser();
        view.setPressed(true);
    }

    public void saveCanvas(View view) {

    }

    public void toggleDraw(View view) {
        if (roomView.getTouchState() == roomView.getDrawState()) {
            roomView.setTouchState(roomView.getResizeState());
        }
        else {
            roomView.setTouchState(roomView.getDrawState());
        }
    }

    @Override
    public void onBackPressed() {
        ncNetworkConnection.stopAdvertising();
        ncNetworkConnection.discover();
        super.onBackPressed();
    }

}