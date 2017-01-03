package com.jamwal.clouduploading.asynctasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import com.jamwal.clouduploading.interfaces.AccountDetailCallback;

/**
 * Created by jamwal on 30/12/16.
 */

public class GetUserAccountDetailsTask extends AsyncTask<Void, Void, FullAccount> {


    private final DbxClientV2 mDbxClient;
    private final AccountDetailCallback callback;
    private Exception mException;

    public GetUserAccountDetailsTask(DbxClientV2 mDbxClient, AccountDetailCallback callback) {
        this.mDbxClient = mDbxClient;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);
        if (mException != null) {
            callback.onAccountDetailFailure(mException);
        } else {
            callback.onAccountDetailSuccess(account);
        }
    }

    @Override
    protected FullAccount doInBackground(Void... params) {
        try {
            return mDbxClient.users().getCurrentAccount();
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}
