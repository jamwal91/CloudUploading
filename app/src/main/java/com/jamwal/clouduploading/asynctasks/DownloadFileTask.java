package com.jamwal.clouduploading.asynctasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.jamwal.clouduploading.application.AppController;
import com.jamwal.clouduploading.interfaces.DownloadFileCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by jamwal on 04/01/17.
 */

public class DownloadFileTask extends AsyncTask<FileMetadata, Void, File> {

    private final DbxClientV2 mDbxClient;
    private final DownloadFileCallback mCallback;
    private Exception mException;

    public DownloadFileTask(DbxClientV2 dbxClient, DownloadFileCallback callback) {
        this.mDbxClient = dbxClient;
        this.mCallback = callback;
    }

    @Override
    protected File doInBackground(FileMetadata... params) {
        FileMetadata metadata = params[0];
        try {
            File path = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, metadata.getName());

            // Make sure the Downloads directory exists.
            if (!path.exists()) {
                if (!path.mkdirs()) {
                    mException = new RuntimeException("Unable to create directory: " + path);
                }
            } else if (!path.isDirectory()) {
                mException = new IllegalStateException("Download path is not a directory: " + path);
                return null;
            }

            // Download the file.
            try (OutputStream outputStream = new FileOutputStream(file)) {
                mDbxClient.files().download(metadata.getPathLower(), metadata.getRev())
                        .download(outputStream);
            }

            // Tell android about the file
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            AppController.getInstance().sendBroadcast(intent);

            return file;
        } catch (DbxException | IOException e) {
            mException = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(file);
        }
    }
}
