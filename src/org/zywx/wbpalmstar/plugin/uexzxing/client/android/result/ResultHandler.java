package org.zywx.wbpalmstar.plugin.uexzxing.client.android.result;

import org.zywx.wbpalmstar.plugin.uexzxing.Result;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.CaptureActivity;
import org.zywx.wbpalmstar.plugin.uexzxing.client.android.ZRes;
import org.zywx.wbpalmstar.plugin.uexzxing.client.result.ParsedResult;
import org.zywx.wbpalmstar.plugin.uexzxing.client.result.ParsedResultType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;




public abstract class ResultHandler {

  public static final int MAX_BUTTON_COUNT = 2;
  private final ParsedResult mResult;
  private final Activity mActivity;
 
  
  ResultHandler(Activity activity, ParsedResult result) {
    this(activity, result, null);
  }

  ResultHandler(Activity activity, ParsedResult result, Result rawResult) {
	  mResult = result;
	  mActivity = activity;
  }

  ParsedResult getResult() {
    return mResult;
  }

  public abstract int getButtonCount();
  public abstract int getButtonText(int index);
  public abstract void handleButtonPress(int index);
  
  public CharSequence getDisplayContents() {
    String contents = mResult.getDisplayResult();
    return contents.replace("\r", "");
  }

  public abstract int getDisplayTitle();

  public final ParsedResultType getType() {
    return mResult.getType();
  }

  void launchIntent(Intent intent) {
    if (intent != null) {
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      try {
    	  mActivity.startActivity(intent);
      } catch (ActivityNotFoundException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(ZRes.app_name);
        builder.setMessage("Sorry, the requested application could not be launched. The barcode contents may be invalid.");
        builder.setPositiveButton(CaptureActivity.DISPLAY_BTN_TEXT_OK, null);
        builder.show();
      }
    }
  }
}