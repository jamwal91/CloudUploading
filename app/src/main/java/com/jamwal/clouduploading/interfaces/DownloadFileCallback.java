package com.jamwal.clouduploading.interfaces;

import java.io.File;

/**
 * Created by jamwal on 04/01/17.
 */

public interface DownloadFileCallback {

    void onDownloadComplete(File result);

    void onError(Exception e);
}
