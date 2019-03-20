package group6.interactivehandwriting.activities.Video;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import group6.interactivehandwriting.common.network.NetworkLayer;
import group6.interactivehandwriting.common.network.nearby.connections.message.NetworkMessageType;

public class ScreenShareService extends Service {

    public static NetworkLayer networkLayer;
    public static MediaProjection mediaProjection;
    public static ImageReader imageReader;
    public static boolean isStreaming;
    public static boolean otherUserStreaming;


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        System.out.println("Screen Share Service Started");

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        Point size = new Point();
        display.getRealSize(size);
        final int width = size.x;
        final int height = size.y;
        int density = metrics.densityDpi;

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        final Handler handler = new Handler();

        int vdFlags = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
        //mediaProjection.createVirtualDisplay("screen-mirror", width, height, density, vdFlags, imageReader.getSurface(), null, handler);
        mediaProjection.createVirtualDisplay("screen-mirror", width, height, density, vdFlags, imageReader.getSurface(), null, null);
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                reader.setOnImageAvailableListener(this, handler);

//                Image image = imageReader.acquireLatestImage();
                Image image = imageReader.acquireNextImage();

                if (image == null) {
                    return;
                }

                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();

                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * metrics.widthPixels;

                // create bitmap
                Bitmap bmp = Bitmap.createBitmap(metrics.widthPixels + (int) ((float) rowPadding / (float) pixelStride), metrics.heightPixels, Bitmap.Config.ARGB_8888);
                bmp.copyPixelsFromBuffer(buffer);

                // network stuff:
                Bitmap realSizeBitmap = Bitmap.createBitmap(bmp, 0, 0, metrics.widthPixels, bmp.getHeight());

                ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                realSizeBitmap.compress(Bitmap.CompressFormat.JPEG, 15, bitmapStream);
                System.out.println("Sharing:  "  + bitmapStream.size());
                byte[] bitmapByteArray = bitmapStream.toByteArray();
                networkLayer.sendBytes(bitmapByteArray, NetworkMessageType.VIDEO_STREAM);

                image.close();

                bmp.recycle();
                buffer.clear();

            }
        }, null);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        System.out.println("Screen Share Service Stopped");
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                return;
            }
        }, null);
        super.onDestroy();
    }


}
