package org.zywx.wbpalmstar.plugin.uexzxing.client.android;


import java.util.Collection;
import java.util.HashSet;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexzxing.EScanStyleModel;
import org.zywx.wbpalmstar.plugin.uexzxing.EScannerUtils;
import org.zywx.wbpalmstar.plugin.uexzxing.ResultPoint;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.camera.CameraManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;


public final class ViewfinderViewForBarcode extends View {

  private static final int[] SCANNER_ALPHA = {0, 64, 128, 192, 255, 192, 128, 64};
  private static final long ANIMATION_DELAY = 10L;
  private static final int OPAQUE = 0xFF;

  private final Paint paint;
  private Bitmap resultBitmap;
  private final int maskColor;
  private final int resultColor;
  private final int laserColor;
  private final int resultPointColor;
  private int scannerAlpha;
  private Collection<ResultPoint> possibleResultPoints;
  private Collection<ResultPoint> lastPossibleResultPoints;
  boolean isFirst;
  private int slideTop;
  private static final int SPEEN_DISTANCE = 4;
  private static final int MIDDLE_LINE_WIDTH = 2;
  private DisplayMetrics dispm;
  private String tipLabel = "对准二维码/条形码,即可自动扫描";
  private String lineImg;
  private String pickBgImg;

  public ViewfinderViewForBarcode(Context context){
	  this(context, null);
  }
  
  public ViewfinderViewForBarcode(Context context, AttributeSet attrs) {
    super(context, attrs);

    paint = new Paint();
    maskColor = 0x60000000;
    resultColor = 0xb0000000;
    laserColor = 0xffffffff;
    resultPointColor = 0xc0ffff00;
    scannerAlpha = 0;
    possibleResultPoints = new HashSet<ResultPoint>(5);
    dispm = new DisplayMetrics();
	((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dispm);
  }

  @Override
  public void onDraw(Canvas canvas) {
    Rect frame = CameraManager.get().getFramingRect();
    if (frame == null) {
      return;
    }
    
    if(!isFirst){
        isFirst = true;
        slideTop = frame.top;
    }
    
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    float lon = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dispm);
    float win = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, dispm);
    paint.setColor(resultBitmap != null ? resultColor : maskColor);
    canvas.drawRect(0, 0, width, frame.top + win, paint);
    canvas.drawRect(0, frame.top + win, frame.left + win, frame.bottom - win, paint);
    canvas.drawRect(frame.right - win, frame.top + win, width, frame.bottom - win, paint);
    canvas.drawRect(0, frame.bottom - win, width, height, paint);

    if (resultBitmap != null) {
    
      tipLabel = "";
      writeText(canvas, frame, width, lon);
      
      paint.setAlpha(OPAQUE);
      float nw = 0;
      float nh = 0;
      if(frame.right - frame.left > resultBitmap.getWidth()) {
    	  nw =  (frame.right - frame.left - resultBitmap.getWidth()) / 2;
      }
      if(frame.bottom - frame.top > resultBitmap.getHeight()) {
    	  nh = (frame.bottom - frame.top - resultBitmap.getHeight()) / 2;
      }
      
      canvas.save();
      canvas.clipRect(frame.left+win, frame.top+win, frame.right-win, frame.bottom-win);
      canvas.drawBitmap(resultBitmap, frame.left + nw, frame.top + nh, paint);
      canvas.restore();
    } else {
    
      writeText(canvas, frame, width, lon);
    	
      paint.setColor(laserColor);
      
      slideTop += SPEEN_DISTANCE;
      if(slideTop >= frame.bottom - SPEEN_DISTANCE){
          slideTop = frame.top;
      }
      
      Bitmap scanBm = null;
      int w = CameraManager.getViewWidth();
      if(lineImg != null) {
    	  scanBm = EScannerUtils.getImage(getContext(), lineImg);
    	  Rect dst = new Rect();
    	  dst.left = frame.left;
    	  dst.top = slideTop - MIDDLE_LINE_WIDTH/2;
    	  dst.right = frame.right;
    	  dst.bottom = slideTop - MIDDLE_LINE_WIDTH/2 + scanBm.getHeight();
    	  canvas.drawBitmap(scanBm, null, dst, paint);
    	  dst = null;
      }else {
    	  Drawable scanD = getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_scanning"));
    	  int scanH = (int) (w * scanD.getIntrinsicHeight()/ scanD.getIntrinsicWidth());
    	  scanBm = drawableToBitmap(w, scanH, scanD);
    	  canvas.drawBitmap(scanBm, frame.left, slideTop - MIDDLE_LINE_WIDTH/2, paint);
      }
      
      paint.setAlpha(OPAQUE);
      
      
      canvas.save();
      Bitmap scanBp = null;
      canvas.clipRect(frame.left, frame.top, frame.right, frame.bottom);
      if(pickBgImg != null) {
    	  scanBp = EScannerUtils.getImage(getContext(), pickBgImg);
    	  Rect dst = new Rect();
    	  dst.left = frame.left;
    	  dst.top = frame.top;
    	  dst.right = frame.right;
    	  dst.bottom = frame.bottom;
    	  canvas.drawBitmap(scanBp, null, dst, paint);
    	  dst = null;
      }else {
    	  Drawable scanDb = getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scan_scanbg"));
    	  scanBp = drawableToBitmap(frame.right - frame.left, frame.bottom - frame.top, scanDb);
    	  canvas.drawBitmap(scanBp, frame.left, frame.top, paint);
      }
      canvas.restore();
      
      
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

      postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
    }
  }

private void writeText(Canvas canvas, Rect frame, int width, float lon) {
    paint.setTextSize(20 * dispm.density);
    paint.setColor(Color.WHITE);
    paint.setTextAlign(Align.CENTER);
    Rect bounds = new Rect();  
    paint.getTextBounds(tipLabel, 0, tipLabel.length(), bounds);  
    canvas.drawText(tipLabel, width/2, frame.bottom + lon, paint);
    paint.reset();
}
  
public void setStyleModel(EScanStyleModel styleModel) {
	if(styleModel.getLineImg() != null) {
		this.lineImg = styleModel.getLineImg();
	}
	if(styleModel.getPickBgImg() != null) {
		this.pickBgImg = styleModel.getPickBgImg();
	}
	if(styleModel.getTipLabel() != null) {
		this.tipLabel = styleModel.getTipLabel();
	}
}

public void drawViewfinder() {
    resultBitmap = null;
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live scanning display.
   *
   * @param barcode An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    possibleResultPoints.add(point);
  }

  public static Bitmap drawableToBitmap(int w, int h, Drawable drawable) {       
      Bitmap bitmap = Bitmap.createBitmap(w, h, 
                  drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                                      : Bitmap.Config.RGB_565);
      Canvas canvas = new Canvas(bitmap);
      drawable.setBounds(0, 0, w, h);
      drawable.draw(canvas);
      return bitmap;
}
  
}
