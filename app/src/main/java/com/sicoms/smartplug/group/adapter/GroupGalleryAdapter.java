package com.sicoms.smartplug.group.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class GroupGalleryAdapter extends RecyclerView.Adapter<GroupGalleryAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<String> mImageList;
    private OnItemClickListener mItemClickListener;

    public GroupGalleryAdapter(Activity activity) {
        mContext = activity;
        mImageList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupGalleryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_group_gallery, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        String imageName = mImageList.get(position);
        int resId = SPUtil.getDrawableResourceId(mContext, imageName);
        viewHolder.iv_bg.setImageResource(resId);
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    public void addAll(List<String> imageList) {
        mImageList.addAll(imageList);
    }

    public void addItem(String image) {
        mImageList.add(image);
        notifyItemInserted(0);
    }

    public List<String> getAll() {
        return mImageList;
    }
    public String getItem(int position){
        return mImageList.get(position);
    }

    public void removeAll(){
        mImageList.clear();
    }
    public void removeItem(int position) {
        if (position >= mImageList.size()) return;

        mImageList.remove(position);
        notifyItemRemoved(position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView iv_bg;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            iv_bg = (ImageView) view.findViewById(R.id.iv_bg);
        }

        @Override
        public void onClick(View v) {

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getItem(getPosition()));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, String imageName);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
