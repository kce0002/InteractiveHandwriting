package group6.interactivehandwriting.activities.Room.DrawingBoard;

import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.sliders.AlphaSlideBar;
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar;

import java.util.Random;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.activities.Room.RoomViewActionUtility;
import group6.interactivehandwriting.activities.Room.views.DocumentView;
import group6.interactivehandwriting.activities.Room.views.RoomView;
import group6.interactivehandwriting.common.network.NetworkLayer;

public class DrawingBoardActivity extends Fragment {
    private ConstraintLayout roomLayout;
    private RoomView roomView;
    private DocumentView documentView;
    private SeekBar seekbar;
    private ColorPickerView color_picker_view;
    private View dbView;

    private boolean resizeToggle;

    private NetworkLayer networkLayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbView = inflater.inflate(R.layout.room_layout, container, false);


        roomLayout = dbView.findViewById(R.id.roomView_layoutDraw);
        roomLayout.setBackgroundColor(Color.WHITE);

        roomView = new RoomView(dbView.getContext());

        roomLayout.addView(roomView);
        roomLayout.bringChildToFront(roomView);

        documentView = dbView.findViewById(R.id.documentView);

        roomView.setDocumentView(documentView);


        seekbar = dbView.findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);

        color_picker_view = dbView.findViewById(R.id.colorPickerLayout);
        AlphaSlideBar alphaSlideBar = dbView.findViewById(R.id.alphaSlideBar);
        BrightnessSlideBar brightnessSlideBar = dbView.findViewById(R.id.brightnessSlide);

        resizeToggle = dbView.findViewById(R.id.adjViewBtn).isActivated();

        // Add alpha and brightness sliders
        color_picker_view.attachAlphaSlider(alphaSlideBar);
        color_picker_view.attachBrightnessSlider(brightnessSlideBar);

        // Create listener for changing color
        color_picker_view.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
                RoomViewActionUtility.ChangeColorHex(envelope.getHexCode());
            }
        });

        set_initial_color();

        return dbView;
    }

    public void setNetworkLayer(NetworkLayer networkLayer) {
        this.networkLayer = networkLayer;
        roomView.setNetworkLayer(networkLayer);
    }

    private void set_initial_color() {
        // Wait for color_picker_view to load to get width and height
        color_picker_view.post(new Runnable() {
                                   public void run() {

                                       int width = color_picker_view.getWidth();
                                       int radius = width / 2;
                                       int center_y = color_picker_view.getHeight() / 2;

                                       // Makes sure the initial color is not too light to see
                                       int min_dist_from_center = radius / 2;

                                       // Generate random angle and distance from center of color wheel
                                       double rand_angle = new Random().nextDouble() * Math.PI*2;
                                       double rand_dist = new Random().nextInt(radius - min_dist_from_center) + min_dist_from_center;

                                       // Use random values to set initial color
                                       int rand_x =(int)(Math.cos(rand_angle) * rand_dist) + radius;
                                       int rand_y =(int)(Math.sin(rand_angle) * rand_dist) + center_y;
                                       color_picker_view.setSelectorPoint(rand_x, rand_y);
                                   }
                               }
        );
    }


    // Used for the SeekBar to change pen width
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int seekbar_progress, boolean fromUser) {
            RoomViewActionUtility.ChangeWidth((float)seekbar_progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            RoomViewActionUtility.ChangeWidth((float)seekbar.getProgress());
        }
    };


    public void toggleToolbox() {
        ConstraintLayout toolboxLayout = dbView.findViewById(R.id.toolbox_view);

        if (toolboxLayout.getVisibility() == View.VISIBLE) {
            toolboxLayout.setVisibility(View.GONE);
        }
        else {
            toolboxLayout.setVisibility(View.VISIBLE);
        }
    }

    public void undo() {
        roomView.undo();
    }

    public void toggleColorPickerView() {

        ConstraintLayout colorPickerLayout = dbView.findViewById(R.id.color_picker_view);
        ConstraintLayout toolboxLayout = dbView.findViewById(R.id.toolbox_view);

        if (colorPickerLayout.getVisibility() == View.VISIBLE) {
            colorPickerLayout.setVisibility(View.GONE);
            toolboxLayout.setVisibility(View.VISIBLE);
        }
        else {
            colorPickerLayout.setVisibility(View.VISIBLE);
            toolboxLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void colorErase() {
        RoomViewActionUtility.setEraser();
        dbView.setPressed(true);
    }

    public void toggleDraw() {
        if (roomView.getTouchState() == roomView.getDrawState()) {
            roomView.setTouchState(roomView.getResizeState());
        }
        else {
            roomView.setTouchState(roomView.getDrawState());
        }
    }
}
