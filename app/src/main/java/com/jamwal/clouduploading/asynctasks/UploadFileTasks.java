package com.jamwal.clouduploading.asynctasks;

import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.jamwal.clouduploading.application.AppController;
import com.jamwal.clouduploading.helper.UriHelpers;
import com.jamwal.clouduploading.interfaces.UploadFileCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jamwal on 03/01/17.
 */

public class UploadFileTasks extends AsyncTask<String, Void, FileMetadata> {

    private final DbxClientV2 mDbxClient;
    private final UploadFileCallback mCallback;
    private Exception exception;

    public UploadFileTasks(DbxClientV2 dbxClientV2, UploadFileCallback callback) {
        this.mDbxClient = dbxClientV2;
        this.mCallback = callback;
    }

    @Override
    protected FileMetadata doInBackground(String... params) {
        String localUri = params[0];
        File localFile = UriHelpers.getFileForUri(AppController.getInstance(), Uri.parse(localUri));

        if (localFile != null) {
            String remoteFolderPath = params[1];

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            try {
                InputStream inputStream = new FileInputStream(localFile);
                return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);
            } catch (DbxException | IOException e) {
                exception = e;
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(FileMetadata fileMetadata) {
        super.onPostExecute(fileMetadata);

        if (exception != null) {
            mCallback.onError(exception);
        } else if (fileMetadata == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(fileMetadata);
        }
    }
}
