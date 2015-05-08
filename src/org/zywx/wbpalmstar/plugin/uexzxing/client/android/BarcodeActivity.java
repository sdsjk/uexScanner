package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexzxing.BarcodeFormat;
import org.zywx.wbpalmstar.plugin.uexzxing.BinaryBitmap;
import org.zywx.wbpalmstar.plugin.uexzxing.DecodeHintType;
import org.zywx.wbpalmstar.plugin.uexzxing.EScanStyleModel;
import org.zywx.wbpalmstar.plugin.uexzxing.EScannerUtils;
import org.zywx.wbpalmstar.plugin.uexzxing.MultiFormatReader;
import org.zywx.wbpalmstar.plugin.uexzxing.Result;
import org.zywx.wbpalmstar.plugin.uexzxing.ResultMetadataType;
import org.zywx.wbpalmstar.plugin.uexzxing.ResultPoint;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.ViewSplashViewForBarcode.OnFlashListener;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.camera.CameraManager;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.result.ResultHandler;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.result.ResultHandlerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.ClipboardManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.Toast;

public final class BarcodeActivity extends Activity implements SurfaceHolder.Callback {

	public static final int auto_focus = 0;
	public static final int decode = 1;
	public static final int decode_failed = 2;
	public static final int decode_succeeded = 3;
	public static final int encode_failed = 4;
	public static final int encode_succeeded = 5;
	public static final int launch_product_query = 6;
	public static final int quit = 7;
	public static final int restart_preview = 8;
	public static final int return_scan_result = 9;
	public static final int search_book_contents_failed = 10;
	public static final int search_book_contents_succeeded = 11;

	private static final long INTENT_RESULT_DURATION = 1500L;
	private static final float BEEP_VOLUME = 0.10f;
	private static final long VIBRATE_DURATION = 200L;
	private static final Set<ResultMetadataType> DISPLAYABLE_METADATA_TYPES;
	public static final String DISPLAY_TEXT;
	public static final String DISPLAY_TEXT_BUG;
	public static final String DISPLAY_TEXT_OK;
	public static final String DISPLAY_BTN_TEXT_OK;
	public static final String DISPLAY_BTN_TEXT_CANCEL;
	
	private BarcodeActivityHandler handler;
	private ViewfinderViewForBarcode viewfinderView;
	private ViewHeaderViewForBarcode statusView;
	private MediaPlayer mediaPlayer;
	private Result lastResult;
	private boolean hasSurface;
	private boolean playBeep;
	private boolean vibrate;
	private boolean copyToClipboard;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private DisplayMetrics dispm;
	private SurfaceView surfaceView;
	private boolean input;
	private ViewSplashViewForBarcode splash;
	private OnFlashListener onFlashListener;
	private EScanStyleModel sModel;
	private boolean isFirstBoot = true;
	
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	ViewfinderViewForBarcode getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}
	
	public SurfaceHolder getSurfaceHolder() {
		return surfaceView.getHolder();
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		if(!ZRes.init(this)){
			Toast.makeText(this, "资源文件不存在,本plugin将退出", Toast.LENGTH_SHORT).show();
			finish();
		}
		sModel = (EScanStyleModel) getIntent().getSerializableExtra(EScannerUtils.SCANER_PARAMS_CODE_OBJ);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		dispm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dispm);
		FrameLayout layout = new FrameLayout(this);
		FrameLayout.LayoutParams parm =  new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		layout.setLayoutParams(parm);
		
		surfaceView = new SurfaceView(this);
		FrameLayout.LayoutParams parm1 =  new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		parm1.gravity = Gravity.CENTER;
		surfaceView.setLayoutParams(parm1);
		
		viewfinderView = new ViewfinderViewForBarcode(this);
		if(sModel != null) {
			viewfinderView.setStyleModel(sModel);
		}
		viewfinderView.setBackgroundColor(0);
		FrameLayout.LayoutParams parm2 = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		parm2.gravity = Gravity.CENTER;
		viewfinderView.setLayoutParams(parm2);
		
		splash = new ViewSplashViewForBarcode(this);
		FrameLayout.LayoutParams parm5 =  new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		splash.setLayoutParams(parm5);
		
		
		if(sModel != null && sModel.getTitle() != null) {
			statusView = new ViewHeaderViewForBarcode(this, sModel.getTitle());
		}else {
			statusView = new ViewHeaderViewForBarcode(this, null);
		}
		FrameLayout.LayoutParams parm3 =  new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		parm3.gravity = Gravity.TOP;
		statusView.setLayoutParams(parm3);
		
		ViewToolViewForBarcode tool = new ViewToolViewForBarcode(this);
		FrameLayout.LayoutParams parm4 =  new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		parm4.gravity = Gravity.BOTTOM;
		tool.setLayoutParams(parm4);
		
		layout.addView(surfaceView);
		layout.addView(viewfinderView);
		layout.addView(splash);
		layout.addView(statusView);
		layout.addView(tool);
		
		setContentView(layout);
		CameraManager.init(this);

		handler = null;
		lastResult = null;
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}
	
	public DisplayMetrics getDisplayMetrics(){
		return dispm;
	}
	
	public void swichInput(View inputView){
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
		setContentView(inputView);
		TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, 
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 1.0f, 
				Animation.RELATIVE_TO_PARENT, 0.0f);
		anim.setDuration(250);
		inputView.startAnimation(anim);
		input = true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		 if (requestCode == 1 && resultCode == RESULT_OK && null != data) {
			 Rect rect = CameraManager.get().getFramingRect();
			 int width = Math.round(rect.right - rect.left);
	         Bitmap bitmap = EScannerUtils.formatBitmap(getApplicationContext(), width, data.getData());
			 try {
		            BinaryBitmap barcode = EScannerUtils.loadImage(bitmap);
		            if(barcode != null) {
		            	MultiFormatReader multiFormatReader = new MultiFormatReader();
		            	
		            	Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(3);
		            	
		            	Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
		            	
		                decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);      //支持解一维码
		                decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);    //支持解QR码
		                decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);//支持解矩阵码
		                
		                hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
		                
		                if (characterSet != null) {
		                    hints.put(DecodeHintType.CHARACTER_SET, characterSet);
		                  }
		                
		                multiFormatReader.setHints(hints);
		                
		            	Result rawResult = multiFormatReader.decodeWithState(barcode);
		            	if(rawResult != null) {
		            		handleDecode(rawResult, bitmap);
		            	}
		            }
		        } catch (Exception e) {
		        	if (handler != null) {
		    			handler.quitSynchronously();
		    			handler = null;
		    		}
		        	inactivityTimer.shutdown();
		    		CameraManager.get().closeDriver();
		        	viewfinderView.drawResultBitmap(bitmap);
		        	Intent intent = new Intent(getIntent().getAction());
		    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		    		intent.putExtra(EUExCallback.F_JK_CODE, "");
		    		intent.putExtra(EUExCallback.F_JK_TYPE, "");
		        	if(handler == null) {
		    			handler = new BarcodeActivityHandler(this, decodeFormats, characterSet);
		    		}
		    		Message message = Message.obtain(handler, return_scan_result);
		    		message.obj = intent;
		    		handler.sendMessageDelayed(message, INTENT_RESULT_DURATION);
		        	e.printStackTrace();
			}
		 }
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		Intent intent = getIntent();
		String action = intent == null ? null : intent.getAction();
		if (intent != null && action != null) {
			if (action.equals(Intents.Scan.ACTION)) {
				decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
			}
			characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);
		}

		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		vibrate = true;
		copyToClipboard = false;
		initBeepSound();
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.shutdown();
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		isFirstBoot = true;
		super.onDestroy();
	}

	public void surfaceCreated(final SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			if(isFirstBoot) {
				splash.setOnFlashListener(new OnFlashListener() {
					
					@Override
					public void onFlash() {
						initCamera(holder);
					}
				});
				splash.startAnims();
				isFirstBoot = false;
			}else {
				initCamera(holder);
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	public void handleDecode(Result rawResult, Bitmap barcode) {
		lastResult = rawResult;
		playBeepSoundAndVibrate();
		handleDecodeExternally(rawResult, barcode);
	}

	public Result getLastResult() {

		return lastResult;
	}

	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(0xffffffff);
			paint.setStrokeWidth(3.0f);
			paint.setStyle(Paint.Style.STROKE);
			Rect border = new Rect(2, 2, barcode.getWidth() - 2, barcode.getHeight() - 2);
			canvas.drawRect(border, paint);

			paint.setColor(0xc000ff00);
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat()
							.equals(BarcodeFormat.UPC_A))
					|| (rawResult.getBarcodeFormat()
							.equals(BarcodeFormat.EAN_13))) {
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b) {
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}


	private void handleDecodeExternally(Result rawResult, Bitmap barcode) {
		viewfinderView.drawResultBitmap(barcode);
		ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(this, rawResult);
		if (copyToClipboard) {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clipboard.setText(resultHandler.getDisplayContents());
		}
		Intent intent = new Intent(getIntent().getAction());
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra(EUExCallback.F_JK_CODE, rawResult.toString());
		intent.putExtra(EUExCallback.F_JK_TYPE, rawResult.getBarcodeFormat().toString());
		if(handler == null) {
			handler = new BarcodeActivityHandler(this, decodeFormats, characterSet);
		}
		Message message = Message.obtain(handler, return_scan_result);
		message.obj = intent;
		handler.sendMessageDelayed(message, INTENT_RESULT_DURATION);
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {

			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(ZRes.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	public void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			displayFrameworkBugMessageAndExit();
			return;
		} catch (RuntimeException e) {
			e.printStackTrace();
			displayFrameworkBugMessageAndExit();
			return;
		}
		if (handler == null) {
			handler = new BarcodeActivityHandler(this, decodeFormats, characterSet);
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(ZRes.app_name));
		builder.setMessage(DISPLAY_TEXT_BUG);
		builder.setPositiveButton(DISPLAY_BTN_TEXT_OK, new FinishListener(
				this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}
	
	static {
		DISPLAYABLE_METADATA_TYPES = new HashSet<ResultMetadataType>(5);
		DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.ISSUE_NUMBER);
		DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.SUGGESTED_PRICE);
		DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.ERROR_CORRECTION_LEVEL);
		DISPLAYABLE_METADATA_TYPES.add(ResultMetadataType.POSSIBLE_COUNTRY);
		Locale language = Locale.getDefault();
		if(language.equals(Locale.CHINA) 
				|| language.equals(Locale.CHINESE) 
				|| language.equals(Locale.TAIWAN) 
				|| language.equals(Locale.TRADITIONAL_CHINESE)
				|| language.equals(Locale.SIMPLIFIED_CHINESE)
				|| language.equals(Locale.PRC)){
			
			DISPLAY_TEXT = "扫一扫";
			DISPLAY_TEXT_BUG = "很抱歉，设备相机出现问题。\n您可能没有配置使用相机的权限。";
			DISPLAY_TEXT_OK = "扫描成功";
			DISPLAY_BTN_TEXT_OK = "确定";
			DISPLAY_BTN_TEXT_CANCEL = "取消";
		}else{
			DISPLAY_TEXT = "Keep the pictrue in the right place";
			DISPLAY_TEXT_BUG = "Sorry, the Android camera encountered a problem.\n You need to add permission:'android.permission.CAMERA'.";
			DISPLAY_TEXT_OK = "Found plain text";
			DISPLAY_BTN_TEXT_OK = "Ok";
			DISPLAY_BTN_TEXT_CANCEL = "Cancel";
		}
	}
}
