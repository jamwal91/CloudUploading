package com.jamwal.clouduploading.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.jamwal.clouduploading.interfaces.CreateSharedLinkCallback;

public class CreateSharedLinkTask extends AsyncTask<String, Void, String> {

    private final DbxClientV2 mDbxClient;
    private final CreateSharedLinkCallback mCallback;
    private Exception mException;

    public CreateSharedLinkTask(DbxClientV2 dbxClient, CreateSharedLinkCallback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onSharedLinkCreated(result);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return mDbxClient.sharing().createSharedLinkWithSettings(params[0]).getUrl();
        } catch (DbxException ex) {
            mException = ex;
        }
        return null;
    }
}
