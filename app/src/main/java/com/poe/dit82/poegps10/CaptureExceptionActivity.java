package com.poe.dit82.poegps10;

import android.app.Activity;
import android.os.Bundle;

public class CaptureExceptionActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the default uncaught exception handler. This handler is invoked
        // in case any Thread dies due to an unhandled exception.
        Thread.setDefaultUncaughtExceptionHandler(new CustomizedExceptionHandler(
                Default.PATH_MAIN));

        setContentView(R.layout.activity_main);
    }

}

