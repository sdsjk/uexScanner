package org.zywx.wbpalmstar.plugin.uexzxing;

import java.io.File;

import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.BarcodeActivity;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.CaptureActivity;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.Intents;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.widget.Toast;

public class EUExScanner extends EUExBase {

	public static final String function = "uexScanner.cbOpen";
	public static final String function_QR = "uexScanner.cbCreateBarCode";
	public static final String function_TwoDimensionCode = "uexScanner.cbTwoDimensionCode";

	private WWidgetData widgetData;
	private String sdPath = "";
	private CreateBarCode createImg;
	private EScanStyleModel model;

	public EUExScanner(Context context, EBrowserView view) {
		super(context, view);
		widgetData = view.getCurrentWidget();
		createImg = new CreateBarCode();
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			// 动态获取数据存放目录
			sdPath = widgetData.getWidgetPath() + "scanner" + File.separator;
			File file = new File(sdPath);
			if (!file.exists()) {
				// 创建目录
				file.mkdirs();
			}
			// Toast.makeText(mContext, sdPath, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, "sd卡不存在，请查看", Toast.LENGTH_SHORT).show();
		}
	}

	@SuppressWarnings("unused")
	public void open(String[] parm) {
		String zXingOrBar = "ZXing";
		String inDes = "";
		if (parm.length > 0) {
			zXingOrBar = parm[0];
		} else if(parm.length > 1){
			zXingOrBar = parm[0];
			inDes = parm[1];
		}
		if ("ZXing".equals(zXingOrBar)) {
			Intent intent = new Intent();
			intent.setAction(Intents.Scan.ACTION);
			intent.setClass(mContext, CaptureActivity.class);
			startActivityForResult(intent, 55555);
		} else if ("ZBar".equals(zXingOrBar)) {
			Intent intent = new Intent();
			intent.setAction(Intents.Scan.ACTION);
			intent.setClass(mContext, BarcodeActivity.class);
			intent.putExtra(EScannerUtils.SCANER_PARAMS_CODE_OBJ, model);
			startActivityForResult(intent, 55555);
		}

	}
	
	public void setJsonData(String[] params){
		if(params != null && params.length != 1) {
			return;
		}
		model = EScannerUtils.parse2Model(params[0]);
	}

	public void openthebarcode(String[] parm) {
		if (parm.length < 1) {
			return;
		}
		Intent intent = new Intent();
		intent.setAction(Intents.Scan.ACTION);
		intent.setClass(mContext, BarcodeActivity.class);
		startActivityForResult(intent, 55555);
	}

	public void createBarCode(String[] parm) {
		if (parm.length < 5) {
			return;
		}
		String contents = "";
		String desiredWidth = "";
		String desiredHeigh = "";
		String displayCode = "";
		String code = "";
		try {
			contents = parm[0];
			desiredWidth = parm[1];
			desiredHeigh = parm[2];
			displayCode = parm[3];
			code = parm[4];
		} catch (Exception e) {
			e.printStackTrace();
		}
		Bitmap bitmap = createImg.creatBarcode(mContext, contents,
				Integer.parseInt(desiredWidth), Integer.parseInt(desiredHeigh),
				Boolean.parseBoolean(displayCode), Integer.parseInt(code));

		System.out.println(bitmap.getWidth() + "----" + bitmap.getHeight());
		String fileName = "scanner" + System.currentTimeMillis() + "" + ".png";
		String strQR_Path1 = createImg.saveMyBitmap(fileName, bitmap, sdPath);

		jsCallback(function_QR, 0, EUExCallback.F_C_TEXT, strQR_Path1);
	}

	public void twoDimensionCode(String parm[]) {
		if (parm.length < 3) {
			return;
		}
		String url = "";
		String width = "";
		String height = "";
		try {
			url = parm[0];
			width = parm[1];
			height = parm[2];
			Bitmap bitmap = CreateTwoDimensionCode.createQRImage(url,
					Integer.parseInt(width), Integer.parseInt(height));
			String imageSrc = "scanner" + System.currentTimeMillis() + ""
					+ ".png";
			String strQR_Path = createImg
					.saveMyBitmap(imageSrc, bitmap, sdPath);
			jsCallback(function_TwoDimensionCode, 0, EUExCallback.F_C_TEXT,
					strQR_Path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			try {
				JSONObject jobj = new JSONObject();
				jobj.put(EUExCallback.F_JK_CODE,
						data.getStringExtra(EUExCallback.F_JK_CODE));
				jobj.put(EUExCallback.F_JK_TYPE,
						data.getStringExtra(EUExCallback.F_JK_TYPE));
				jsCallback(function, 0, EUExCallback.F_C_JSON, jobj.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean clean() {
		return false;
	}
}
