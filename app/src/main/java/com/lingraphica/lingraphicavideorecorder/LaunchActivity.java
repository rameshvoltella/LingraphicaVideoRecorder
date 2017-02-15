package com.lingraphica.lingraphicavideorecorder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by punitraizada on 2/14/17.
 */

public class LaunchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent appIntent = new Intent(this, LGCameraActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(appIntent);
        finish();
    }
}
