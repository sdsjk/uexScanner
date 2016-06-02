package org.zywx.wbpalmstar.plugin.uexzxing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.camera.CameraManager;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexscanner.JsConst;

public class ViewToolView extends RelativeLayout {

	private ImageButton mBtnCancel;
	private ImageView mBtnLight;
	private ImageView mGallery;
	private Context mContext;
	private DataJsonVO mData;
	private CaptureActivity.ToolListener mListener;

	public ViewToolView(Context context, DataJsonVO data, CaptureActivity.ToolListener listener) {
		super(context);
		this.mData = data;
		this.mContext = context;
		this.mListener = listener;
		init();
	}

	@SuppressWarnings("deprecation")
	private void init() {
		setBackgroundResource(Color.TRANSPARENT);
		mGallery = new ImageView(mContext);
		mGallery.setClickable(true);
		Drawable scan_gallery_on = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_gallery_pressed"));
		Drawable scan_gallery_off = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_gallery_normal"));
		StateListDrawable gallery_style = new StateListDrawable();
		gallery_style.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, scan_gallery_on);
		gallery_style.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, scan_gallery_on);
		gallery_style.addState(new int[] { android.R.attr.state_enabled }, scan_gallery_off);
		mGallery.setBackgroundDrawable(gallery_style);

		// by waka 是否显示从相册选择的开关
		String isGallery = mData.getIsGallery();
		if (isGallery != null) {
			if (isGallery.equals("0")) {
				mGallery.setVisibility(View.INVISIBLE);
			}
		}

		mBtnLight = new ImageView(mContext);
		mBtnLight.setClickable(true);
		Drawable scan_light_on = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_light_pressed"));
		Drawable scan_light_off = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_light_normal"));
		StateListDrawable light_style = new StateListDrawable();
		light_style.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, scan_light_on);
		light_style.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, scan_light_on);
		light_style.addState(new int[] { android.R.attr.state_enabled }, scan_light_off);
		mBtnLight.setBackgroundDrawable(light_style);

		mBtnCancel = new ImageButton(mContext);
		mBtnCancel.setClickable(true);
		Drawable scan_cancel_on = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_cancel_pressed"));
		Drawable scan_cancel_off = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_cancel_normal"));
		StateListDrawable cancel_style = new StateListDrawable();
		cancel_style.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, scan_cancel_on);
		cancel_style.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, scan_cancel_on);
		cancel_style.addState(new int[] { android.R.attr.state_enabled }, scan_cancel_off);

		mBtnCancel.setBackgroundDrawable(cancel_style);

		LayoutParams parmh = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parmh.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		mBtnLight.setLayoutParams(parmh);

		LayoutParams parml = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parml.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		mGallery.setLayoutParams(parml);

		LayoutParams parmc = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mBtnCancel.setLayoutParams(parmc);

		addView(mGallery);
		addView(mBtnLight);
		addView(mBtnCancel);

		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.hideViewFinder();
				}
				((Activity) getContext()).setResult(Activity.RESULT_CANCELED);
				((Activity) getContext()).finish();
			}
		});

		mBtnLight.setOnClickListener(new OnClickListener() {
			boolean on;

			@Override
			public void onClick(View v) {
				CameraManager cmg = CameraManager.getInstance(mContext);
				if (!cmg.suportFlashlight()) {
					Toast.makeText(mContext, EUExUtil.getResStringID("plugin_uexscanner_not_support_flash"), Toast.LENGTH_SHORT).show();
					return;
				}
				if (on) {
					cmg.setTorch(false);
					on = false;
					mBtnLight.setBackgroundResource(EUExUtil.getResDrawableID("plugin_scanner_light_normal"));
				} else {
					cmg.setTorch(true);
					mBtnLight.setBackgroundResource(EUExUtil.getResDrawableID("plugin_scanner_light_pressed"));
					on = true;
				}
			}
		});
		mGallery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				((Activity) mContext).startActivityForResult(i, JsConst.START_IMAGE_INTENT);
				if (mListener != null) {
					mListener.hideViewFinder();
				}
			}
		});
	}
}
