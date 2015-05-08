package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import org.zywx.wbpalmstar.plugin.uexzxing.client.android.camera.CameraManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ViewToolViewForBarcode extends RelativeLayout {

	private ImageView mBtnLight, mBtnText;
	private Context mContext;
	private DisplayMetrics dispm;
	private int dis;
	private static int RESULT_LOAD_IMAGE = 1;

	public ViewToolViewForBarcode(Context context) {
		super(context);
		mContext = context;
		dispm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dispm);
		dis = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, dispm);
		init();
	}

	private void init() {
		setBackgroundColor(Color.parseColor("#55494949"));

		mBtnLight = new ImageView(mContext);
		mBtnLight.setClickable(true);
		mBtnLight.setBackgroundResource(ZRes.plugin_scan_lig_show);

		mBtnText = new ImageView(mContext);
		mBtnText.setClickable(true);
		mBtnText.setBackgroundResource(ZRes.plugin_scan_photo);

		LayoutParams parml = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		parml.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		parml.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		parml.setMargins(dis, dis, 0, dis);
		mBtnLight.setLayoutParams(parml);

		LayoutParams parmc = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parmc.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		parmc.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		parmc.setMargins(0, 0, dis, dis);
		mBtnText.setLayoutParams(parmc);

		addView(mBtnLight);
		addView(mBtnText);

		mBtnText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				 ((Activity)mContext).startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});
		mBtnLight.setOnClickListener(new OnClickListener() {
			boolean on;

			@Override
			public void onClick(View v) {
				CameraManager cmg = CameraManager.get();
				if (!cmg.suportFlashlight()) {
					Toast.makeText(mContext, "您的设备不支持闪光灯", Toast.LENGTH_SHORT).show();
					return;
				}
				if (on) {
					cmg.disableFlashlight();
					on = false;
				} else {
					cmg.enableFlashlight();
					on = true;
				}
			}
		});
	}
}
