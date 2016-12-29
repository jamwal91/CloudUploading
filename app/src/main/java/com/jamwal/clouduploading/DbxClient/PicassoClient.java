package com.jamwal.clouduploading.DbxClient;

import android.content.Context;

import com.dropbox.core.v2.DbxClientV2;
import com.jamwal.clouduploading.handler.FileThumbnailRequestHandler;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by jamwal on 30/12/16.
 */

public class PicassoClient {

    private static Picasso sPicasso;

    public static void init(Context context, DbxClientV2 dbxClient) {

        // Configure picasso to know about special thumbnail requests
        sPicasso = new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(context))
                .addRequestHandler(new FileThumbnailRequestHandler(dbxClient))
                .build();
    }


    public static Picasso getPicasso() {
        return sPicasso;
    }
}
