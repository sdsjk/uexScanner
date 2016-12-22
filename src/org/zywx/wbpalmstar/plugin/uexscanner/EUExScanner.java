package org.zywx.wbpalmstar.plugin.uexscanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.ace.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.ace.universalimageloader.core.ImageLoader;
import com.ace.universalimageloader.core.ImageLoaderConfiguration;
import com.ace.universalimageloader.core.assist.QueueProcessingType;
import com.ace.universalimageloader.core.download.BaseImageDownloader;
import com.ace.zxing.BarcodeFormat;
import com.ace.zxing.BinaryBitmap;
import com.ace.zxing.DecodeHintType;
import com.ace.zxing.MultiFormatReader;
import com.ace.zxing.NotFoundException;
import com.ace.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.DecodeFormatManager;
import com.google.zxing.client.android.Intents;

import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.plugin.uexzxing.DataJsonVO;
import org.zywx.wbpalmstar.plugin.uexzxing.ScannerUtils;
import org.zywx.wbpalmstar.widgetone.dataservice.WWidgetData;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

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
        if (params != null && params.length == 1) {
            openFuncId = params[0];
        }
        mSupportCamera = isCameraCanUse();

        if (!mSupportCamera) {
            if (null != openFuncId) {
                callbackToJs(Integer.parseInt(openFuncId), false, EUExCallback.F_C_FAILED);
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

    public String recognizeFromImage(String params[]) {
        final String str = params[0];
        if (TextUtils.isEmpty(str)) {
            return null;
        }

        Bitmap picBitmap = getPicBitmap(str);
        String result = recognizeFromBitmap(picBitmap);
        return result;
    }

    private Bitmap getPicBitmap(String path) {
        if (path.startsWith("http")) {
            ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(mContext);
            config.threadPriority(Thread.NORM_PRIORITY);
            config.denyCacheImageMultipleSizesInMemory();
            config.memoryCacheExtraOptions(480, 800);
            config.memoryCache(new LRULimitedMemoryCache(8 * 1024 * 1024));
            config.tasksProcessingOrder(QueueProcessingType.LIFO);
            //修改连接超时时间5秒，下载超时时间20秒
            config.imageDownloader(new BaseImageDownloader(mContext, 5 * 1000, 20 * 1000));
            ImageLoader.getInstance().init(config.build());
            return ImageLoader.getInstance().loadImageSync(path);
        } else {
            return BUtility.getLocalImg(mContext, path);
        }
    }

    private String recognizeFromBitmap(Bitmap picBitmap) {
        BinaryBitmap barcode = null;
        try {
            barcode = ScannerUtils.loadImage(picBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (barcode != null) {
            MultiFormatReader multiFormatReader = new MultiFormatReader();

            Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(3);

            Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();

            // decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
            // //支持解一维码
            decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS);
            decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS); // 支持解QR码
            decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);// 支持解矩阵码

            hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);


            multiFormatReader.setHints(hints);
            Result rawResult = null;
            try {
                rawResult = multiFormatReader.decodeWithState(barcode);
            } catch (NotFoundException e) {
                e.printStackTrace();
            }
            if (rawResult != null) {
                return rawResult.toString();
            }
        }
        return null;
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
                jobj.put(EUExCallback.F_JK_CODE,
                        data.getStringExtra(EUExCallback.F_JK_CODE).replace("\"", "\\\""));
                jobj.put(EUExCallback.F_JK_TYPE, data.getStringExtra(EUExCallback.F_JK_TYPE));
                String result = jobj.toString();
                if (null != openFuncId) {
                    callbackToJs(Integer.parseInt(openFuncId), false, EUExCallback.F_C_SUCCESS, jobj);
                } else {
                    jsCallback(JsConst.CALLBACK_OPEN, 0, EUExCallback.F_C_JSON, result);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
