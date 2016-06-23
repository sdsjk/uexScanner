package org.zywx.wbpalmstar.plugin.uexscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexzxing.DataJsonVO;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;

public class EUExScanner extends EUExBase {

    private static final String BUNDLE_DATA = "data";
    private static final int MSG_SET_JSON_DATA = 1;
    private static final int MSG_OPEN = 2;
    private WWidgetData widgetData;
    private String sdPath = "";
    private DataJsonVO dataJson;

    public boolean mSupportCamera = false;
    private String openFuncId;

    public EUExScanner(Context context, EBrowserView view) {
        super(context, view);
        widgetData = view.getCurrentWidget();
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            // 动态获取数据存放目录
            sdPath = widgetData.getWidgetPath() + "scanner" + File.separator;
            File file = new File(sdPath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } else {
            Toast.makeText(mContext, "sd卡不存在，请查看", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected boolean clean() {
        return false;
    }

    public boolean setJsonData(String[] params) {
        String json = params[0];
        dataJson = DataHelper.gson.fromJson(json, DataJsonVO.class);
        if (dataJson.getLineImg() != null) {
            String lingImg = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), dataJson.getLineImg()), mBrwView.getCurrentWidget().m_widgetPath, mBrwView.getCurrentWidget().m_wgtType);
            dataJson.setLineImg(lingImg);
        }

        if (dataJson.getPickBgImg() != null) {
            String pickImg = BUtility.makeRealPath(BUtility.makeUrl(mBrwView.getCurrentUrl(), dataJson.getPickBgImg()), mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            dataJson.setPickBgImg(pickImg);
        }
        return true;
    }

    public void open(String[] params) {
        if (params != null && params.length ==1) {
            openFuncId = params[0];
        }
        mSupportCamera = isCameraCanUse();

        if (!mSupportCamera) {
            if (null != openFuncId) {
                callbackToJs(Integer.parseInt(openFuncId), false);
            } else {
                jsCallback(JsConst.CALLBACK_OPEN, 1, EUExCallback.F_C_JSON, "");
            }
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intents.Scan.ACTION);
        if (dataJson != null && dataJson.getCharset() != null) {
            intent.putExtra(Intents.Scan.CHARACTER_SET, dataJson.getCharset());
        }
        intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
        intent.setClass(mContext, CaptureActivity.class);
        if (dataJson != null) {
            intent.putExtra(JsConst.DATA_JSON, dataJson);
        }
        startActivityForResult(intent, 55555);
        dataJson = null;

    }



    public static boolean isCameraCanUse() {
        boolean canUse = true;
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);//这里是对于魅族MX5等手机在不授权时会挂掉的处理的
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            mCamera.release();
            mCamera = null;
        }

        return canUse;
    }


    @SuppressWarnings("unused")
    private void callBackPluginJs(String methodName, String jsonData) {
        String js = SCRIPT_HEADER + "if(" + methodName + "){" + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            try {
                JSONObject jobj = new JSONObject();
                jobj.put(EUExCallback.F_JK_CODE, data.getStringExtra(EUExCallback.F_JK_CODE));
                jobj.put(EUExCallback.F_JK_TYPE, data.getStringExtra(EUExCallback.F_JK_TYPE));
                String result = jobj.toString();
                jsCallback(JsConst.CALLBACK_OPEN, 0, EUExCallback.F_C_JSON, result);
                if (null != openFuncId) {
                    callbackToJs(Integer.parseInt(openFuncId), false, jobj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
