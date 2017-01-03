package com.jamwal.clouduploading.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jamwal.clouduploading.R;
import com.jamwal.clouduploading.login.DropboxLoginActivity;

/**
 * Created by jamwal on 30/12/16.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setClickListener();
    }

    private void setClickListener() {
        findViewById(R.id.login_dropbox).setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            startActivity(new Intent(this, FilesActivity.class));
            finish();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Login Failure...!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_dropbox:
                startActivityForResult(new Intent(this, DropboxLoginActivity.class), 100);
                break;
            default:
                break;
        }
    }
}
