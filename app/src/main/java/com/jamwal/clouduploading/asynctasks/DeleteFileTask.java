package com.jamwal.clouduploading.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.jamwal.clouduploading.interfaces.DeleteFileCallback;

public class DeleteFileTask extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final DeleteFileCallback mCallback;
    private Exception mException;

    public DeleteFileTask(DbxClientV2 dbxClient, DeleteFileCallback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Metadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onDeleteComplete();
        }
    }

    @Override
    protected Metadata doInBackground(String... params) {
        try {
            return mDbxClient.files().delete(params[0]);
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}
