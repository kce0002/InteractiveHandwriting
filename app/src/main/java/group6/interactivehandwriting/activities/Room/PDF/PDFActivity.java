package group6.interactivehandwriting.activities.Room.PDF;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.activities.Room.views.DocumentView;
import group6.interactivehandwriting.common.app.Permissions;
import group6.interactivehandwriting.common.network.NetworkLayer;

public class PDFActivity extends Fragment {
    private Context context;
    private DocumentView documentView;
    private NetworkLayer networkLayer;
    private View pdfView;
    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pdfView = inflater.inflate(R.layout.pdf_tab_layout, container, false);
        documentView = pdfView.findViewById(R.id.documentView);
        context = pdfView.getContext();
        return pdfView;
    }

    public DocumentView getDocumentView() {
        return this.documentView;
    }

    public void showDocument() {
        new MaterialFilePicker()
                .withActivity(this.getActivity())
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
                showPDF(new File(filePath), context);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
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

            pdfiumCore.closeDocument(pdfDocument); // important!


            getView().findViewById(R.id.decPageBtn).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.incPageBtn).setVisibility(View.VISIBLE);

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




}
