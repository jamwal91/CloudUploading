package com.jamwal.clouduploading.asynctasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;
import com.jamwal.clouduploading.interfaces.FilesDetailCallback;

import static com.dropbox.core.android.AuthActivity.result;

/**
 * Created by jamwal on 03/01/17.
 */

public class GetFilesDetailsTask extends AsyncTask<String, Void, ListFolderResult> {

    private final DbxClientV2 mDbxClient;
    private final FilesDetailCallback mCallback;
    private Exception mException;

    public GetFilesDetailsTask(DbxClientV2 mDbxClient, FilesDetailCallback callback) {
        this.mDbxClient = mDbxClient;
        this.mCallback = callback;
    }

    @Override
    protected ListFolderResult doInBackground(String... params) {
        try {
            return mDbxClient.files().listFolder(params[0]);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }

    @Override
    protected void onPostExecute(ListFolderResult listFolderResult) {
        super.onPostExecute(listFolderResult);

        if (mException != null) {
            mCallback.onFileDetailsError(mException);
        } else {
            mCallback.onFileDetailsSeccess(listFolderResult);
        }
    }
}
