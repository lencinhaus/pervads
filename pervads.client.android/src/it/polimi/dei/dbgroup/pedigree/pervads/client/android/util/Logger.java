package it.polimi.dei.dbgroup.pedigree.pervads.client.android.util;

import it.polimi.dei.dbgroup.pedigree.pervads.client.android.Config;
import android.text.TextUtils;
import android.util.Log;

public class Logger {
	public static final boolean V = Config.LOG && Config.LOGV;
	public static final boolean D = Config.LOG && Config.LOGD;
	public static final boolean I = Config.LOG;
	public static final boolean W = Config.LOG;
	public static final boolean E = Config.LOG;
	
	private String tag;

	public Logger(String tag) {
		if (TextUtils.isEmpty(tag))
			throw new NullPointerException("tag cannot be null or empty");
		this.tag = tag;
	}
	
	public Logger(Class<? extends Object> clazz) {
		this(clazz.getSimpleName());
	}

	public String getTag() {
		return tag;
	}
	
	public void v(String message) {
		if(Config.LOG && Config.LOGV)
			Log.v(tag, message);
	}

	public void d(String message) {
		if (Config.LOG && Config.LOGD)
			Log.d(tag, message);
	}

	public void i(String message) {
		if (Config.LOG)
			Log.i(tag, message);
	}

	public void w(String message) {
		if (Config.LOG)
			Log.w(tag, message);
	}

	public void w(String message, Throwable t) {
		if (Config.LOG)
			Log.w(tag, message, t);
	}

	public void e(String message) {
		if (Config.LOG)
			Log.e(tag, message);
	}

	public void e(String message, Throwable t) {
		if (Config.LOG)
			Log.e(tag, message, t);
	}
}
