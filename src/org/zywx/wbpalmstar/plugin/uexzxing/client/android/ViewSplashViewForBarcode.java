package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import android.content.Context;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ViewSplashViewForBarcode extends LinearLayout implements AnimationListener {

	private ImageView mIvTop;
	private ImageView mIvBotton;
	private Context mContext;
	private OnFlashListener onFlashListener;

	public ViewSplashViewForBarcode(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public void setOnFlashListener(OnFlashListener onFlashListener) {
		this.onFlashListener = onFlashListener;
	}

	private void init() {
		setOrientation(LinearLayout.VERTICAL);
		mIvTop = new ImageView(mContext);
		mIvTop.setBackgroundResource(ZRes.plugin_scan_top);

		mIvBotton = new ImageView(mContext);
		mIvBotton.setBackgroundResource(ZRes.plugin_scan_botton);

		LayoutParams parml = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		parml.weight = 1;
		mIvTop.setLayoutParams(parml);

		LayoutParams parmc = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		parmc.weight = 1;
		mIvBotton.setLayoutParams(parmc);

		addView(mIvTop);
		addView(mIvBotton);
	}
	
	public void startAnims() {
		AnimationSet upSet = new AnimationSet(true);
		AnimationSet downSet = new AnimationSet(true);
		TranslateAnimation animationUp = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f,
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, -1f);
		AlphaAnimation alphaAnimationUp = new AlphaAnimation(1f, 0f);
		alphaAnimationUp.setDuration(800);
		animationUp.setDuration(1500);
		alphaAnimationUp.setFillAfter(true);
		animationUp.setFillAfter(true);
		upSet.addAnimation(alphaAnimationUp);
		upSet.addAnimation(animationUp);
		upSet.setFillAfter(true);
		alphaAnimationUp.setAnimationListener(this);
		upSet.setStartOffset(1000);
		mIvTop.startAnimation(upSet);
		
		TranslateAnimation animationDown = new TranslateAnimation(
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f,
				Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 1f);
		AlphaAnimation alphaAnimationDown = new AlphaAnimation(1f, 0f);
		alphaAnimationDown.setDuration(800);
		animationDown.setDuration(1500);
		alphaAnimationDown.setFillAfter(true);
		animationDown.setFillAfter(true);
		downSet.addAnimation(alphaAnimationDown);
		downSet.addAnimation(animationDown);
		downSet.setFillAfter(true);
		downSet.setStartOffset(1000);
		mIvBotton.startAnimation(downSet);
		
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if(onFlashListener != null) {
			onFlashListener.onFlash();
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}
	
	public interface OnFlashListener {
		void onFlash();
	}
}
