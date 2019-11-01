package com.app.hotgo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.maps.android.ui.IconGenerator;
import com.app.hotgo.R;
import com.app.hotgo.utility.AppUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * This utility class contains all image operations
 *
 * @author |dmb TEAM|
 */
public class ImageUtil {
    // 16:9
    // public static final double aspectRation = 1.77;
    // 4:3
    /**
     * Constant for the aspect ratio.
     */
    public static final double aspectRationSlider = 1.77;
    public static final double aspectRationThumb = 1.5;

    public static int widthForSlider;
    public static int widthForThumbs;

    /**
     * Constant for the maximum size of image thumbnails.
     */
    public static final int MAX_IMAGE_SIZE_THUMNAILS = 150;
    public static final int MAX_IMAGE_SIZE_MARKER_ICON = 100;

    /**
     * Creates Bitmap image by given URL.
     *
     * @param url url of the image
     * @return <code>Bitmap</code> object with the image
     */
    public static Bitmap createBitmapFromUrl(String url) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream((InputStream) new URL(url)
                    .getContent());
        } catch (MalformedURLException e) {
            //
        } catch (IOException e) {
            //
        }

        return bitmap;
    }

    /**
     * Creates thumbnail bitmap from given URL
     *
     * @param url <code>String</code> object containing the URL
     * @return <code>Bitmap</code> object
     */
    public static Bitmap createThumbBitmapFromUrl(String url) {

        InputStream stream = null;
        try {
            stream = (InputStream) new URL(url).getContent();
        } catch (MalformedURLException e) {
            //
        } catch (IOException e) {
            //
        }

        Bitmap bitmap = null;

        bitmap = BitmapFactory.decodeStream(stream);

        double newBitmapHeight = (double) MAX_IMAGE_SIZE_THUMNAILS
                / aspectRationSlider;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = MAX_IMAGE_SIZE_THUMNAILS;
        options.outHeight = (int) newBitmapHeight;

        bitmap = Bitmap.createScaledBitmap(bitmap, MAX_IMAGE_SIZE_THUMNAILS,
                (int) newBitmapHeight, false);

        return bitmap;
    }

    /**
     * Fixes the bitmap size for specific screens.
     *
     * @param stream      input stream to fetch the bitmap
     * @param screenWidth widh of the screen
     * @return <code>Bitmap</code> object with the image with fixed size
     */
    public static Bitmap fixBitmapForSpecificScreen(InputStream stream,
                                                    Integer screenWidth) {

        Bitmap bitmap = null;

        bitmap = BitmapFactory.decodeStream(stream);

        double newBitmapHeight = (double) screenWidth / aspectRationSlider;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = screenWidth;
        options.outHeight = (int) newBitmapHeight;

        bitmap = Bitmap.createScaledBitmap(bitmap, screenWidth,
                (int) newBitmapHeight, false);

        return bitmap;
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public final static Bitmap getResizedBitmap(Bitmap bm, int newHeight,
                                                int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        Log.d("BITMAP", width + "-" + height + "");
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);
        // RECREATE THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;

    }

    public final static Bitmap getRotateBitmap(Bitmap bm, int rotate) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        Matrix matrix = new Matrix();
        if (height < width)
            matrix.postRotate(rotate);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, true);

        return resizedBitmap;

    }

    public static String convertImageSize(String url, int imageSize) {
        String newUrl = "";
        if (url != null) {
            if (url.length() > 3) {
                newUrl = url.substring(0, url.length() - 3);
                newUrl += imageSize;
            }
        }
        return newUrl;

    }

    public static String convertGalleryImageSize(String url, int imageSize) {
        String newUrl = "";
        if (url != null) {
            newUrl = url.substring(0, url.indexOf("?"));
            newUrl += "?width=" + imageSize + "&height=" + imageSize;
        }
        return newUrl;

    }

    public static String addImageSize(String url, int imageSize) {
        String newUrl = url;
        if (url != null) {
            newUrl += "?width=" + imageSize;
        }
        return newUrl;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int getSizeBaseOnDensity(Context context, int size) {//size of MDPI
        int currentsize = size;
        int density = context.getResources().getDisplayMetrics().densityDpi;
        //Log.d("LUAN", ""+density);
        switch (density) {
            case DisplayMetrics.DENSITY_LOW:
//                Toast.makeText(context, "LDPI", Toast.LENGTH_SHORT).show();
                currentsize = (int) (size * 0.75);
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
//                Toast.makeText(context, "MDPI", Toast.LENGTH_SHORT).show();
                currentsize = (int) size * 1;
                break;
            case DisplayMetrics.DENSITY_HIGH:
//                Toast.makeText(context, "HDPI", Toast.LENGTH_SHORT).show();
                currentsize = (int) (size * 1.5);
                break;
            case DisplayMetrics.DENSITY_XHIGH:
//                Toast.makeText(context, "XHDPI", Toast.LENGTH_SHORT).show();
                currentsize = (int) (size * 2);
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
//                Toast.makeText(context, "XXHDPI", Toast.LENGTH_SHORT).show();
                currentsize = (int) (size * 3);
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
//                Toast.makeText(context, "XXXHDPI", Toast.LENGTH_SHORT).show();
                currentsize = (int) (size * 4);
                break;
            case DisplayMetrics.DENSITY_560:
//                Toast.makeText(context, "XXXHDPI", Toast.LENGTH_SHORT).show();
                currentsize = (int) (size * 3);
                break;
            default:
                currentsize = (int) (size * 1.5);
                break;
        }
        return currentsize;
    }

    public static File saveBitmap(Context context, Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        String rootFolder = Environment.getExternalStorageDirectory() + "/"
                + context.getString(R.string.app_name) + "/images/";

        File folder = new File(rootFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String filePath = rootFolder + "/" + SystemClock.currentThreadTimeMillis() + "image.jpg";
        File f = new File(filePath);
        if (f.exists())
            f.delete();
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }

    public static Bitmap getMarkerIconWithLabel(Context mContext, String label, int color, float angle, Bitmap bitmap) {
        IconGenerator iconGenerator = new IconGenerator(mContext);
        View markerView = LayoutInflater.from(mContext).inflate(R.layout.lay_marker, null);
        ImageView imgMarker = markerView.findViewById(R.id.img_marker);
        TextView tvLabel = markerView.findViewById(R.id.tv_label);
        imgMarker.getLayoutParams().width = AppUtil.convertDpToPixel(mContext, 28);
        imgMarker.getLayoutParams().height = AppUtil.convertDpToPixel(mContext, 30);
        imgMarker.setImageBitmap(bitmap);
        imgMarker.setRotation(angle);
        tvLabel.setText(label);
        tvLabel.setTextColor(color);
        tvLabel.setShadowLayer(2, 1, 1, Color.WHITE);
        iconGenerator.setContentView(markerView);
        iconGenerator.setBackground(null);
        return iconGenerator.makeIcon(label);
    }
}

