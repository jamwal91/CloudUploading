package com.jamwal.clouduploading.interfaces;

import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.util.List;

/**
 * Created by jamwal on 20/01/17.
 */

public interface SharedLinkCallback {

    void onSharedLinkGenerated(List<SharedLinkMetadata> result);

    void onError(Exception e);
}
