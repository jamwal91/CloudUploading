package com.jamwal.clouduploading.asynctasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;
import com.jamwal.clouduploading.interfaces.RenameFileCallback;

/**
 * Async task to list items in a folder
 */
public class RenameFileTask extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final RenameFileCallback mCallback;
    private Exception mException;

    public RenameFileTask(DbxClientV2 dbxClient, RenameFileCallback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Metadata result) {
        super.onPostExecute(result);

        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onRenameSuccess(result);
        }
    }

    @Override
    protected Metadata doInBackground(String... params) {
        try {
            return mDbxClient.files().move(params[0], params[1]);
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}
