package org.zywx.wbpalmstar.plugin.uexzxing;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.plugin.uexzxing.common.HybridBinarizer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

public class EScannerUtils {

	public static final String SCANER_PARAMS_CODE_LINEIMG = "lineImg";
	public static final String SCANER_PARAMS_CODE_PICKBGIMG = "pickBgImg";
	public static final String SCANER_PARAMS_CODE_TIPLABEL = "tipLabel";
	public static final String SCANER_PARAMS_CODE_TITLE = "title";
	public static final String SCANER_PARAMS_CODE_OBJ = "obj";

	public static BinaryBitmap loadImage(Bitmap bitmap) throws IOException {
		int lWidth = bitmap.getWidth();
		int lHeight = bitmap.getHeight();
		int[] lPixels = new int[lWidth * lHeight];
		bitmap.getPixels(lPixels, 0, lWidth, 0, 0, lWidth, lHeight);
		return new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(
				lWidth, lHeight, lPixels)));
	}

	public static Bitmap formatBitmap(Context context, int target, Uri uri) {

		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = context.getContentResolver().query(uri, filePathColumn,
				null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();

		Options options = new Options();
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

		Options opt = new Options();
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
		;
		matrix.postRotate(angle);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	public static Bitmap getImage(Context ctx, String imgUrl) {
		if (imgUrl == null || imgUrl.length() == 0) {
			return null;
		}
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			if (imgUrl.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
				is = BUtility.getInputStreamByResPath(ctx, imgUrl);
				bitmap = BitmapFactory.decodeStream(is);
			} else if (imgUrl.startsWith(BUtility.F_FILE_SCHEMA)) {
				imgUrl = imgUrl.replace(BUtility.F_FILE_SCHEMA, "");
				bitmap = BitmapFactory.decodeFile(imgUrl);
			} else if (imgUrl.startsWith(BUtility.F_Widget_RES_path)) {
				try {
					is = ctx.getAssets().open(imgUrl);
					if (is != null) {
						bitmap = BitmapFactory.decodeStream(is);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				bitmap = BitmapFactory.decodeFile(imgUrl);
			}
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	public static EScanStyleModel parse2Model(String string) {
		EScanStyleModel sModel = null;
		try {
			JSONObject obj = new JSONObject(string);
			sModel = new EScanStyleModel();
			if (obj.has(SCANER_PARAMS_CODE_LINEIMG)) {
				sModel.setLineImg(obj.getString(SCANER_PARAMS_CODE_LINEIMG));
			}
			if (obj.has(SCANER_PARAMS_CODE_PICKBGIMG)) {
				sModel.setPickBgImg(obj.getString(SCANER_PARAMS_CODE_PICKBGIMG));
			}
			if (obj.has(SCANER_PARAMS_CODE_TIPLABEL)) {
				sModel.setTipLabel(obj.getString(SCANER_PARAMS_CODE_TIPLABEL));
			}
			if (obj.has(SCANER_PARAMS_CODE_TITLE)) {
				sModel.setTitle(obj.getString(SCANER_PARAMS_CODE_TITLE));
			}
		} catch (JSONException e) {
			e.printStackTrace();
			sModel = null;
		}
		return sModel;
	}

}
