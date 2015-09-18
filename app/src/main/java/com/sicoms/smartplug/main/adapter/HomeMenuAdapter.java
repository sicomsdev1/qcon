package com.sicoms.smartplug.main.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.HomeMenuVo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class HomeMenuAdapter extends RecyclerView.Adapter<HomeMenuAdapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<HomeMenuVo> mVoList;
    private OnItemClickListener mItemClickListener;

    public HomeMenuAdapter(Activity activity) {
        mActivity = activity;
        mVoList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HomeMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_home_menu, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        viewHolder.tv_menu_name.setText(mVoList.get(position).getMenuName());
        viewHolder.iv_menu_icon.setImageResource(mVoList.get(position).getMenuIconImg());
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVoList.size();
    }

    public void addAll(List<HomeMenuVo> voList) {
        mVoList.addAll(voList);
    }

    public void addItem(HomeMenuVo vo) {
        mVoList.add(vo);
        notifyItemInserted(0);
    }

    public HomeMenuVo getItem(int position){
        return mVoList.get(position);
    }

    public void removeAll(){
        mVoList.clear();
    }
    public void removeItem(int position) {
        if (position >= mVoList.size()) return;

        mVoList.remove(position);
        notifyItemRemoved(position);
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tv_menu_name;
        public ImageView iv_menu_icon;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            tv_menu_name = (TextView) view.findViewById(R.id.tv_menu_name);
            iv_menu_icon = (ImageView) view.findViewById(R.id.iv_menu_icon);
        }

        @Override
        public void onClick(View v) {

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getItem(getPosition()));
            }
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, HomeMenuVo theVo);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
