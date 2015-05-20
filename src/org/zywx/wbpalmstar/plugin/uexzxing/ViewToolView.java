package org.zywx.wbpalmstar.plugin.uexzxing;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.camera.CameraManager;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

public class ViewToolView extends RelativeLayout {

    private ImageButton mBtnCancel;
    private ImageView mBtnLight;
    private TextView mTitle;
    private Context mContext;
    private DataJsonVO mData;
    
    public ViewToolView(Context context, DataJsonVO data) {
        super(context);
        this.mData = data;
        this.mContext = context;
        init();
    }

    @SuppressWarnings("deprecation")
    private void init(){
        setBackgroundResource(Color.TRANSPARENT);
        mTitle = new TextView(mContext);
        mTitle.setBackgroundDrawable(null);
        mTitle.setTextColor(Color.WHITE);
        mTitle.setText(mData.getTitle());
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);

        mBtnLight = new ImageView(mContext);
        mBtnLight.setClickable(true);
        Drawable scan_light_on = mContext.getResources()
                .getDrawable(EUExUtil.getResDrawableID("plugin_scanner_light_pressed"));
        Drawable scan_light_off = mContext.getResources()
                .getDrawable(EUExUtil.getResDrawableID("plugin_scanner_light_normal"));
        StateListDrawable light_style = new StateListDrawable();
        light_style.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, scan_light_on);
        light_style.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, scan_light_on);
        light_style.addState(new int[]{android.R.attr.state_enabled}, scan_light_off);
        mBtnLight.setBackgroundDrawable(light_style);
        
        mBtnCancel = new ImageButton(mContext);
        mBtnCancel.setClickable(true);
        Drawable scan_cancel_on = mContext.getResources()
                .getDrawable(EUExUtil.getResDrawableID("plugin_scanner_cancel_pressed"));
        Drawable scan_cancel_off = mContext.getResources()
                .getDrawable(EUExUtil.getResDrawableID("plugin_scanner_cancel_normal"));
        StateListDrawable cancel_style = new StateListDrawable();
        cancel_style.addState(new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, scan_cancel_on);
        cancel_style.addState(new int[]{android.R.attr.state_enabled, android.R.attr.state_focused}, scan_cancel_on);
        cancel_style.addState(new int[]{android.R.attr.state_enabled}, scan_cancel_off);
    
        mBtnCancel.setBackgroundDrawable(cancel_style);

        LayoutParams parmh = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        parmh.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        mTitle.setLayoutParams(parmh);
        
        LayoutParams parml = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        parml.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mBtnLight.setLayoutParams(parml);
        
        LayoutParams parmc = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        mBtnCancel.setLayoutParams(parmc);

        
        addView(mTitle);
        addView(mBtnLight);
        addView(mBtnCancel);
        
        mBtnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)getContext()).setResult(Activity.RESULT_CANCELED);
                ((Activity)getContext()).finish();
            }
        });

        mBtnLight.setOnClickListener(new OnClickListener() {
            boolean on;
            @Override
            public void onClick(View v) {
                CameraManager cmg = CameraManager.getInstance(mContext);
                if(!cmg.suportFlashlight()){
                    Toast.makeText(mContext,
                            EUExUtil.getResStringID("plugin_uexscanner_not_support_flash"),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if(on){
                    cmg.setTorch(false);
                    on = false;
                    mBtnLight.setBackgroundResource(
                            EUExUtil.getResDrawableID("plugin_scanner_light_normal"));
                }else{
                    cmg.setTorch(true);
                    mBtnLight.setBackgroundResource(
                            EUExUtil.getResDrawableID("plugin_scanner_light_pressed"));
                    on = true;
                }
            }
        });
    }
}
