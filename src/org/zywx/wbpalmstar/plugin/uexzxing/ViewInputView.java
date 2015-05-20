package org.zywx.wbpalmstar.plugin.uexzxing;

import org.zywx.wbpalmstar.engine.ESystemInfo;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewInputView extends RelativeLayout {

	private EditText mText;
	private Context mContext;

	public ViewInputView(Context context) {
		super(context);
		mContext = context;
		setBackgroundColor(0xFF808080);
		init();
	}

	private void init() {
		RelativeLayout ipt = new RelativeLayout(mContext);
		LayoutParams parmi = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		parmi.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		ipt.setLayoutParams(parmi);

		TextView lable = new TextView(mContext);
		lable.setId(0x111101);
		lable.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		lable.setText("请输入条码");
		LayoutParams parml = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		parml.leftMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10,
				ESystemInfo.getIntence().mDisplayMetrics);
		lable.setLayoutParams(parml);

		mText = new EditText(mContext);
		mText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
		mText.setLines(3);

		GradientDrawable grade = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
						0xFFFFFFFF, 0xFFFFFFFF });
		int width = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 3,
				ScannerUtils.getDisplayMetrics(mContext));
		int pading = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 5,
				ScannerUtils.getDisplayMetrics(mContext));
		grade.setStroke(width, 0xAA52565E, width, 0);
		grade.setCornerRadius(8);
		mText.setPadding(pading, width, pading, pading);
		mText.setBackgroundDrawable(grade);
		// mText.setBackgroundResource(ZRes.plugin_scan_edit_style);

		LayoutParams parmt = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		parmt.leftMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10,
				ESystemInfo.getIntence().mDisplayMetrics);
		parmt.rightMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10,
				ESystemInfo.getIntence().mDisplayMetrics);
		parmt.topMargin = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 10,
				ESystemInfo.getIntence().mDisplayMetrics);
		parmt.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		parmt.addRule(RelativeLayout.BELOW, 0x111101);
		mText.setLayoutParams(parmt);

		ViewToolBar bar = new ViewToolBar(mContext);
		LayoutParams parmb = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);
		parmb.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		bar.setLayoutParams(parmb);

		ipt.addView(lable);
		ipt.addView(mText);
		addView(ipt);
		addView(bar);
	}

	public class ViewToolBar extends RelativeLayout {

		private ImageView mBtnCancel;
		private ImageView mBtnOk;

		public ViewToolBar(Context context) {
			super(context);
			init(context);
		}

		private void init(Context context) {
			setBackgroundResource(EUExUtil.getResDrawableID("plugin_scan_bg"));
			mBtnOk = new ImageView(context);
			mBtnOk.setClickable(true);

			Drawable scan_ok_on = mContext.getResources().getDrawable(
					EUExUtil.getResDrawableID("plugin_scan_ok_on"));
			Drawable scan_ok_off = mContext.getResources().getDrawable(
					EUExUtil.getResDrawableID("plugin_scan_ok_off"));
			StateListDrawable scan_ok_style = new StateListDrawable();
			scan_ok_style.addState(new int[] { android.R.attr.state_pressed,
					android.R.attr.state_enabled }, scan_ok_on);
			scan_ok_style.addState(new int[] { android.R.attr.state_enabled,
					android.R.attr.state_focused }, scan_ok_on);
			scan_ok_style.addState(new int[] { android.R.attr.state_enabled },
					scan_ok_off);
			mBtnOk.setBackgroundDrawable(scan_ok_style);
			// mBtnOk.setBackgroundResource(ZRes.plugin_scan_ok_style);

			mBtnCancel = new ImageView(context);
			mBtnCancel.setClickable(true);
			Drawable scan_cancel_x_on = mContext.getResources().getDrawable(
					EUExUtil.getResDrawableID("plugin_scan_cancel_x_on"));
			Drawable scan_cancel_x_off = mContext.getResources().getDrawable(
					EUExUtil.getResDrawableID("plugin_scan_cancel_x_off"));
			StateListDrawable scan_cancel_x_style = new StateListDrawable();
			scan_cancel_x_style.addState(
					new int[] { android.R.attr.state_pressed,
							android.R.attr.state_enabled }, scan_cancel_x_on);
			scan_cancel_x_style.addState(
					new int[] { android.R.attr.state_enabled,
							android.R.attr.state_focused }, scan_cancel_x_on);
			scan_cancel_x_style.addState(
					new int[] { android.R.attr.state_enabled },
					scan_cancel_x_off);
			mBtnCancel.setBackgroundDrawable(scan_cancel_x_style);
			// mBtnCancel.setBackgroundResource(ZRes.plugin_scan_cancel_x_style);

			LayoutParams parmh = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			parmh.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			parmh.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			mBtnOk.setLayoutParams(parmh);

			LayoutParams parmc = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			parmc.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
					RelativeLayout.TRUE);
			parmc.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
			mBtnCancel.setLayoutParams(parmc);

			addView(mBtnOk);
			addView(mBtnCancel);

			mBtnCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					((Activity) getContext())
							.setResult(Activity.RESULT_CANCELED);
					((Activity) getContext()).finish();
				}
			});
			mBtnOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(((Activity) getContext())
							.getIntent().getAction());
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					intent.putExtra(EUExCallback.F_JK_CODE, mText.getText()
							.toString());
					intent.putExtra(EUExCallback.F_JK_TYPE, "");
					((Activity) getContext()).setResult(Activity.RESULT_OK,
							intent);
					((Activity) getContext()).finish();
				}
			});
		}

	}
}
