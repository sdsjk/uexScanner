package org.zywx.wbpalmstar.plugin.uexzxing.client.android;

import android.content.Context;
import android.content.res.Resources;

public class ZRes{

	public static int plugin_scan_bg;
	public static int plugin_scan_lig_off;
	public static int plugin_scan_lig_on;
	public static int plugin_scan_cancel_off;
	public static int plugin_scan_cancel_on;
	public static int plugin_scan_barcode_cancel_off;
	public static int plugin_scan_barcode_cancel_on;
	public static int plugin_scan_cancel_x_off;
	public static int plugin_scan_cancel_x_on;
	public static int plugin_scan_input_off;
	public static int plugin_scan_input_on;
	public static int plugin_scan_ok_off;
	public static int plugin_scan_ok_on;
	public static int plugin_scan_back;
	public static int plugin_scan_top;
	public static int plugin_scan_botton;
	public static int plugin_scan_lig_show;
	public static int plugin_scan_photo;
	
	public static int beep;
	public static int app_name;
	
	public static boolean init(Context context){
		String packg = context.getPackageName();
		Resources res = context.getResources();
		plugin_scan_bg = res.getIdentifier("plugin_scan_bg", "drawable", packg);
		plugin_scan_lig_off = res.getIdentifier("plugin_scanner_light_normal", "drawable", packg);
		plugin_scan_lig_on = res.getIdentifier("plugin_scanner_light_pressed", "drawable", packg);
		plugin_scan_cancel_off = res.getIdentifier("plugin_scanner_cancel_normal", "drawable", packg);;
		plugin_scan_cancel_on = res.getIdentifier("plugin_scanner_cancel_pressed", "drawable", packg);;
		plugin_scan_barcode_cancel_off = res.getIdentifier("plugin_scan_barcode_cancel_off", "drawable", packg);;
		plugin_scan_barcode_cancel_on = res.getIdentifier("plugin_scan_barcode_cancel_on", "drawable", packg);;
		plugin_scan_cancel_x_off = res.getIdentifier("plugin_scan_cancel_x_off", "drawable", packg);;
		plugin_scan_cancel_x_on = res.getIdentifier("plugin_scan_cancel_x_on", "drawable", packg);;
		plugin_scan_input_off = res.getIdentifier("plugin_scan_input_off", "drawable", packg);;
		plugin_scan_input_on = res.getIdentifier("plugin_scan_input_on", "drawable", packg);;
		plugin_scan_ok_off = res.getIdentifier("plugin_scan_ok_off", "drawable", packg);;
		plugin_scan_ok_on = res.getIdentifier("plugin_scan_ok_on", "drawable", packg);;
		plugin_scan_back = res.getIdentifier("plugin_scan_back", "drawable", packg);
		plugin_scan_top = res.getIdentifier("plugin_scan_top", "drawable", packg);
		plugin_scan_botton = res.getIdentifier("plugin_scan_botton", "drawable", packg);
		plugin_scan_lig_show = res.getIdentifier("plugin_scanner_light_show", "drawable", packg);
		plugin_scan_photo = res.getIdentifier("plugin_scanner_photo", "drawable", packg);
		beep = res.getIdentifier("beep", "raw", packg);
		app_name = res.getIdentifier("app_name", "string", packg);
		
		if(plugin_scan_bg == 0 
				|| plugin_scan_lig_off == 0 
				|| plugin_scan_lig_on == 0
				|| plugin_scan_cancel_off == 0
				|| plugin_scan_cancel_on == 0
				|| plugin_scan_barcode_cancel_off == 0
				|| plugin_scan_barcode_cancel_on == 0
				|| plugin_scan_cancel_x_off == 0
				|| plugin_scan_cancel_x_on == 0
				|| plugin_scan_input_off == 0
				|| plugin_scan_input_on == 0
				|| plugin_scan_ok_off == 0
				|| plugin_scan_ok_on == 0
				|| plugin_scan_back == 0
				|| plugin_scan_top == 0
				|| plugin_scan_botton == 0
				|| plugin_scan_lig_show == 0
				|| plugin_scan_photo == 0
				|| beep == 0 
				|| app_name == 0){
			return false;
		}
		
		return true;
	}
}
