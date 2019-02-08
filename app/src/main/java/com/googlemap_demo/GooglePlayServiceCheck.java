package com.googlemap_demo;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * Created by SONU on 22/09/15.
 */
public class GooglePlayServiceCheck {

    /*  Check the existence of Google Play Services  */
    boolean isGooglePlayInstalled(AppCompatActivity appCompatActivity) {
        // Getting status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(appCompatActivity);

        // Showing status
        if (status == ConnectionResult.SUCCESS) {
            return true;
        } else {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, appCompatActivity, requestCode);
            dialog.show();
            return false;
        }
    }
}
