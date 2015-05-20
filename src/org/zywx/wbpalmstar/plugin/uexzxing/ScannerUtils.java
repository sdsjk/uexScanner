package org.zywx.wbpalmstar.plugin.uexzxing;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ScannerUtils {
	
	public static DisplayMetrics getDisplayMetrics(Context context){
		DisplayMetrics dism = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dism);
		return dism;
	}

}
