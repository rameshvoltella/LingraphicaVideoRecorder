package com.lingraphica.lingraphicavideorecorder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.lingraphica.audio.SoundPlayer;
import com.lingraphica.device.camera.CameraOrientationController;
import com.lingraphica.device.camera.CameraPreview;
import com.lingraphica.util.GraphicsHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

public class LGCameraActivity extends Activity implements Camera.PictureCallback, Camera.ShutterCallback, MediaRecorder.OnInfoListener {

    Context mContext;

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private CameraOrientationController mCameraOrientationController;
    private int mCameraId = -1; // don't know the camera to use at start up.

    private Uri mVideoFileUri;
    private MediaRecorder mediaRecorder;
    private boolean recordingVideoRightNow;
    private String cameraMode;

    final Handler handler = new Handler();
    private static final long FOCUS_DELAY = 1000;

    Runnable camera_auto_focus = new Runnable() {
        public void run() {
            if (null == mCamera) {
                return;
            }
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    handler.postDelayed(camera_auto_focus, FOCUS_DELAY);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_lgcamera);

        mCameraOrientationController = CameraOrientationController.newInstance();
        new File(LGVideoRecorderApplication.TEMP_VIDEO_OUTPUT).delete();
        setupCameraForTakeVideo();

        // handle screen cancellation
        ImageButton backButton = (ImageButton) findViewById(R.id.camera_cancel_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unused")
            @Override
            public void onClick(View v) {
                onVideoTaken(RESULT_CANCELED);
            }
        });

        // Handle camera switch
        findViewById(R.id.camera_switch_camera_button).setOnClickListener(
                new View.OnClickListener() {

                    @SuppressWarnings("unused")
                    @Override
                    public void onClick(View v) {
                        handleCameraSwitch();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ((LGVideoRecorderApplication) getApplication()).enableKioskMode(this);
        startCameraInPreviewMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((LGVideoRecorderApplication) getApplication()).disableKioskMode(this);
        releaseMediaRecorder();
        releaseCamera();
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }

    @Override
    public void onShutter() {
        new SoundPlayer().play(mContext, R.raw.camera_shutter_click);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        // regardless of "what" we want to stop the recording
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
                mr.stop();
                Toast.makeText(mContext,
                        R.string.Unknown_error_while_recording,
                        Toast.LENGTH_SHORT).show();
                ((ImageButton) findViewById(R.id.camera_take_video_button))
                        .setImageResource(R.drawable.take_video);
                onVideoTaken(RESULT_CANCELED);
                break;
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
                mr.stop();
                // Toast.makeText(mContext, "Recording created.",
                // Toast.LENGTH_SHORT).show();
                ((ImageButton) findViewById(R.id.camera_take_video_button))
                        .setImageResource(R.drawable.take_video);
                onVideoTaken(RESULT_OK);
                break;
            default:
                break;
        }
    }

    private void releaseCamera() {
        if (mCameraPreview != null) {
            ((FrameLayout) findViewById(R.id.camera_preview)).removeAllViews();
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
            mCameraPreview = null;
        }

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }

    }

    void handleCameraSwitch() {
        int numOfCam = Camera.getNumberOfCameras();
        if (numOfCam < 2) {
            Toast.makeText(mContext, R.string.Cannot_switch_camera,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        mCamera.stopPreview();
        releaseCamera();
        // switch between 0 and 1
        mCameraId = (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK ? Camera.CameraInfo.CAMERA_FACING_FRONT
                : Camera.CameraInfo.CAMERA_FACING_BACK);
        startCameraInPreviewMode();
    }

    private void setupCameraForTakeVideo() {
        mVideoFileUri = Uri.fromFile(new File(LGVideoRecorderApplication.TEMP_VIDEO_OUTPUT));

        // Handle take video
        findViewById(R.id.camera_take_video_button).setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings({"unused", "synthetic-access"})
            @Override
            public void onClick(View v) {
                if (recordingVideoRightNow) {
                    mCameraPreview.setRecording(false);
                    mediaRecorder.stop();
                    ((ImageButton) findViewById(R.id.camera_take_video_button))
                            .setImageResource(R.drawable.take_video);
                    onVideoTaken(RESULT_OK);
                } else {
                    if (prepareVideoRecorder()) {
                        findViewById(R.id.camera_switch_camera_button)
                                .setEnabled(false);
                        ((ImageButton) findViewById(R.id.camera_take_video_button))
                                .setImageResource(R.drawable.take_video_stop);
                        mediaRecorder.start();
                        recordingVideoRightNow = true;
                        mCameraPreview.setRecording(true);
                    } else {
                        Toast.makeText(mContext,
                                R.string.Unable_to_start_recording,
                                Toast.LENGTH_SHORT).show();
                        releaseMediaRecorder();
                    }
                }
            }
        });
    }

    public void onVideoTaken(int resultCode) {
        releaseMediaRecorder();
        recordingVideoRightNow = false;
        setResult(resultCode);
        ((LGVideoRecorderApplication) getApplication()).disableKioskMode(mContext);
        if (resultCode == RESULT_OK) {
            // copy to output file
            new File(LGVideoRecorderApplication.VIDEO_OUTPUT).delete();
            new File(LGVideoRecorderApplication.TEMP_VIDEO_OUTPUT)
                    .renameTo(new File(LGVideoRecorderApplication.VIDEO_OUTPUT));
            // Thumbnail
            Bitmap bitmap = new GraphicsHelper().getThumbnailImage(new File(LGVideoRecorderApplication.VIDEO_OUTPUT));
            FileOutputStream out;
            try {
                out = new FileOutputStream(LGVideoRecorderApplication.THUMBNAIL_IMAGE);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.getFD().sync();
                out.close();
            } catch (Exception e) {
                Log.e(LGVideoRecorderApplication.LOG_TAG, "Exception while storing thumbnail", e);
            }
        }
        finish();

    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset(); // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
        }

        mCamera.lock(); // lock camera for later use

    }

    private boolean prepareVideoRecorder() {
        mediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setProfile(CamcorderProfile.get(mCameraId,
                CamcorderProfile.QUALITY_HIGH));

        mediaRecorder.setOutputFile(mVideoFileUri.getPath());
        mediaRecorder.setMaxDuration(60000); // Set max duration 60 sec.
        // mediaRecorder.setMaxFileSize(5000000); // Set max file size 5M
        mediaRecorder.setPreviewDisplay(mCameraPreview.getmHolder()
                .getSurface());

        mediaRecorder.setOnInfoListener(this);

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.i(((LGVideoRecorderApplication) getApplication()).LOG_TAG, "Exception in prepare " + e.getMessage()); //NON-NLS
            return false;
        } catch (IOException e) {
            Log.i(((LGVideoRecorderApplication) getApplication()).LOG_TAG, "Exception in prepare " + e.getMessage()); //NON-NLS
            return false;
        }
        return true;

    }

    private void startCameraInPreviewMode() {
        // get a new camera instance
        mCamera = getCameraInstance();

        if (null == mCamera) {
            Toast.makeText(mContext, R.string.Unable_to_start_camera, Toast.LENGTH_SHORT).show();
            return;
        }

        if (null != mCameraOrientationController) {
            mCameraOrientationController.setCameraDisplayOrientation(this, mCamera, mCameraId);
        }

        // setup a new preview object
        mCameraPreview = new CameraPreview(this, mCamera);

        // Set the placement of the preview
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);
        //handler.postDelayed(camera_auto_focus, FOCUS_DELAY);
    }

    private Camera getCameraInstance() {
        Camera c = null;
        try {
            if (-1 == mCameraId) {
                // try to get front facing cam
                int numOfCam = Camera.getNumberOfCameras();
                // disable switch camera button for one cam devices
                if (1 == numOfCam) {
                    findViewById(R.id.camera_switch_camera_button).setEnabled(false);
                }
                //mCameraId = numOfCam - 1;
                mCameraId = 0;
            }

            Log.i(((LGVideoRecorderApplication) getApplication()).LOG_TAG, "Opening Cam with Id: " + mCameraId); //NON-NLS
            c = Camera.open(mCameraId);
            if (null != c) {
                setCameraSettings(c);
            }
        } catch (Exception e) {
            Log.d(((LGVideoRecorderApplication) getApplication()).LOG_TAG, "getCameraInstance " + e.getClass() + " | " + e.getMessage());
        }

        return c; // returns null if camera is unavailable
    }

    @SuppressWarnings("boxing")
    private void setCameraSettings(Camera camera) {
        Camera.Parameters params = camera.getParameters();

        // setting scene mode
        List<String> sceneModes = params.getSupportedSceneModes();
        if (null != sceneModes
                && sceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO)) {
            params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        }

        List<String> focusModes = params.getSupportedFocusModes();
        if (null != focusModes) {
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            if (focusModes.contains("continuous-picture")) { // Dell Android 5.0, API 14 NON-NLS
                params.setFocusMode("continuous-picture"); //NON-NLS
            }
        }


        try {
            Method m = Camera.Parameters.class.getMethod(
                    "setRecordingHint", new Class[]{boolean.class}); //$NON-NLS-1$
            m.invoke(params, true);
        } catch (Exception e) {
            Log.i(((LGVideoRecorderApplication) getApplication()).LOG_TAG, "Cannot set recording hint " + e.getMessage()); //$NON-NLS-1$
        }


        camera.setParameters(params);

    }
}
