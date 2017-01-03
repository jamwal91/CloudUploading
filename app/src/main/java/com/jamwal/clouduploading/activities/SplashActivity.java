package com.jamwal.clouduploading.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jamwal.clouduploading.DbxClient.DropboxClientFactory;
import com.jamwal.clouduploading.DbxClient.PicassoClient;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("dropbox-sample", MODE_PRIVATE);

        String accessToken = prefs.getString("access-token", null);
        if (accessToken == null) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            DropboxClientFactory.init(accessToken);
            PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
            startActivity(new Intent(this, FilesActivity.class));
        }
        finish();
    }
}
