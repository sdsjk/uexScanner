/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;

import com.ace.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexscanner.utils.MLog;
import org.zywx.wbpalmstar.plugin.uexzxing.DataJsonVO;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class ViewfinderView extends View {

	@SuppressWarnings("unused")
	private static final int[] SCANNER_ALPHA = { 0, 64, 128, 192, 255, 192, 128, 64 };
	private static final long ANIMATION_DELAY = 10L;
	private static final int OPAQUE = 0xFF;

	private final Paint paint;
	private Bitmap resultBitmap;
	private final int maskColor;
	private final int resultColor;
	// private final int frameColor;
	@SuppressWarnings("unused")
	private final int laserColor;
	private final int resultPointColor;
	@SuppressWarnings("unused")
	private int scannerAlpha;
	private Collection<ResultPoint> possibleResultPoints;
	private Collection<ResultPoint> lastPossibleResultPoints;
	private Context mContext;

	boolean isFirst;
	/**
	 * 中间滑动线的最顶端位置
	 */
	private int slideTop;

	/**
	 * 扫描框中的中间线的宽度
	 */
	private static final int MIDDLE_LINE_WIDTH = 2;

	/**
	 * 中间那条线每次刷新移动的距离
	 */
	private static final int SPEEN_DISTANCE = 4;

	/**
	 * 手机的屏幕密度
	 */
	private static float density;

	/**
	 * 字体大小
	 */
	private static final int TEXT_SIZE = 14;
	/**
	 * 字体距离扫描框下面的距离
	 */
	private static final int TEXT_PADDING_TOP = 30;

	private DataJsonVO mData;

	// This constructor is used when the class is built from an XML resource.
	public ViewfinderView(Context context, DataJsonVO data) {
		super(context);
		this.mContext = context;
		this.mData = data;
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;
		// Initialize these once for performance rather than calling them every
		// time in onDraw().
		paint = new Paint();
		maskColor = resources.getColor(EUExUtil.getResColorID("plugin_uexscanner_viewfinder_mask"));
		resultColor = resources.getColor(EUExUtil.getResColorID("plugin_uexscanner_result_view"));
		laserColor = resources.getColor(EUExUtil.getResColorID("plugin_uexscanner_viewfinder_laser"));
		resultPointColor = resources.getColor(EUExUtil.getResColorID("plugin_uexscanner_possible_result_points"));

		scannerAlpha = 0;
		possibleResultPoints = new HashSet<ResultPoint>(5);
	}

	@SuppressLint("DrawAllocation")
	@Override
	public void onDraw(Canvas canvas) {
		// 中间的扫描框，你要修改扫描框的大小，去CameraManager里面修改
		Rect frame = CameraManager.getInstance(mContext).getFramingRect();
		if (frame == null) {
			return;
		}

		// 初始化中间线滑动的最上边和最下边
		if (!isFirst) {
			isFirst = true;
			slideTop = frame.top;
		}

		// 获取屏幕的宽和高
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		paint.setColor(resultBitmap != null ? resultColor : maskColor);

		// 画出扫描框外面的阴影部分，共四个部分，扫描框的上面到屏幕上面，扫描框的下面到屏幕下面
		// 扫描框的左边面到屏幕左边，扫描框的右边到屏幕右边
		canvas.drawRect(0, 0, width, frame.top, paint);
		canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
		canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
		canvas.drawRect(0, frame.bottom + 1, width, height, paint);

		if (resultBitmap != null) {
			// Draw the opaque result bitmap over the scanning rectangle
			paint.setAlpha(OPAQUE);
			canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
		} else {
			// 画扫描框边上的角，总共8个部分
			int w = CameraManager.getViewWidth();
			int h = CameraManager.getViewHeight();
			Drawable d = getPickBgImg();
			Bitmap bg = drawableToBitmap(w, h, d);
			canvas.drawBitmap(bg, frame.left, frame.top, paint);

			// 绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
			slideTop += SPEEN_DISTANCE;
			if (slideTop >= frame.bottom) {
				slideTop = frame.top;
			}
			Drawable scanD = getLineImg();
			int scanH = (int) (w * scanD.getIntrinsicHeight() / scanD.getIntrinsicWidth());
			Bitmap scanBm = drawableToBitmap(w, scanH, scanD);
			canvas.drawBitmap(scanBm, frame.left, slideTop - MIDDLE_LINE_WIDTH / 2, paint);

			// 画扫描框下面的字
			paint.setColor(Color.WHITE);
			paint.setTextSize(TEXT_SIZE * density);
			paint.setTextAlign(Align.CENTER);
			canvas.drawText(getTipLabel(), width / 2, (float) (frame.bottom + (float) TEXT_PADDING_TOP * density), paint);

			Collection<ResultPoint> currentPossible = possibleResultPoints;
			Collection<ResultPoint> currentLast = lastPossibleResultPoints;
			if (currentPossible.isEmpty()) {
				lastPossibleResultPoints = null;
			} else {
				possibleResultPoints = new HashSet<ResultPoint>(5);
				lastPossibleResultPoints = currentPossible;
				paint.setAlpha(OPAQUE);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentPossible) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 6.0f, paint);
				}
			}
			if (currentLast != null) {
				paint.setAlpha(OPAQUE / 2);
				paint.setColor(resultPointColor);
				for (ResultPoint point : currentLast) {
					canvas.drawCircle(frame.left + point.getX(), frame.top + point.getY(), 3.0f, paint);
				}
			}

			// 只刷新扫描框的内容，其他地方不刷新
			postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);

		}
	}

	public void drawViewfinder() {
		resultBitmap = null;
		invalidate();
	}

	/**
	 * Draw a bitmap with the result points highlighted instead of the live
	 * scanning display.
	 *
	 * @param barcode
	 *            An image of the decoded barcode.
	 */
	public void drawResultBitmap(Bitmap barcode) {
		resultBitmap = barcode;
		invalidate();
	}

	public void addPossibleResultPoint(ResultPoint point) {
		possibleResultPoints.add(point);
	}

	public static Bitmap drawableToBitmap(int w, int h, Drawable drawable) {
		MLog.getIns().i("w = " + w + " h = " + h);
		if (h <= 0) {// 当h小于等于0时,createBitmap会抛出IllegalArgumentException: width
						// and height must be > 0
			h = 5;// 所以当h小于等于0时，强制把h变为0以上
		}
		Bitmap bitmap = Bitmap.createBitmap(w, h, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		drawable.draw(canvas);
		return bitmap;
	}

	@SuppressWarnings("deprecation")
	private Drawable getLineImg() {
		Drawable lineImg;
		if (mData.getLineImg() == null) {
			lineImg = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_scanning"));
		} else {
			Bitmap bitmap = BUtility.getLocalImg(mContext, mData.getLineImg());
			lineImg = new BitmapDrawable(bitmap);
		}
		return lineImg;
	}

	@SuppressWarnings("deprecation")
	private Drawable getPickBgImg() {
		Drawable pickBgImg;
		if (mData.getLineImg() == null) {
			pickBgImg = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_frame"));
		} else {
			Bitmap bitmap = BUtility.getLocalImg(mContext, mData.getPickBgImg());
			pickBgImg = new BitmapDrawable(bitmap);
		}
		return pickBgImg;
	}

	private String getTipLabel() {
		String tip = mContext.getResources().getString(EUExUtil.getResStringID("plugin_uexscanner_msg_default_status"));
		if (!TextUtils.isEmpty(mData.getTipLabel())) {
			tip = mData.getTipLabel();
		}
		return tip;
	}
}
