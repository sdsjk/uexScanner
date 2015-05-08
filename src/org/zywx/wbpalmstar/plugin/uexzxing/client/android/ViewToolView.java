package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import org.zywx.wbpalmstar.plugin.uexzxing.client.android.camera.CameraManager;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ViewToolView extends RelativeLayout {

	private ImageButton mBtnCancel;
	private ImageView mBtnHandler;
	private ImageView mBtnLight;
	private Context mContext;
	
	public ViewToolView(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	@SuppressWarnings("deprecation")
	private void init(){
		setBackgroundResource(0x00000000);
		mBtnHandler = new ImageView(mContext);
		mBtnHandler.setClickable(true);
		
		Drawable scan_input_on = mContext.getResources().getDrawable(ZRes.plugin_scan_input_on);
		Drawable scan_input_off = mContext.getResources().getDrawable(ZRes.plugin_scan_input_off);
		StateListDrawable input_style = new StateListDrawable();
		input_style.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, scan_input_on);
		input_style.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, scan_input_on);
		input_style.addState(new int[]{android.R.attr.state_enabled}, scan_input_off);
		mBtnHandler.setBackgroundDrawable(input_style);
//		mBtnHandler.setBackgroundResource(ZRes.plugin_scan_input_style);
		
		mBtnLight = new ImageView(mContext);
		mBtnLight.setClickable(true);
		Drawable scan_light_on = mContext.getResources().getDrawable(ZRes.plugin_scan_lig_on);
        Drawable scan_light_off = mContext.getResources().getDrawable(ZRes.plugin_scan_lig_off);
        StateListDrawable light_style = new StateListDrawable();
        light_style.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, scan_light_on);
        light_style.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, scan_light_on);
        light_style.addState(new int[]{android.R.attr.state_enabled}, scan_light_off);
		mBtnLight.setBackgroundDrawable(light_style);
		
		mBtnCancel = new ImageButton(mContext);
		mBtnCancel.setClickable(true);
		Drawable scan_cancel_on = mContext.getResources().getDrawable(ZRes.plugin_scan_cancel_on);
		Drawable scan_cancel_off = mContext.getResources().getDrawable(ZRes.plugin_scan_cancel_off);
		StateListDrawable cancel_style = new StateListDrawable();
		cancel_style.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, scan_cancel_on);
		cancel_style.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, scan_cancel_on);
		cancel_style.addState(new int[]{android.R.attr.state_enabled}, scan_cancel_off);
	
		mBtnCancel.setBackgroundDrawable(cancel_style);

//		mBtnCancel.setBackgroundResource(ZRes.plugin_scan_cancel_style);
		
		
		LayoutParams parmh = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parmh.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		parmh.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		mBtnHandler.setLayoutParams(parmh);
		
		LayoutParams parml = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parml.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		//parml.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		mBtnLight.setLayoutParams(parml);
		
		LayoutParams parmc = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		mBtnCancel.setLayoutParams(parmc);

		
	//	addView(mBtnHandler);
		addView(mBtnLight);
		addView(mBtnCancel);
		
		mBtnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((Activity)getContext()).setResult(Activity.RESULT_CANCELED);
				((Activity)getContext()).finish();
			}
		});
		mBtnHandler.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewInputView input = new ViewInputView(mContext);
				((CaptureActivity)mContext).swichInput(input);
			}
		});
		mBtnLight.setOnClickListener(new OnClickListener() {
			boolean on;
			@Override
			public void onClick(View v) {
				CameraManager cmg = CameraManager.get();
				if(!cmg.suportFlashlight()){
					Toast.makeText(mContext, "您的设备不支持闪光灯", Toast.LENGTH_SHORT).show();
					return;
				}
				if(on){
					cmg.disableFlashlight();
					on = false;
					mBtnLight.setBackgroundResource(ZRes.plugin_scan_lig_off);
				}else{
					cmg.enableFlashlight();
					mBtnLight.setBackgroundResource(ZRes.plugin_scan_lig_on);
					on = true;
				}
			}
		});
	}
}
