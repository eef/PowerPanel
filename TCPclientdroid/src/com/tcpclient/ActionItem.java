package com.tcpclient;

import android.graphics.drawable.Drawable;
import android.view.View.OnClickListener;

public class ActionItem {
	private Drawable icon;
	private String title;
	private OnClickListener listener;

	public ActionItem() {}

	public ActionItem(Drawable icon) {
		this.icon = icon;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	
	public Drawable getIcon() {
		return this.icon;
	}
	
	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}
	
	public OnClickListener getListener() {
		return this.listener;
	}
}