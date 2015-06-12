package org.zywx.wbpalmstar.plugin.uexzxing;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import com.ace.zxing.BinaryBitmap;
import com.ace.zxing.RGBLuminanceSource;
import com.ace.zxing.common.HybridBinarizer;

import java.io.IOException;

public class ScannerUtils {
	
	public static DisplayMetrics getDisplayMetrics(Context context){
		DisplayMetrics dism = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dism);
		return dism;
	}

    public static String getRealPath(Context context, Uri uri){
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().query(uri, filePathColumn,
                null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }
    public static Bitmap formatBitmap(Context context, int target, Uri uri) {

        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = context.getContentResolver().query(uri, filePathColumn,
                null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath, options);

        int width = options.outWidth, height = options.outHeight;
        int scale = 1;
        int temp = width > height ? width : height;
        while (true) {
            if (temp / 2 < target)
                break;
            temp = temp / 2;
            scale *= 2;
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = scale;
        opt.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opt);

        int orientation = readPictureDegree(picturePath);
        if (Math.abs(orientation) > 0) {
            bitmap = rotateBitmap(orientation, bitmap);
        }
        return bitmap;
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static BinaryBitmap loadImage(Bitmap bitmap) throws IOException {
        int lWidth = bitmap.getWidth();
        int lHeight = bitmap.getHeight();
        int[] lPixels = new int[lWidth * lHeight];
        bitmap.getPixels(lPixels, 0, lWidth, 0, 0, lWidth, lHeight);
        return new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(
                lWidth, lHeight, lPixels)));
    }
}
