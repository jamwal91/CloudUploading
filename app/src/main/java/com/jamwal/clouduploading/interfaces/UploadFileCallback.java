package com.jamwal.clouduploading.interfaces;

import com.dropbox.core.v2.files.FileMetadata;

/**
 * Created by jamwal on 03/01/17.
 */

public interface UploadFileCallback {

    void onUploadComplete(FileMetadata result);

    void onError(Exception e);
}
