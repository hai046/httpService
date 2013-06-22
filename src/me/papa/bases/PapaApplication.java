package me.papa.bases;

import me.papa.managers.HttpServerManager;
import me.papa.utils.Util;
import android.app.Application;
import android.util.Log;

public class PapaApplication extends Application {

    String TAG = "PapaApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        HttpServerManager.init();
        Log.i(TAG, "HttpServer  current phone ip=" + Util.getIpAddress(this));
    }
}
