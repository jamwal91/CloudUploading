package com.jamwal.clouduploading.interfaces;

/**
 * Created by jamwal on 20/01/17.
 */

public interface CreateSharedLinkCallback {
    void onSharedLinkCreated(String result);

    void onError(Exception e);
}
