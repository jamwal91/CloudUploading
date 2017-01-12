package com.jamwal.clouduploading.interfaces;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;

/**
 * Created by jamwal on 04/01/17.
 */

public interface FilesCallback {
    void onFolderClicked(FolderMetadata folder);

    void onFileClicked(FileMetadata file);

    void onRenameFile(String fromName);

    void onDeleteFile(String filePath);

    void onShareFile();
}
