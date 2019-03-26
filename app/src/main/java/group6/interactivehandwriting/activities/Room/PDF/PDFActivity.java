package group6.interactivehandwriting.activities.Room.PDF;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;

import group6.interactivehandwriting.R;
import group6.interactivehandwriting.activities.Room.views.DocumentView;
import group6.interactivehandwriting.common.network.NetworkLayer;

public class PDFActivity extends Fragment {
    Context context;
    DocumentView documentView;
    NetworkLayer networkLayer;
    //@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pdf_tab_layout, container, false);
        documentView = view.findViewById(R.id.documentView);
        context = view.getContext();
        return view;
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




}
