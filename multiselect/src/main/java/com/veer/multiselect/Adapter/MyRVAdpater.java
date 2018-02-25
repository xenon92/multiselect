package com.veer.multiselect.Adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.veer.multiselect.MultiSelectActivity;
import com.veer.multiselect.R;
import com.veer.multiselect.Util.Constants;
import com.veer.multiselect.Util.CursorRVAdpater;
import com.veer.multiselect.Util.GetPaths;
import com.veer.multiselect.Util.LoadBitmap;

import java.io.File;
import java.util.ArrayList;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

/**
 * Created by Brajendr on 1/5/2017.
 */

public class MyRVAdpater extends CursorRVAdpater<MyRVAdpater.ViewHolder> {
    private Context context;
    private ArrayList<String> paths;
    private ArrayList<Integer> visiblity;

    public MyRVAdpater(Context context, Cursor cursor, ArrayList<Integer> visiblity) {
        super(context, cursor);
        this.context = context;
        this.visiblity = visiblity;
        paths = new ArrayList<>();
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor, final int position) {
        final String path = GetPaths.getPathFromCursor(cursor, MultiSelectActivity.pathType);
        LoadBitmap.loadBitmap(path, viewHolder.ivThumb);
        //set every image as Selected by default
        markSelected(viewHolder, path, position);

        //Handle 'Long Click' action on the image

        //Functionality in default library -
        // - onClick - Image is marked as SELECTED
        // - onLongClick - Not handled - nothing happened

        //Changed functionality -
        // - onClick - Image is marked as SELECTED
        // - onLongClick - Image is opened in default image viewer in the device

        viewHolder.ivThumb.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                File imageFile = new File(path);
                //Handling the FileUriExposedException introduced for android >= N using
                //FileProvider
                Uri imageUri = FileProvider.getUriForFile(context,
                        context.getApplicationContext().getPackageName()
                                + ".provider", imageFile);
                intent.setDataAndType(imageUri, "image/*");
                //Grant permission for the external app to read the file URI from our app
                intent.addFlags(FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
                return false;
            }
        });

        viewHolder.ivThumb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (visiblity.get(position) == View.GONE) {
                    markSelected(viewHolder, path, position);
                } else {
                    viewHolder.rlSelected.setVisibility(View.GONE);
                    paths.remove(path);
                    visiblity.add(position, View.GONE);
                    if (context instanceof MultiSelectActivity) {
                        ((MultiSelectActivity) context).checkSelection(paths);
                    }
                }
            }
        });
    }

    /**
     * Mark the image as Selected
     *
     * @param viewHolder
     * @param path
     * @param position
     */
    private void markSelected(ViewHolder viewHolder, String path, int position) {
        if (paths.size() < MultiSelectActivity.mLimit) {
            viewHolder.rlSelected.setVisibility(View.VISIBLE);
            paths.add(path);
            visiblity.add(position, View.VISIBLE);
            if (context instanceof MultiSelectActivity) {
                ((MultiSelectActivity) context).checkSelection(paths);
            }
        } else {
            Toast.makeText(context, Constants.LIMIT_MESSAGE + paths.size() + " items",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.multiselect_gallery_item, parent, false);

        return new ViewHolder(itemView);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivThumb;
        RelativeLayout rlSelected;

        public ViewHolder(View itemView) {
            super(itemView);
            ivThumb = (ImageView) itemView.findViewById(R.id.thumbImage);
            rlSelected = (RelativeLayout) itemView.findViewById(R.id.layout_image_Sel);
        }
    }
}
