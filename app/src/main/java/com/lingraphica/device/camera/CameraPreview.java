package com.lingraphica.device.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.lingraphica.lingraphicavideorecorder.R;

import java.util.List;

public class CameraPreview extends SurfaceView implements
        SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview"; //NON-NLS
    private SurfaceHolder mHolder;
    private Paint textPaint = new Paint();

    private Camera mCamera;

    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private boolean recording;

    private long videoRecodingStartTime = 0;

    final Handler handler = new Handler();
    Runnable update_video_time = new Runnable() {
        public void run() {
            invalidate();
        }
    };

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // supported preview sizes
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        //mSupportedPreviewSizes = mCamera.getParameters().getSupportedPictureSizes();

//        for(Camera.Size str: mSupportedPreviewSizes) {
//            Log.e(TAG, str.width + "/" + str.height);
//        }


        textPaint.setARGB(255, 200, 0, 0);
        textPaint.setTextSize(60);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public SurfaceHolder getmHolder() {
        return mHolder;
    }

    public void setPreviewCallback(Camera.PreviewCallback callback) {
        // not needed in our application.
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the
        // preview.
        // Surface Changed will also be called .. we will start the preview
        // there.
        setWillNotDraw(false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2,
                               int i3) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        stopCameraPreview();

        // start preview with new settings
        startCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isRecording()) {
            return;
        }

        canvas.drawCircle(60, 60, 50, textPaint);
        canvas.drawText(getContext().getString(R.string.recording_label), 130, 80, textPaint);

        long duration = (int) ((System.currentTimeMillis() - videoRecodingStartTime) / 1000);
        long timeLeft = 60 - duration;
        timeLeft = (timeLeft < 0) ? 0 : timeLeft;
        String text = (timeLeft < 10) ? "0" + timeLeft : timeLeft + "";
        text = "00:" + text;

        canvas.drawText(text, canvas.getWidth()
                - (textPaint.measureText(text) + 10), 80, textPaint);
        handler.postDelayed(update_video_time, 1000);
    }

    private void startCameraPreview() {
        try {
            // set preview size and picture size and make any resize, rotate or reformatting changes here
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
            //mCamera.setDisplayOrientation(90);

            // start preview with new settings
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage()); //NON-NLS
        }
    }

    private void stopCameraPreview() {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error stopping camera preview: " + e.getMessage()); //NON-NLS
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
        if (true) {
            videoRecodingStartTime = System.currentTimeMillis();
            this.invalidate();
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        float ratio;
        if (mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

//        Log.e(TAG, "onMeasure setMeasuredDimension(" + width + ", " + (int)(width/ratio) + ")");
        setMeasuredDimension(width, (int) (width / ratio));
    }

    /**
     * Function taken from http://stackoverflow.com/questions/19577299/android-camera-preview-stretched
     *
     * @param sizes
     * @param w
     * @param h
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }
}
