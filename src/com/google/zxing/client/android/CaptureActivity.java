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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ace.zxing.BarcodeFormat;
import com.ace.zxing.BinaryBitmap;
import com.ace.zxing.DecodeHintType;
import com.ace.zxing.MultiFormatReader;
import com.ace.zxing.Result;
import com.ace.zxing.ResultMetadataType;
import com.google.zxing.client.android.camera.CameraManager;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.ACEImageLoader;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexscanner.JsConst;
import org.zywx.wbpalmstar.plugin.uexzxing.DataJsonVO;
import org.zywx.wbpalmstar.plugin.uexzxing.ScannerUtils;
import org.zywx.wbpalmstar.plugin.uexzxing.ViewToolView;

import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

  private static final String TAG = CaptureActivity.class.getSimpleName();
  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private ViewfinderView viewfinderView;
  private boolean hasSurface;
  private IntentSource source;
  private Collection<BarcodeFormat> decodeFormats;
  private String characterSet;
  private InactivityTimer inactivityTimer;
  private AmbientLightManager ambientLightManager;
  private RelativeLayout mConRel;
  private DataJsonVO mData;
  private ViewToolView mToolView;
    private Rect rect;
    private ImageView mGalleryPic;
    private TextView mFailText;
    private ScanPicHandler mHandler;

    ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public Handler getHandler() {
    return handler;
  }

  CameraManager getCameraManager() {
    return cameraManager;
  }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(EUExUtil.getResLayoutID("plugin_uexscanner_capture_layout"));

        if (getIntent() != null){
            DataJsonVO data = (DataJsonVO) getIntent().getSerializableExtra(JsConst.DATA_JSON);
            if (data != null){
            mData = data;
        }else{
            mData = new DataJsonVO();
        }
    }

    hasSurface = false;
    inactivityTimer = new InactivityTimer(this);
    ambientLightManager = new AmbientLightManager(this);
    mConRel = (RelativeLayout) findViewById(EUExUtil.getResIdID("plugin_uexscanner_content_rel"));
    initConView();
        mHandler = new ScanPicHandler(Looper.getMainLooper());
  }

  private void initConView(){
    mToolView = new ViewToolView(this, mData, toolListener);
    RelativeLayout.LayoutParams toolParams = new RelativeLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    int top = (int)getResources().getDimension(
            EUExUtil.getResDimenID("plugin_uexscanner_tool_top"));
    int left = (int)getResources().getDimension(
            EUExUtil.getResDimenID("plugin_uexscanner_tool_left"));
    toolParams.setMargins(left, top, left, 0);
      mToolView.setId(1);
    mToolView.setLayoutParams(toolParams);
    mConRel.addView(mToolView);

    viewfinderView = new ViewfinderView(this, mData);
    RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    viewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
    viewfinderView.setLayoutParams(viewParams);
    mConRel.addView(viewfinderView);

      mGalleryPic = new ImageView(this);
      RelativeLayout.LayoutParams picParams = new RelativeLayout.LayoutParams(
              LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
      picParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      picParams.addRule(RelativeLayout.BELOW, 1);
      picParams.setMargins(left, top, left, top);
      mGalleryPic.setLayoutParams(picParams);
      mConRel.addView(mGalleryPic);

      mGalleryPic.setVisibility(View.GONE);

      mFailText = new TextView(this);
      RelativeLayout.LayoutParams failTextParams = new RelativeLayout.LayoutParams(
              LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      failTextParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
      mFailText.setLayoutParams(failTextParams);
      mFailText.setGravity(Gravity.CENTER);
      mFailText.setTextColor(Color.WHITE);
      mFailText.setTextSize(25);
      mConRel.addView(mFailText);

      mFailText.setVisibility(View.GONE);
      mFailText.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              mFailText.setVisibility(View.GONE);
              mGalleryPic.setVisibility(View.GONE);
          }
      });

  }


    public class ScanPicHandler extends Handler {

        public ScanPicHandler(Looper loop) {
            super(loop);
        }

        public void handleMessage(Message msg) {
            Log.i("djf", "handleMessage");
            Bitmap bitmap = null;
            switch (msg.what){
                case JsConst.DISPLAY_BARCODE_IMAGE:
                    bitmap = (Bitmap) msg.obj;
                    mGalleryPic.setVisibility(View.VISIBLE);
                    mGalleryPic.setImageBitmap(bitmap);
                    break;
                case JsConst.HANDLE_RESULT:
                    handleDecode((Result) msg.obj, bitmap, 0f);
                    break;
            }
        }
    }

  @Override
  protected void onResume() {
    super.onResume();
      Log.i("djf", "onResume");
    // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
    // want to open the camera driver and measure the screen size if we're going to show the help on
    // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
    // off screen.
    cameraManager = CameraManager.getInstance(getApplication());

    handler = null;

    //resetStatusView();

    ambientLightManager.start(cameraManager);

    inactivityTimer.onResume();

    Intent intent = getIntent();

    source = IntentSource.NONE;
    decodeFormats = null;
    characterSet = null;

    if (intent != null) {
      String action = intent.getAction();
      if (Intents.Scan.ACTION.equals(action)) {
        // Scan the formats the intent requested, and return the result to the calling activity.
        source = IntentSource.NATIVE_APP_INTENT;
      }
      characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
    }

    SurfaceView surfaceView = (SurfaceView) findViewById(
            EUExUtil.getResIdID("plugin_uexscanner_preview_view"));
    SurfaceHolder surfaceHolder = surfaceView.getHolder();
    if (hasSurface) {
      // The activity was paused but not stopped, so the surface still exists. Therefore
      // surfaceCreated() won't be called, so init the camera here.
      initCamera(surfaceHolder);
    } else {
      // Install the callback and wait for surfaceCreated() to init the camera.
      surfaceHolder.addCallback(this);
    }
  }

    ToolListener toolListener = new ToolListener() {
        @Override
        public void hideViewFinder() {
            if (viewfinderView != null){
                viewfinderView.setVisibility(View.GONE);
            }
        }
    };
    public interface ToolListener {
        public void hideViewFinder();
    }

  @Override
  protected void onPause() {
    if (handler != null) {
      handler.quitSynchronously();
      handler = null;
    }
    inactivityTimer.onPause();
    ambientLightManager.stop();
    cameraManager.closeDriver();
    //historyManager = null; // Keep for onActivityResult
    if (!hasSurface) {
      SurfaceView surfaceView = (SurfaceView) findViewById(EUExUtil.getResIdID("preview_view"));
        if (surfaceView != null){
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            surfaceHolder.removeCallback(this);
        }
    }
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    inactivityTimer.shutdown();
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_BACK:
        if (source == IntentSource.NATIVE_APP_INTENT) {
          setResult(RESULT_CANCELED);
          finish();
          return true;
        }
        break;
      case KeyEvent.KEYCODE_FOCUS:
      case KeyEvent.KEYCODE_CAMERA:
        // Handle these events so they don't launch the Camera app
        return true;
      // Use volume up/down to turn on light
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        cameraManager.setTorch(false);
        return true;
      case KeyEvent.KEYCODE_VOLUME_UP:
        cameraManager.setTorch(true);
        return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if (holder == null) {
      Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
    }
    if (!hasSurface) {
      hasSurface = true;
      initCamera(holder);
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    hasSurface = false;
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

  }

  /**
   * A valid barcode has been found, so give an indication of success and show the results.
   *
   * @param rawResult The contents of the barcode.
   * @param scaleFactor amount by which thumbnail was scaled
   * @param barcode   A greyscale bitmap of the camera data which was decoded.
   */
  public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
    Log.i(TAG,"handleDecode-result = " + rawResult.getText());
    inactivityTimer.onActivity();
    ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
    switch (source) {
      case NATIVE_APP_INTENT:
        handleDecodeExternally(rawResult, resultHandler, barcode);
        break;
    }
  }

  // Briefly show the contents of the barcode, then handle the result outside Barcode Scanner.
  private void handleDecodeExternally(Result rawResult, ResultHandler resultHandler, Bitmap barcode) {
    if (source == IntentSource.NATIVE_APP_INTENT) {
      // Hand back whatever action they requested - this can be changed to Intents.Scan.ACTION when
      // the deprecated intent is retired.
      Intent intent = new Intent(getIntent().getAction());
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("code", rawResult.toString());
            jsonObject.put("result", rawResult.getText());
            jsonObject.put("type", rawResult.getBarcodeFormat());
            Map<ResultMetadataType, Object> data = new Hashtable<ResultMetadataType, Object>();
            Iterator iterator = data.keySet().iterator();
            while (iterator.hasNext()){
                final ResultMetadataType key = (ResultMetadataType) iterator.next();
                jsonObject.put(key.toString(), data.get(key).toString());
            }
            jsonObject.put("timestamp", rawResult.getTimestamp());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        intent.putExtra(EUExCallback.F_JK_CODE, rawResult.toString());
      intent.putExtra(EUExCallback.F_JK_TYPE, rawResult.getBarcodeFormat()
              .toString());
      sendReplyMessage(EUExUtil.getResIdID("return_scan_result"), intent, 0);
    }
  }

  private void sendReplyMessage(int id, Object arg, long delayMS) {
    if (handler != null) {
      Message message = Message.obtain(handler, id, arg);
      if (delayMS > 0L) {
        handler.sendMessageDelayed(message, delayMS);
      } else {
        handler.sendMessage(message);
      }
    }
  }

  private void initCamera(SurfaceHolder surfaceHolder) {
    if (surfaceHolder == null) {
      throw new IllegalStateException("No SurfaceHolder provided");
    }
    if (cameraManager.isOpen()) {
      Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
      return;
    }
    try {
        cameraManager.openDriver(surfaceHolder);
      // Creating the handler starts the preview, which can also throw a RuntimeException.
      if (handler == null) {
        handler = new CaptureActivityHandler(this, decodeFormats, null, characterSet, cameraManager);
      }
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      displayFrameworkBugMessageAndExit();
    } catch (RuntimeException e) {
      // Barcode Scanner has seen crashes in the wild of this variety:
      // java.?lang.?RuntimeException: Fail to connect to camera service
      Log.w(TAG, "Unexpected error initializing camera", e);
      displayFrameworkBugMessageAndExit();
    }
      rect = getCameraManager().getFramingRect();
  }

  private void displayFrameworkBugMessageAndExit() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(EUExUtil.getResStringID("app_name")));
    builder.setMessage(getString(EUExUtil.getResStringID("plugin_uexscanner_msg_camera_framework_bug")));
                    builder.setPositiveButton(EUExUtil.getResStringID("confirm"),
                            new FinishListener(this));
                            builder.setOnCancelListener(new FinishListener(this));
    builder.show();
  }

  private void resetStatusView() {
    viewfinderView.setVisibility(View.VISIBLE);
  }

  public void drawViewfinder() {
    viewfinderView.drawViewfinder();
  }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i("djf", "onActivityResult");
        mGalleryPic.setVisibility(View.GONE);
        mGalleryPic.setImageBitmap(null);
        mFailText.setVisibility(View.GONE);
        mFailText.setText(null);
        if (requestCode == JsConst.START_IMAGE_INTENT
                && resultCode == RESULT_OK && null != data) {
            ScanLocalImageAsyncTask task = new ScanLocalImageAsyncTask(data.getData());
            task.execute();
        }else {
            resetStatusView();
        }
    }

    private class ScanLocalImageAsyncTask extends AsyncTask<Object, Object, Integer>{

        private Uri uri;
        private ProgressDialog dialog;

        public ScanLocalImageAsyncTask(Uri uri) {
            this.uri = uri;
        }

        @Override
        protected void onPreExecute() {
            dialog = ProgressDialog.show(CaptureActivity.this,
                    EUExUtil.getString("prompt"),
                    EUExUtil.getString("plugin_uexscanner_msg_scanning"));
        }

        @Override
        protected void onPostExecute(Integer o) {
            if (dialog != null){
                dialog.dismiss();
                dialog = null;
            }
            resetStatusView();
            if (o == JsConst.SCAN_FAIL){
                mFailText.setText(Html.fromHtml("图片扫描失败\n" +
                        "<font color=\"#ff0000\">点击重试</font>"));
                mFailText.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Integer doInBackground(Object[] params) {
            int width = Math.round(rect.right - rect.left);
            Bitmap bitmap = ScannerUtils.formatBitmap(getApplicationContext(), width, uri);
            Message msg = new Message();
            msg.what = JsConst.DISPLAY_BARCODE_IMAGE;
            msg.obj = bitmap;
            mHandler.sendMessage(msg);
            try {
                BinaryBitmap barcode = ScannerUtils.loadImage(bitmap);
                if(barcode != null) {
                    MultiFormatReader multiFormatReader = new MultiFormatReader();

                    Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(3);

                    Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();

                    //decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);      //支持解一维码
                    decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS);
                    decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS);
                    decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS);
                    decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS);
                    decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);    //支持解QR码
                    decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);//支持解矩阵码

                    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

                    if (characterSet != null) {
                        hints.put(DecodeHintType.CHARACTER_SET, characterSet);
                    }

                    multiFormatReader.setHints(hints);
                    Result rawResult = multiFormatReader.decodeWithState(barcode);
                    if(rawResult != null) {
                        Log.i("djf", "handleDecode");
                        Message handleResult = new Message();
                        handleResult.what = JsConst.HANDLE_RESULT;
                        handleResult.obj = rawResult;
                        mHandler.sendMessage(handleResult);
                    }
                }
                return JsConst.SCAN_SUCCESS;
            } catch (Exception e) {
                e.printStackTrace();
                return JsConst.SCAN_FAIL;
            }
        }
    }
}
