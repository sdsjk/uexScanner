package org.zywx.wbpalmstar.plugin.uexzxing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexscanner.JsConst;

/**
 * Created by zhang on 2018/5/15.
 */

public class ViewTitleBarNav extends RelativeLayout{


    public ViewTitleBarNav(Context context) {
        this(context,null);
    }

    public ViewTitleBarNav(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ViewTitleBarNav(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView( context);
    }

    private void initView(Context context) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View titleView=inflater.inflate(EUExUtil.getResLayoutID("layout_view_titile_barnav"),null);
        ImageView leftView= (ImageView) titleView.findViewById(EUExUtil.getResIdID("plugin_uexscanner_left"));
        leftView.setClickable(true);
        Drawable scan_gallery_on = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_cancle_pressed"));
        Drawable scan_gallery_off = mContext.getResources().getDrawable(EUExUtil.getResDrawableID("plugin_scanner_cancel_normal"));
        StateListDrawable gallery_style = new StateListDrawable();
        gallery_style.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, scan_gallery_on);
        gallery_style.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, scan_gallery_on);
        gallery_style.addState(new int[] { android.R.attr.state_enabled }, scan_gallery_off);
        leftView.setBackgroundDrawable(gallery_style);

        leftView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ((Activity) getContext()).setResult(Activity.RESULT_CANCELED);
                ((Activity) getContext()).finish();
            }
        });

        TextView rightView= (TextView) titleView.findViewById(EUExUtil.getResIdID("plugin_uexscanner_right"));



        rightView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ((Activity) mContext).startActivityForResult(i, JsConst.START_IMAGE_INTENT);
            }
        });
        setAlpha(0.7f);
        addView(titleView);
    }
}
