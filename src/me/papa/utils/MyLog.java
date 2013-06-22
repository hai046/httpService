package me.papa.utils;


import android.util.Log;

public class MyLog {
	public static boolean flag = true;

	public static void E(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.e(tag, msg);
	}

	public static void D(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.d(tag, msg);
	}

	public static void I(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.i(tag, msg);
	}

	public static void W(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.w(tag, msg);
	}

	public static void V(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.v(tag, msg);

	}

	public static void e(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.e(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.d(tag, msg);
	}

	public static void d(String tag, Object... msg) {
		if (tag == null || msg == null) {
			return;
		}
		
		if (flag)
		{
			String str="";
			for(int i=0;i<msg.length;i++)
			{
				str+=msg[i];
			}
			Log.d(tag, str);
		}
	}

	public static void i(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.i(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.w(tag, msg);
	}

	public static void v(String tag, String msg) {
		if (tag == null || msg == null) {
			return;
		}
		if (flag)
			Log.v(tag, msg);

	}

}
