package com.jamwal.clouduploading.application;

import android.app.Application;
import android.os.SystemClock;

/**
 * Created by jamwal on 30/12/16.
 */

public class AppController extends Application {

    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        SystemClock.sleep(3000);
    }

    public static AppController getInstance(){
        return mInstance;
    }
}
