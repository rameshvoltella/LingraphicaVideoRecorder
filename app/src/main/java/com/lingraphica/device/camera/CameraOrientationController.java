package com.lingraphica.device.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Build;
import android.view.Surface;

/**
 * This class is to control the preview orientation of the camera
 */
public class CameraOrientationController {
    private OrientationController mController;

    // the controller instance can only be obtained through object factory
    private CameraOrientationController() {
        mController = new OrientationController();
    }

    public static CameraOrientationController newInstance() {
        // the function in this class is only supported for GINGERBREAD and
        // above
        // disable the class if the system OS version is below GINGERBREAD
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            try {
                return new CameraOrientationController();
            } catch (Throwable e) {
            }
        }
        return null;
    }

    public void setCameraDisplayOrientation(Activity activity, Camera camera,
                                            int cameraId) {
        if (mController != null)
            mController.setCameraDisplayOrientation(activity, camera, cameraId);
    }

    private class OrientationController {

        public OrientationController() {

        }

        public void setCameraDisplayOrientation(Activity activity,
                                                Camera camera, int cameraId) {
            int rotation = activity.getWindowManager().getDefaultDisplay()
                    .getRotation();
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);

            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360; // compensate the mirror
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            camera.setDisplayOrientation(result);
        }
    }
}
