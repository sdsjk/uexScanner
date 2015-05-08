package org.zywx.wbpalmstar.plugin.uexzxing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.zywx.wbpalmstar.plugin.uexzxing.common.BitMatrix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class CreateBarCode {

	/**
	 * 生成条形码
	 * 
	 * @param context
	 * @param contents
	 *            需要生成的内容
	 * @param desiredWidth
	 *            生成条形码的宽带
	 * @param desiredHeight
	 *            生成条形码的高度
	 * @param displayCode
	 *            是否在条形码下方显示内容
	 * @return
	 */
	public Bitmap creatBarcode(Context context, String contents,
			int desiredWidth, int desiredHeight, boolean displayCode, int code) {
		Bitmap ruseltBitmap = null;
		/**
		 * 图片两端所保留的空白的宽度
		 */
		int marginW = 20;
		/**
		 * 条形码的编码类型
		 */
		BarcodeFormat barcodeFormat = formatBar(code);

		if (displayCode) {
			Bitmap barcodeBitmap = encodeAsBitmap(contents, barcodeFormat,
					desiredWidth, desiredHeight);
			Bitmap codeBitmap = creatCodeBitmap(contents, desiredWidth + 2
					* marginW, desiredHeight, context);
			ruseltBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(
					0, desiredHeight));
		} else {
			ruseltBitmap = encodeAsBitmap(contents, barcodeFormat,
					desiredWidth, desiredHeight);
		}

		return ruseltBitmap;
	}

	/**
	 * 生成条形码的Bitmap
	 * 
	 * @param contents
	 *            需要生成的内容
	 * @param format
	 *            编码格式
	 * @param desiredWidth
	 * @param desiredHeight
	 * @return
	 * @throws WriterException
	 */
	BitMatrix result = null;

	protected Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
			int desiredWidth, int desiredHeight) {
		final int WHITE = 0xFFFFFFFF;
		final int BLACK = 0xFF000000;

		MultiFormatWriter writer = new MultiFormatWriter();
		// BitMatrix result = null;
		try {
			result = writer.encode(contents, format, desiredWidth,
					desiredHeight, null);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	/**
	 * 生成显示编码的Bitmap
	 * 
	 * @param contents
	 * @param width
	 * @param height
	 * @param context
	 * @return
	 */
	protected Bitmap creatCodeBitmap(String contents, int width, int height,
			Context context) {
		TextView tv = new TextView(context);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(layoutParams);
		tv.setTextSize(8f);
		tv.setText(contents);
		tv.setHeight(height);
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setWidth(width);
		tv.setDrawingCacheEnabled(true);
		tv.setTextColor(Color.BLACK);
		tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
		tv.buildDrawingCache();
		Bitmap bitmapCode = tv.getDrawingCache();
		return bitmapCode;
	}

	/**
	 * 将两个Bitmap合并成一个
	 * 
	 * @param first
	 * @param second
	 * @param fromPoint
	 *            第二个Bitmap开始绘制的起始位置（相对于第一个Bitmap）
	 * @return
	 */
	protected Bitmap mixtureBitmap(Bitmap first, Bitmap second, PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		int marginW = 20;
		Bitmap newBitmap = Bitmap.createBitmap(first.getWidth() + marginW * 2,
				first.getHeight() + second.getHeight() / 2, Config.ARGB_4444);
		Canvas cv = new Canvas(newBitmap);
		cv.drawColor(Color.WHITE);
		cv.drawBitmap(first, marginW, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		cv.save(Canvas.ALL_SAVE_FLAG);
		cv.restore();

		return newBitmap;
	}

	public BarcodeFormat formatBar(int code) {
		BarcodeFormat barcodeFormat = null;
		switch (code) {
		case 0:
			barcodeFormat = BarcodeFormat.EAN_8;
			break;
		case 1:
			barcodeFormat = BarcodeFormat.EAN_13;
			break;
		case 2:
			barcodeFormat = BarcodeFormat.CODE_128; // 13
			break;
		case 3:
			barcodeFormat = BarcodeFormat.CODE_39;
			break;
		case 4:
			barcodeFormat = BarcodeFormat.CODE_93;
			break;
		case 5:
			barcodeFormat = BarcodeFormat.ITF;
			break;
		default:
			break;
		}

		return barcodeFormat;
	}

	public String saveMyBitmap(String fileName, Bitmap mBitmap, String sdPath) {
		File f = new File(sdPath + fileName);
		System.out.println(sdPath + fileName + "---------------");
		try {
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileOutputStream fOut = null;
		try {
			fOut = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		try {
			fOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return isOrFileExists(sdPath + fileName);
	}

	public String isOrFileExists(String path) {
		File file = new File(path);
		Log.d("path", path + "================");
		if (file.exists() && file.length() > 0) {
			Log.d("path", path + "=========--------=======");
			return path;
		} else {
			file.delete();
		}
		return null;
	}

}
