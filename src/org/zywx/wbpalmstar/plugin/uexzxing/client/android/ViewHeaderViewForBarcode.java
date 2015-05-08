package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewHeaderViewForBarcode extends RelativeLayout {

	private ImageView mBtnBack;
	private TextView mTvText;
	private Context mContext;
	private DisplayMetrics dispm;
	private int dis;
	private String title = "扫一扫";

	public ViewHeaderViewForBarcode(Context context, String title) {
		super(context);
		mContext = context;
		dispm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dispm);
		dis = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, dispm);
		if(title != null && !"".equalsIgnoreCase(title)) {
			this.title = title;
		}
		init();
	}

	private void init() {
		setBackgroundColor(Color.parseColor("#55494949"));
		mBtnBack = new ImageView(mContext);
		mTvText = new TextView(mContext);

		mBtnBack.setBackgroundResource(ZRes.plugin_scan_back);

		mTvText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		mTvText.setTextColor(0xFFFFFFFF);
		mTvText.setText(title);

		LayoutParams parml = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		parml.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		parml.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		parml.setMargins(dis, dis, 0, dis);
		mBtnBack.setLayoutParams(parml);

		LayoutParams parmc = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		parmc.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		parmc.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		mTvText.setLayoutParams(parmc);

		addView(mBtnBack);
		addView(mTvText);
		
		mBtnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((Activity) getContext()).setResult(Activity.RESULT_CANCELED);
				((Activity) getContext()).finish();
			}
		});
	}
}
