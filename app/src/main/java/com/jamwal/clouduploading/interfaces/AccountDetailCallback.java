package com.jamwal.clouduploading.interfaces;

import com.dropbox.core.v2.users.FullAccount;

/**
 * Created by jamwal on 30/12/16.
 */

public interface AccountDetailCallback {
    void onAccountDetailSuccess(FullAccount account);

    void onAccountDetailFailure(Exception e);
}
