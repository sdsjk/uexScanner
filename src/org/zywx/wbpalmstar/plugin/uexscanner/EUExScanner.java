package org.zywx.wbpalmstar.plugin.uexscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
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

    public boolean mHasChecked =false;
    public boolean mSupportCamera =false;

    public EUExScanner(Context context, EBrowserView view) {
        super(context, view);
        widgetData = view.getCurrentWidget();
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

    @Override
    protected boolean clean() {
        return false;
    }


    public void setJsonData(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_SET_JSON_DATA;
        Bundle bd = new Bundle();
        bd.putStringArray(BUNDLE_DATA, params);
        msg.setData(bd);
        mHandler.sendMessage(msg);
    }

    private void setJsonDataMsg(String[] params) {
        String json = params[0];
        dataJson = DataHelper.gson.fromJson(json, DataJsonVO.class);
        if (dataJson.getLineImg() != null){
            String lingImg = BUtility.makeRealPath(
                    BUtility.makeUrl(mBrwView.getCurrentUrl(), dataJson.getLineImg()),
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            dataJson.setLineImg(lingImg);
        }

        if (dataJson.getPickBgImg() != null){
            String pickImg = BUtility.makeRealPath(
                    BUtility.makeUrl(mBrwView.getCurrentUrl(), dataJson.getPickBgImg()),
                    mBrwView.getCurrentWidget().m_widgetPath,
                    mBrwView.getCurrentWidget().m_wgtType);
            dataJson.setPickBgImg(pickImg);
        }

    }

    public void open(String[] params) {
        Message msg = new Message();
        msg.obj = this;
        msg.what = MSG_OPEN;
        mHandler.sendMessage(msg);
    }

    private void openMsg() {
        if (!mHasChecked){
            mSupportCamera =isCameraCanUse();
            mHasChecked =true;
        }
        if (!mSupportCamera){
            jsCallback(JsConst.CALLBACK_OPEN, 1, EUExCallback.F_C_JSON, "");
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intents.Scan.ACTION);
        intent.putExtra(Intents.Scan.MODE, Intents.Scan.QR_CODE_MODE);
        intent.setClass(mContext, CaptureActivity.class);
        if (dataJson != null){
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
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            mCamera.release();
            mCamera = null;
        }

        return canUse;
    }

    @Override
    public void onHandleMessage(Message message) {
        if(message == null){
            return;
        }
        Bundle bundle=message.getData();
        switch (message.what) {

            case MSG_SET_JSON_DATA:
                setJsonDataMsg(bundle.getStringArray(BUNDLE_DATA));
                break;
            case MSG_OPEN:
                openMsg();
                break;
            default:
                super.onHandleMessage(message);
        }
    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
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
                String result = jobj.toString();
                jsCallback(JsConst.CALLBACK_OPEN, 0, EUExCallback.F_C_JSON, result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
