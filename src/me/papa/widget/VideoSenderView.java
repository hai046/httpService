package me.papa.widget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import me.papa.managers.HttpServerManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.RelativeLayout;

public class VideoSenderView extends RelativeLayout implements Callback {

    private static final String TAG = "VideoSenderView";

    private SurfaceHolder mSurfaceHolder;

    private SurfaceView mSurfaceView;

    private Camera mServiceCamera;

    public boolean mRecordingStatus;

    private MediaRecorder mMediaRecorder;

    private File directory;

    private Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FLAG_INIT_SIZE:
                    initAdjustView();
                    break;

                default:
                    break;
            }
        };
    };

    private static final int FLAG_INIT_SIZE = 1;

    public VideoSenderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public VideoSenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VideoSenderView(Context context) {
        super(context);
        init();
    }

    RelativeLayout.LayoutParams layoutParams;

    private void init() {
        mSurfaceView = new SurfaceView(getContext());

        //        initAdjustView();
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        handler.sendEmptyMessageDelayed(FLAG_INIT_SIZE, 200);
    }

    private void initAdjustView() {
        handler.removeMessages(FLAG_INIT_SIZE);
        //        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        this.mOutputHeight = getHeight();
        this.mOutputWidth = getWidth();

        if (mOutputHeight <= 0 || mOutputWidth <= 0) {
            handler.sendEmptyMessageDelayed(FLAG_INIT_SIZE, 100);
            return;
        }

        int[] ss = getScaleSize(240, 320);
        layoutParams = new RelativeLayout.LayoutParams(ss[0], ss[1]);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        removeAllViews();
        Log.e(TAG, "layoutParams" + layoutParams.width + "   " + layoutParams.height
                + "   mOutputWidth=" + mOutputWidth + "   mOutputHeight=" + mOutputHeight);
        addView(mSurfaceView, layoutParams);

    }

    private int[] getScaleSize(int width, int height) {
        float newWidth;
        float newHeight;

        float widthRatio = (float) width / mOutputWidth;
        float heightRatio = (float) height / mOutputHeight;

        boolean adjustWidth = true;
        adjustWidth = widthRatio > heightRatio;
        Log.e(TAG, "getScaleSize   widthRatio=" + widthRatio + "   heightRatio=" + heightRatio);
        if (!adjustWidth) {
            newHeight = mOutputHeight;
            newWidth = height / heightRatio;// (newHeight / height) * width;
        } else {
            newWidth = mOutputWidth;
            newHeight = height / widthRatio;//(newWidth / width) * height;
        }
        return new int[] { Math.round(newWidth), Math.round(newHeight) };
    }

    @SuppressLint("NewApi")
    public void startRecording() {
        new AsyncTask<Void, Void, Void>() {

            protected void onPreExecute() {
                try {
                    mMediaRecorder = new MediaRecorder();
                    mRecordingStatus = true;
                    mServiceCamera = Camera.open();
                    mServiceCamera.setDisplayOrientation(90);
                    Camera.Parameters params = mServiceCamera.getParameters();
                    mServiceCamera.setParameters(params);
                    Camera.Parameters p = mServiceCamera.getParameters();
                    p.set("orientation", "landscape");
                    mServiceCamera.setParameters(p);
                    mServiceCamera.unlock();
                    mMediaRecorder.setCamera(mServiceCamera);
                    //            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                    mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
                    mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    //            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                    //            mMediaRecorder.setVideoEncodingBitRate(100 * 1024);
                    mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//100k   //200x164
                    List<int[]> fpss = params.getSupportedPreviewFpsRange();
                    for (int[] fps : fpss) {
                        Log.e(TAG, "fps=" + fps[0] + "--" + fps[1]);
                    }
                    mMediaRecorder.setVideoEncodingBitRate(100 * 1024);
                    List<Integer> frs = params.getSupportedPreviewFrameRates();
                    if (frs != null && !frs.isEmpty()) {
                        int rate = Math.min(frs.get(0), frs.get(frs.size() - 1));
                        Log.e(TAG, "min frame rate=" + rate);
                        mMediaRecorder.setVideoFrameRate(rate);
                    }
                    mMediaRecorder.setVideoSize(320, 240);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            protected void onPostExecute(Void result) {

                mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
                mMediaRecorder.setOrientationHint(90);
                try {
                    mMediaRecorder.prepare();
                    mMediaRecorder.start();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            protected Void doInBackground(Void... pam) {

                Log.v(TAG, "recording service starting..");
                try {

                    //                    directory = new File(Environment.getExternalStorageDirectory().toString()
                    //                            + "/aa/");
                    //                    if (!directory.exists()) directory.mkdirs();
                    //
                    //                    long currentTime = System.currentTimeMillis();
                    //                    String uniqueOutFile = Environment.getExternalStorageDirectory().toString()
                    //                            + "/aa/videooutput" + currentTime + ".mp4";
                    //                    File outFile = new File(directory, uniqueOutFile);
                    //                    if (outFile.exists()) {
                    //                        outFile.delete();
                    //                    }
                    //
                    //                    mMediaRecorder.setOutputFile(uniqueOutFile);

                    Socket receiver = new Socket(InetAddress.getByName("localhost"),
                            HttpServerManager.port);
                    
                    ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(receiver);
                    mMediaRecorder.setOutputFile(pfd.getFileDescriptor());

                } catch (IllegalStateException e) {

                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();
    }

    class PapaFileOutputStream extends FileOutputStream {

        public PapaFileOutputStream(String path) throws FileNotFoundException {
            super(path);
        }

        @Override
        public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
            Log.e(TAG, "write  byteOffset=" + byteOffset + "   byteCount=" + byteCount);
            super.write(buffer, byteOffset, byteCount);
        }

        @Override
        public void write(int oneByte) throws IOException {
            Log.e(TAG, "write  oneByte=" + oneByte);
            super.write(oneByte);
        }

    }

    private int mOutputWidth, mOutputHeight;

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int width, int height) {
        Log.e(TAG, "arg1=" + arg1 + "  width= " + width + "   height=" + height);
        this.mOutputHeight = height;
        this.mOutputWidth = width;
        startRecording();

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (mServiceCamera != null) {
            stopRecording();
            mServiceCamera.release();
        }
        mRecordingStatus = false;
    }

    public void stopRecording() {

        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        handler.removeMessages(FLAG_INIT_SIZE);
        Log.v(TAG, "recording service stopped");
    }

    public void relase() {
        //        stopRecording();

    }
}
