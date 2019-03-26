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
//    private RoomView roomView;
    private Context context;
//    private ConstraintLayout roomLayout;
    private PDFActivity pdfActivity;
    private DrawingBoardActivity drawingBoardActivity;

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

//        roomView = new RoomView(context);

        pdfActivity = new PDFActivity();
        drawingBoardActivity = new DrawingBoardActivity();

        viewPager = (ViewPagerNoScroll) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(drawingBoardActivity, "Drawing Board");
        adapter.addFragment(pdfActivity, "PDF");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        networkServiceConnection = getNetworkServiceConnection();

    }

    @Override
    protected void onStart() {
        super.onStart();
        NetworkLayerService.startNetworkService(this);
        NetworkLayerService.bindNetworkService(this, networkServiceConnection);
//        roomLayout.bringChildToFront(roomView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(networkServiceConnection);
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
            public void onServiceDisconnected (ComponentName name){

            }
        };
    }

    private void handleNetworkStarted() {
//        roomView.setNetworkLayer(networkLayer);
        networkLayer.setRoomActivity(this);
        networkLayer.setPDFActivity(pdfActivity);
        ncNetworkConnection = networkLayer.getNCNetworkConnection();
        ncNetworkConnection.stopDiscovering();
        ncNetworkConnection.advertise();
        pdfActivity.setNetworkLayer(networkLayer);
        drawingBoardActivity.setNetworkLayer(networkLayer);
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


    public void toggleToolboxPDF(View view) {
        pdfActivity.toggleToolbox();
    }

    public void toggleToolboxDrawingBoard(View view) {
        drawingBoardActivity.toggleToolbox();
    }

    public void undoPDF(View view) {
        pdfActivity.undo();
    }

    public void undoDrawingBoard(View view) {
        drawingBoardActivity.undo();
    }

    public void incPDFPage(View view) {
        pdfActivity.incPDFPage();
    }

    public void decPDFPage(View view) {
        pdfActivity.decPDFPage();
    }

    public void toggleColorPickerView(View view) {
        drawingBoardActivity.toggleColorPickerView();
    }

    public void changeColor(View view) {
        RoomViewActionUtility.ChangeColorHex(view.getTag().toString());
    }

    public void colorEraseDrawingBoard(View view) {
        drawingBoardActivity.colorErase();
    }

    public void colorErasePDF(View view) {
        pdfActivity.colorErase();
    }

    public void toggleDrawDrawingBoard(View view) {
        drawingBoardActivity.toggleDraw();
    }

    public void toggleDrawPDF(View view) {
        pdfActivity.toggleDraw();
    }

    @Override
    public void onBackPressed() {
        ncNetworkConnection.stopAdvertising();
        ncNetworkConnection.discover();
        super.onBackPressed();
    }

}