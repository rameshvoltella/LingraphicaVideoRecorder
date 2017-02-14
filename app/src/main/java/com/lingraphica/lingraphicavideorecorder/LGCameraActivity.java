package com.lingraphica.lingraphicavideorecorder;

import android.app.Activity;
import android.os.Bundle;

public class LGCameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lgcamera);


    }

    @Override
    protected void onResume() {
        super.onResume();
        ((LGVideoRecorderApplication) getApplication()).enableKioskMode(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((LGVideoRecorderApplication) getApplication()).disableKioskMode(this);
    }
}
