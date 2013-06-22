package me.papa.widget;

import me.papa.managers.HttpServerManager;
import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.widget.VideoView;

public class VideoReceiverView extends VideoView implements Callback {

    private static final String TAG = "VideoReceiverView";

    //    private SurfaceHolder mSurfaceHolder;

    public void startService(String path) {
        //        if (!HttpServerManager.getInstance().isConnected()) {
        //            Log.d(TAG, "HttpServerManager  is not connect");
        //            return;
        //        }
        //        File file = new File("/mnt/sdcard/aa");
        //        String path = "";
        //        if (file.exists()) {
        //            File[] fs = file.listFiles();
        //            path = fs[fs.length - 1].getPath();
        //            Log.e(TAG, "path=" + path);
        //            //            name = fs[fs.length - 1].getName();
        setVideoURI(Uri.parse("http://localhost:" + HttpServerManager.port
                + "/videooutput1371801702402.mp4"));
        //        }
        //        setVideoPath(string);
        start();
    }

    public void relase() {
        stopPlayback();
    }

    public VideoReceiverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public VideoReceiverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoReceiverView(Context context) {
        super(context);
        init();
    }

    private void init() {

        //        mSurfaceHolder = getHolder();
        //        mSurfaceHolder.addCallback(this);

    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {

    }

}
