package com.jamwal.clouduploading.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.util.List;

public class SharedLinkTask extends AsyncTask<String, Void, List<SharedLinkMetadata>> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public SharedLinkTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(List<SharedLinkMetadata> result) {
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
    protected List<SharedLinkMetadata> doInBackground(String... params) {
        try {
            return mDbxClient.sharing().listSharedLinksBuilder()
                    .withPath(params[0]).withDirectOnly(true).start().getLinks();
        } catch (DbxException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public interface Callback {
        void onUploadComplete(List<SharedLinkMetadata> result);

        void onError(Exception e);
    }
}
