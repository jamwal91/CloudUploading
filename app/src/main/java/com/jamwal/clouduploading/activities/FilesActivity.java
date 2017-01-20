package com.jamwal.clouduploading.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.jamwal.clouduploading.DbxClient.DropboxClientFactory;
import com.jamwal.clouduploading.DbxClient.PicassoClient;
import com.jamwal.clouduploading.R;
import com.jamwal.clouduploading.adapters.FilesAdapters;
import com.jamwal.clouduploading.asynctasks.CreateSharedLinkTask;
import com.jamwal.clouduploading.asynctasks.DeleteFileTask;
import com.jamwal.clouduploading.asynctasks.DownloadFileTask;
import com.jamwal.clouduploading.asynctasks.GetFilesDetailsTask;
import com.jamwal.clouduploading.asynctasks.RenameFileTask;
import com.jamwal.clouduploading.asynctasks.SharedLinkTask;
import com.jamwal.clouduploading.asynctasks.UploadFileTasks;
import com.jamwal.clouduploading.interfaces.CreateSharedLinkCallback;
import com.jamwal.clouduploading.interfaces.DeleteFileCallback;
import com.jamwal.clouduploading.interfaces.DownloadFileCallback;
import com.jamwal.clouduploading.interfaces.FilesCallback;
import com.jamwal.clouduploading.interfaces.FilesDetailCallback;
import com.jamwal.clouduploading.interfaces.RenameFileCallback;
import com.jamwal.clouduploading.interfaces.SharedLinkCallback;
import com.jamwal.clouduploading.interfaces.UploadFileCallback;
import com.jamwal.clouduploading.swipe.util.Attributes;

import java.io.File;
import java.text.DateFormat;
import java.util.List;

public class FilesActivity extends AppCompatActivity {

    private static final String TAG = FilesActivity.class.getName();

    public final static String EXTRA_PATH = "FilesActivity_Path";
    private static final int PICKFILE_REQUEST_CODE = 1;

    private ViewGroup nullParent = null;

    private String mPath;
    private FilesAdapters mFilesAdapter;
    private FileMetadata mSelectedFile;

    public static Intent getIntent(Context context, String path) {
        Intent filesIntent = new Intent(context, FilesActivity.class);
        filesIntent.putExtra(FilesActivity.EXTRA_PATH, path);
        return filesIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String path = getIntent().getStringExtra(EXTRA_PATH);
        mPath = path == null ? "" : path;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_files_data);
        mFilesAdapter = new FilesAdapters(PicassoClient.getPicasso(), new FilesCallback() {
            @Override
            public void onFolderClicked(FolderMetadata folder) {
                startActivity(FilesActivity.getIntent(FilesActivity.this, folder.getPathLower()));
            }

            @Override
            public void onFileClicked(final FileMetadata file) {
                mSelectedFile = file;
                performWithPermissions(FileAction.DOWNLOAD);
            }

            @Override
            public void onRenameFile(String fromName) {
                showDialogToRenameFile(fromName);
            }

            @Override
            public void onDeleteFile(String filePath) {
                showDialogDeleteFile(filePath);
            }

            @Override
            public void onShareFile(String filePath) {
                generateSharedLink(filePath);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mFilesAdapter.setMode(Attributes.Mode.Single);
        recyclerView.setAdapter(mFilesAdapter);

        mSelectedFile = null;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performWithPermissions(FileAction.UPLOAD);
            }
        });

        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_files, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchFilePicker() {
        // Launch intent to pick file for upload
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // This is the result of a call to launchFilePicker
                uploadFile(data.getData().toString());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int actionCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        FileAction action = FileAction.fromCode(actionCode);

        boolean granted = true;
        for (int i = 0; i < grantResults.length; ++i) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                Log.w(TAG, "User denied " + permissions[i] +
                        " permission to perform file action: " + action);
                granted = false;
                break;
            }
        }

        if (granted) {
            performAction(action);
        } else {
            switch (action) {
                case UPLOAD:
                    Toast.makeText(this,
                            "Can't upload file: read access denied. " +
                                    "Please grant storage permissions to use this functionality.",
                            Toast.LENGTH_LONG)
                            .show();
                    break;
                case DOWNLOAD:
                    Toast.makeText(this,
                            "Can't download file: write access denied. " +
                                    "Please grant storage permissions to use this functionality.",
                            Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        }
    }

    private void performAction(FileAction action) {
        switch (action) {
            case UPLOAD:
                launchFilePicker();
                break;
            case DOWNLOAD:
                if (mSelectedFile != null) {
                    downloadFile(mSelectedFile);
                } else {
                    Log.e(TAG, "No file selected to download.");
                }
                break;
            default:
                Log.e(TAG, "Can't perform unhandled file action: " + action);
        }
    }

    protected void loadData() {

        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Loading");
        dialog.show();

        new GetFilesDetailsTask(DropboxClientFactory.getClient(), new FilesDetailCallback() {
            @Override
            public void onFileDetailsSeccess(ListFolderResult result) {
                dialog.dismiss();
                mFilesAdapter.setFiles(result.getEntries());
            }

            @Override
            public void onFileDetailsError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to list folder.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(mPath);
    }

    private void downloadFile(FileMetadata file) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Downloading File...!");
        dialog.show();

        new DownloadFileTask(DropboxClientFactory.getClient(), new DownloadFileCallback() {
            @Override
            public void onDownloadComplete(File result) {
                dialog.dismiss();
                if (result != null) {
                    viewFileInExternalApp(result);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to download file.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(file);

    }

    private void viewFileInExternalApp(File result) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = result.getName().substring(result.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);

        intent.setDataAndType(Uri.fromFile(result), type);

        // Check for a handler first to avoid a crash
        PackageManager manager = getPackageManager();
        List<ResolveInfo> resolveInfo = manager.queryIntentActivities(intent, 0);
        if (resolveInfo.size() > 0) {
            startActivity(intent);
        }
    }

    private void generateSharedLink(final String filePath) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Generating Shared Link...!");
        dialog.show();
        new SharedLinkTask(DropboxClientFactory.getClient(), new SharedLinkCallback() {
            @Override
            public void onSharedLinkGenerated(List<SharedLinkMetadata> result) {
                if (result.size() > 0) {
                    shareLink(result.get(0).getUrl());
                    dialog.dismiss();
                } else {
                    createSharedLink(filePath, dialog);
                }
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Log.e(TAG, "Failed to Generate Shared Link.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(filePath);
    }

    private void createSharedLink(String filePath, final ProgressDialog dialog) {
        new CreateSharedLinkTask(DropboxClientFactory.getClient(), new CreateSharedLinkCallback() {
            @Override
            public void onSharedLinkCreated(String result) {
                dialog.dismiss();
                shareLink(result);
            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();
                Log.e(TAG, "Failed to Generate Shared Link.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(filePath);
    }

    private void shareLink(String shareUrl) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        startActivity(Intent.createChooser(shareIntent, "Share link using"));
    }

    private void renameFile(String fromName, String toName) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Renaming File...!");
        dialog.show();
        new RenameFileTask(DropboxClientFactory.getClient(), new RenameFileCallback() {
            @Override
            public void onRenameSuccess(Metadata result) {
                mFilesAdapter.update(result);
                dialog.dismiss();
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
                dialog.dismiss();
            }
        }).execute(fromName, toName);
    }

    private void showDialogDeleteFile(final String filePath) {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.text_msg_delete))
                .setPositiveButton(getString(R.string.text_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSelectedFile(filePath);
                    }
                })
                .setNegativeButton(getString(R.string.text_cancel), null)
                .create()
                .show();
    }

    private void deleteSelectedFile(String filePath) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage("Deleting File...!");
        dialog.show();
        new DeleteFileTask(DropboxClientFactory.getClient(), new DeleteFileCallback() {
            @Override
            public void onDeleteComplete() {
                dialog.cancel();
                mFilesAdapter.removeFile();
            }

            @Override
            public void onError(Exception e) {
                dialog.cancel();
                Log.e(TAG, "Failed to Generate Shared Link.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(filePath);
    }

    private void uploadFile(String fileUri) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Uploading File...!");
        dialog.show();

        new UploadFileTasks(DropboxClientFactory.getClient(), new UploadFileCallback() {
            @Override
            public void onUploadComplete(FileMetadata result) {
                dialog.dismiss();

                String message = result.getName() + " size " + result.getSize() + " modified " +
                        DateFormat.getDateTimeInstance().format(result.getClientModified());
                Toast.makeText(FilesActivity.this, message, Toast.LENGTH_SHORT)
                        .show();

                mFilesAdapter.addUploadedFile(result);

            }

            @Override
            public void onError(Exception e) {
                dialog.dismiss();

                Log.e(TAG, "Failed to upload file.", e);
                Toast.makeText(FilesActivity.this,
                        "An error has occurred",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }).execute(fileUri, mPath);
    }

    public void showDialogToRenameFile(final String name) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        View mView = layoutInflaterAndroid.inflate(R.layout.layout_edit_text, nullParent);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(this);
        alertDialogBuilderUserInput.setMessage(getString(R.string.text_edit_name));
        alertDialogBuilderUserInput.setView(mView);

        String nameWithoutExt = name.substring(0, name.lastIndexOf("."));

        final EditText et_edit = (EditText) mView.findViewById(R.id.enter_name);
        et_edit.setText(nameWithoutExt);
        et_edit.setSelection(et_edit.getText().length(), et_edit.getText().length());

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(getString(R.string.text_rename), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        if (TextUtils.isEmpty(et_edit.getText().toString().trim())) {
                            et_edit.setText("");
                            et_edit.setError(getString(R.string.text_error));
                        } else {
                            renameFile("/" + name, "/" + et_edit.getText().toString());
                        }
                    }
                })
                .setNegativeButton(getString(R.string.text_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        assert alertDialog.getWindow() != null;
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
    }

    private void performWithPermissions(final FileAction action) {
        if (hasPermissionsForAction(action)) {
            performAction(action);
            return;
        }

        if (shouldDisplayRationaleForAction(action)) {
            new AlertDialog.Builder(this)
                    .setMessage("This app requires storage access to download and upload files.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissionsForAction(action);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            requestPermissionsForAction(action);
        }
    }

    private boolean hasPermissionsForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldDisplayRationaleForAction(FileAction action) {
        for (String permission : action.getPermissions()) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    private void requestPermissionsForAction(FileAction action) {
        ActivityCompat.requestPermissions(
                this,
                action.getPermissions(),
                action.getCode()
        );
    }

    private enum FileAction {
        DOWNLOAD(Manifest.permission.WRITE_EXTERNAL_STORAGE),
        UPLOAD(Manifest.permission.READ_EXTERNAL_STORAGE);

        private static final FileAction[] values = values();

        private final String[] permissions;

        FileAction(String... permissions) {
            this.permissions = permissions;
        }

        public int getCode() {
            return ordinal();
        }

        public String[] getPermissions() {
            return permissions;
        }

        public static FileAction fromCode(int code) {
            if (code < 0 || code >= values.length) {
                throw new IllegalArgumentException("Invalid FileAction code: " + code);
            }
            return values[code];
        }
    }
}
