package com.jamwal.clouduploading.interfaces;

import com.dropbox.core.v2.files.Metadata;

/**
 * Created by jamwal on 09/01/17.
 */

public interface RenameFileCallback {

    void onRenameSuccess(Metadata result);

    void onError(Exception e);
}
