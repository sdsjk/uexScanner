package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import java.util.Hashtable;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import org.zywx.wbpalmstar.plugin.uexzxing.BarcodeFormat;
import org.zywx.wbpalmstar.plugin.uexzxing.DecodeHintType;
import org.zywx.wbpalmstar.plugin.uexzxing.ResultPointCallback;

import android.os.Handler;
import android.os.Looper;

/**
 * 
 *解码线程
 */
final class DecodeThreadForBarcode extends Thread {

  public static final String BARCODE_BITMAP = "barcode_bitmap";

  private final BarcodeActivity activity;
  private final Hashtable<DecodeHintType, Object> hints;
  private Handler handler;
  private final CountDownLatch handlerInitLatch;

  DecodeThreadForBarcode(BarcodeActivity activity,
               Vector<BarcodeFormat> decodeFormats,
               String characterSet,
               ResultPointCallback resultPointCallback) {

    this.activity = activity;
    handlerInitLatch = new CountDownLatch(1);

    hints = new Hashtable<DecodeHintType, Object>(3);

    // The prefs can't change while the thread is running, so pick them up once here.
    if (decodeFormats == null || decodeFormats.isEmpty()) {
      decodeFormats = new Vector<BarcodeFormat>();
      decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);      //支持解一维码
      decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);    //支持解QR码
      decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);//支持解矩阵码
    }
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

    if (characterSet != null) {
      hints.put(DecodeHintType.CHARACTER_SET, characterSet);
    }

    hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
  }

  Handler getHandler() {
    try {
      handlerInitLatch.await();
    } catch (InterruptedException ie) {
      // continue?
    }
    return handler;
  }

  @Override
  public void run() {
    Looper.prepare();
    handler = new DecodeHandlerForBarcode(activity, hints);
    handlerInitLatch.countDown();
    Looper.loop();
  }

}
