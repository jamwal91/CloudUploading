package com.jamwal.clouduploading.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

public class CreateSharedLinkTask extends AsyncTask<String, Void, String> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public CreateSharedLinkTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
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
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return mDbxClient.sharing().createSharedLinkWithSettings(params[0]).getUrl();
        } catch (DbxException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public interface Callback {
        void onUploadComplete(String result);

        void onError(Exception e);
    }
}
