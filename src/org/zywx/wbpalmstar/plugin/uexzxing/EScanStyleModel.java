package org.zywx.wbpalmstar.plugin.uexzxing;

import java.io.Serializable;

@SuppressWarnings("serial")
public class EScanStyleModel implements Serializable {
	private String lineImg;
	private String pickBgImg;
	private String tipLabel;
	private String title;
	
	public EScanStyleModel() {
		super();
	}
	
	public EScanStyleModel(String lineImg, String pickBgImg, String tipLabel,
			String title) {
		super();
		this.lineImg = lineImg;
		this.pickBgImg = pickBgImg;
		this.tipLabel = tipLabel;
		this.title = title;
	}
	public String getLineImg() {
		return lineImg;
	}
	public void setLineImg(String lineImg) {
		this.lineImg = lineImg;
	}
	public String getPickBgImg() {
		return pickBgImg;
	}
	public void setPickBgImg(String pickBgImg) {
		this.pickBgImg = pickBgImg;
	}
	public String getTipLabel() {
		return tipLabel;
	}
	public void setTipLabel(String tipLabel) {
		this.tipLabel = tipLabel;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}
