package com.lingraphica.lingraphicavideorecorder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class LGCameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lgcamera);


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!((LGVideoRecorderApplication)getApplication()).isDeviceinLockTaskMode(this)){
            try {
                startLockTask();
            }catch(Exception e){
                Log.i(LGVideoRecorderApplication.LOG_TAG, "Cannot start Lock Task");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(((LGVideoRecorderApplication)getApplication()).isDeviceinLockTaskMode(this)){
            try {
                stopLockTask();
            }catch(Exception e){
                Log.i(LGVideoRecorderApplication.LOG_TAG, "Cannot stop Lock Task");
            }
        }
    }
}
