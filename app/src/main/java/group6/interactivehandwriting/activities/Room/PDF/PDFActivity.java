package group6.interactivehandwriting.activities.Room.PDF;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.File;
import java.io.IOException;
import java.util.Random;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.activities.Room.RoomViewActionUtility;
import group6.interactivehandwriting.activities.Room.views.DocumentView;
import group6.interactivehandwriting.activities.Room.views.RoomView;
import group6.interactivehandwriting.common.app.Permissions;
import group6.interactivehandwriting.common.network.NetworkLayer;

public class PDFActivity extends Fragment {
    private Context context;
    private DocumentView documentView;
    private NetworkLayer networkLayer;
    private SeekBar seekbar;
    private ColorPickerView color_picker_view;
    private View pdfView;
    private RoomView roomView;
    private ConstraintLayout roomLayout;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pdfView = inflater.inflate(R.layout.pdf_tab_layout, container, false);

        context = pdfView.getContext();

        roomLayout = pdfView.findViewById(R.id.roomView_layout);
        roomLayout.setBackgroundColor(Color.WHITE);

        roomView = new RoomView(context);

        System.out.println("~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~");
        System.out.println("onCreateView for PDF Activity has been called");
        System.out.println("~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~");
        System.out.println("~~~~~~~~~~~~~~~~");

        roomLayout.addView(roomView);
        roomLayout.bringChildToFront(roomView);

        documentView = pdfView.findViewById(R.id.documentView);
        roomView.setDocumentView(documentView);

        seekbar = pdfView.findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(seekBarChangeListener);

        color_picker_view = pdfView.findViewById(R.id.colorPickerLayout);
        AlphaSlideBar alphaSlideBar = pdfView.findViewById(R.id.alphaSlideBar);
        BrightnessSlideBar brightnessSlideBar = pdfView.findViewById(R.id.brightnessSlide);


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

        return pdfView;
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

    public void showPDF(File file, Context context) {
        try {
            PdfiumCore pdfiumCore = new PdfiumCore(context);

            ParcelFileDescriptor fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfDocument pdfDocument = pdfiumCore.newDocument(fd);

            //Get current screen size
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int screen_width = metrics.widthPixels;
            int screen_height = metrics.heightPixels;

            int pageCount = pdfiumCore.getPageCount(pdfDocument);

            // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
            // RGB_565 - little worse quality, twice less memory usage

            Bitmap bitmapArr[] = new Bitmap[pageCount];

            for (int pageNum = 0; pageNum < pageCount; pageNum++) {
                Bitmap bitmap = Bitmap.createBitmap(screen_width, screen_height, Bitmap.Config.ARGB_8888);
                pdfiumCore.openPage(pdfDocument, pageNum);
                pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, screen_width, screen_height, true);
                bitmapArr[pageNum] = bitmap;
            }

            documentView.setPDF(bitmapArr);

            if (networkLayer != null) {
                networkLayer.sendFile(fd);
            }

            pdfiumCore.closeDocument(pdfDocument);

        } catch(IOException ex) {
            Toast.makeText(context, "File corrupted", Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    public void incPDFPage() {
        documentView.incPDFPage();
    }

    public void decPDFPage() {
        documentView.decPDFPage();
    }

    public void toggleToolbox() {
        ConstraintLayout toolboxLayout = pdfView.findViewById(R.id.toolbox_view);

        if (toolboxLayout.getVisibility() == View.VISIBLE) {
            toolboxLayout.setVisibility(View.GONE);
        }
        else {
            toolboxLayout.setVisibility(View.VISIBLE);
        }
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

    public void undo() {
        roomView.undo();
    }

    public void colorErase() {
        RoomViewActionUtility.setEraser();
        pdfView.setPressed(true);
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
