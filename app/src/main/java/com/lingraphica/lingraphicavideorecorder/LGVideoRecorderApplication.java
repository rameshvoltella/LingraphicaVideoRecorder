package com.lingraphica.lingraphicavideorecorder;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * Created by punit on 2/14/17.
 */

public class LGVideoRecorderApplication extends Application {

    public static final String LOG_TAG = "LG VIDEO";

    public static final String TEMP_VIDEO_OUTPUT = Environment.getExternalStorageDirectory()
            + "/lingraphica/tmp/tempvideo.mp4"; //$NON-NLS-1$

    public static final String VIDEO_OUTPUT = Environment.getExternalStorageDirectory()
            + "/lingraphica/tmp/video_output.mp4";

    public static final String THUMBNAIL_IMAGE = Environment.getExternalStorageDirectory()
            + "/lingraphica/tmp/video_thumbnail.png";

    public boolean isDeviceinLockTaskMode(Context context) {
        Log.i(LOG_TAG, "Checking isInLockTaskMode");
        ActivityManager manager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        return manager.isInLockTaskMode();
    }

    public void enableKioskMode(Context context) {
        if (!isDeviceinLockTaskMode(context)) {
            try {
                Log.i(LOG_TAG, "Enabling LockTask");
                ((Activity) context).startLockTask();
            } catch (Exception e) {
                Log.i(LOG_TAG, "Cannot start Lock Task");
            }
        }
    }

    public void disableKioskMode(Context context) {
        if (isDeviceinLockTaskMode(context)) {
            try {
                Log.i(LOG_TAG, "Disabling LockTask");
                ((Activity) context).stopLockTask();
            } catch (Exception e) {
                Log.i(LOG_TAG, "Cannot stop Lock Task");
            }
        }
    }
}
