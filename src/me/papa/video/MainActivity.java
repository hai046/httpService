package me.papa.video;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import me.papa.managers.HttpServerManager;
import me.papa.widget.VideoReceiverView;
import me.papa.widget.VideoSenderView;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    private VideoSenderView senderView;

    private VideoReceiverView mReceiverView;
    @Override
    protected void onPause() {
        mReceiverView.relase();
        senderView.relase();
        super.onPause();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mReceiverView = (VideoReceiverView) findViewById(R.id.videoReceiverView);
        mReceiverView.startService("/mnt/sdcard/aa/videooutput1371807176674.mp4");
        senderView = (VideoSenderView) findViewById(R.id.videoSenderView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        //        http://10.10.9.139:9000

        return super.onMenuItemSelected(featureId, item);
    }

    private void startNetRequest() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL("http://localhost:" + HttpServerManager.port + "/request.mp4");
                    URLConnection conn = url.openConnection();
                    InputStream is = conn.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    byte[] buffer = new byte[1024];
                    int lg = -1;
                    while ((lg = bis.read(buffer)) != -1) {
                        Log.i("hh", "");
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }
   

}
