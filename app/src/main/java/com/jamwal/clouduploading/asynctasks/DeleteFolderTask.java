package com.jamwal.clouduploading.asynctasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

public class DeleteFolderTask extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public DeleteFolderTask(DbxClientV2 dbxClient, Callback callback) {
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
//            mDbxClient.files().permanentlyDelete(params[0]);
            return mDbxClient.files().delete(params[0]);
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }

    public interface Callback {
        void onDeleteComplete();

        void onError(Exception e);
    }
}
