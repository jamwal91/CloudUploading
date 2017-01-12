package com.jamwal.clouduploading.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.jamwal.clouduploading.R;
import com.jamwal.clouduploading.handler.FileThumbnailRequestHandler;
import com.jamwal.clouduploading.interfaces.FilesCallback;
import com.jamwal.clouduploading.swipe.SwipeLayout;
import com.jamwal.clouduploading.swipe.adapters.RecyclerSwipeAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jamwal on 03/01/17.
 */

public class FilesAdapters extends RecyclerSwipeAdapter<FilesAdapters.MetadataViewHolder> {
    private List<Metadata> mFiles;
    private final Picasso mPicasso;
    private final FilesCallback mCallback;

    private int position;

    public void setFiles(List<Metadata> files) {
//        mFiles = Collections.unmodifiableList(new ArrayList<>(files));
        mFiles = files;
        notifyDataSetChanged();
    }

    public void addUploadedFile(FileMetadata metadata) {
        mFiles.add(0, metadata);
        notifyItemInserted(0);
    }

    public void update(Metadata metadata) {
        mItemManger.closeItem(position);
        mFiles.set(position, metadata);
        notifyItemChanged(position);
    }

    public void removeFile() {
        mItemManger.closeItem(position);
        mFiles.remove(position);
        notifyItemRemoved(position);
    }

    public FilesAdapters(Picasso picasso, FilesCallback callback) {
        mPicasso = picasso;
        mCallback = callback;
    }

    @Override
    public MetadataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.files_item, viewGroup, false);
        return new MetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MetadataViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(mFiles.get(i));
    }

    @Override
    public long getItemId(int position) {
        return mFiles.get(position).getPathLower().hashCode();
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    class MetadataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final ImageView mImageView;
        private Metadata mItem;

        private final SwipeLayout swipeLayout;
        private final ImageView iv_more;
        private final ImageView iv_close;
        private final ImageView iv_edit;
        private final ImageView iv_delete;


        MetadataViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image);
            mTextView = (TextView) itemView.findViewById(R.id.text);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            iv_more = (ImageView) itemView.findViewById(R.id.iv_more);
            iv_close = (ImageView) itemView.findViewById(R.id.iv_close);
            iv_edit = (ImageView) itemView.findViewById(R.id.iv_edit);
            iv_delete = (ImageView) itemView.findViewById(R.id.iv_delete);

            itemView.setOnClickListener(this);
            iv_more.setOnClickListener(this);
            iv_close.setOnClickListener(this);
            iv_edit.setOnClickListener(this);
            iv_delete.setOnClickListener(this);

            swipeLayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_more:
                    swipeLayout.open(true, true);
                    break;
                case R.id.iv_close:
                    swipeLayout.close(true, true);
                    break;
                case R.id.iv_edit:
                    position = getAdapterPosition();
                    mCallback.onRenameFile(mFiles.get(getAdapterPosition()).getName());
                    break;
                case R.id.iv_delete:
                    position = getAdapterPosition();
                    mCallback.onDeleteFile(mFiles.get(getAdapterPosition()).getPathDisplay());
                    break;
                default:
                    if (mItem instanceof FolderMetadata) {
                        mCallback.onFolderClicked((FolderMetadata) mItem);
                    } else if (mItem instanceof FileMetadata) {
                        mCallback.onFileClicked((FileMetadata) mItem);
                    }
                    break;
            }


        }

        void bind(Metadata item) {
            mItem = item;
            mTextView.setText(mItem.getName());
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);

            // Load based on file path
            // Prepending a magic scheme to get it to
            // be picked up by DropboxPicassoRequestHandler

            if (item instanceof FileMetadata) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = item.getName().substring(item.getName().indexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);
                if (type != null && type.startsWith("image/")) {
                    mPicasso.load(FileThumbnailRequestHandler.buildPicassoUri((FileMetadata) item))
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .into(mImageView);
                } else {
                    mPicasso.load(R.mipmap.ic_launcher)
                            .noFade()
                            .into(mImageView);
                }
            } else if (item instanceof FolderMetadata) {
                mPicasso.load(R.mipmap.ic_launcher)
                        .noFade()
                        .into(mImageView);
            }
        }
    }
}
