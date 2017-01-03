package com.jamwal.clouduploading.interfaces;

import com.dropbox.core.v2.files.ListFolderResult;

/**
 * Created by jamwal on 03/01/17.
 */

public interface FilesDetailCallback {

    void onFileDetailsSeccess(ListFolderResult result);

    void onFileDetailsError(Exception e);
}
