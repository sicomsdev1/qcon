package com.sicoms.smartplug.menu.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.menu.interfaces.PlaceResultCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.ViewHolder> {

    private Activity mActivity;
    private ArrayList<PlaceVo> mVoList;
    private OnItemClickListener mItemClickListener;
    private PlaceResultCallbacks mCallbacks;
    private int mLastPosition = 0;

    public PlaceAdapter(Activity activity) {
        mActivity = activity;
        mVoList = new ArrayList<>();
    }
    public void setPlaceResultCallbacks(PlaceResultCallbacks callbacks){
        mCallbacks = callbacks;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlaceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_place, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        PlaceVo placeVo = mVoList.get(position);
        viewHolder.tv_place_name.setText(placeVo.getPlaceName());
        viewHolder.tv_place_address.setText(placeVo.getAddress());
        viewHolder.tv_plug_count.setText(String.valueOf(placeVo.getPlugCount()));
        viewHolder.tv_member_count.setText(String.valueOf(placeVo.getMemberCount()));
        if( placeVo.getAuth().equalsIgnoreCase(String.valueOf(SPConfig.MEMBER_MASTER))){
            viewHolder.tv_auth.setText(SPConfig.MEMBER_MASTER_NAME);
        } else if( placeVo.getAuth().equalsIgnoreCase(String.valueOf(SPConfig.MEMBER_SETTER))){
            viewHolder.tv_auth.setText(SPConfig.MEMBER_SETTER_NAME);
        } else {
            viewHolder.tv_auth.setText(SPConfig.MEMBER_USER_NAME);
        }

        if(mVoList.get(position).isHere()){
            viewHolder.iv_is_place.setSelected(true);
        } else {
            viewHolder.iv_is_place.setSelected(false);
        }
        viewHolder.rl_mod_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( mCallbacks != null) {
                    PlaceVo placeVo = mVoList.get(position);
                    SPFragment.intentAddPlaceFragment(mActivity, placeVo, mCallbacks);
                    mLastPosition = position;
                }
            }
        });
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVoList.size();
    }

    public void addAll(List<PlaceVo> voList) {
        mVoList.addAll(voList);
    }

    public void addItem(PlaceVo vo) {
        mVoList.add(vo);
        notifyItemInserted(0);
    }

    public PlaceVo getItem(int position){
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
    public void removeItem(PlaceVo vo){
        mVoList.remove(vo);
    }
    public void updateItem( PlaceVo vo){
        mVoList.set(mLastPosition, vo);
    }
    public void setUnSelect(){
        for( PlaceVo vo : mVoList){
            vo.setIsHere(false);
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout rl_place;
        private ImageView iv_is_place;
        private TextView tv_place_name;
        private TextView tv_place_address;
        private RelativeLayout rl_mod_btn;
        private TextView tv_plug_count;
        private TextView tv_member_count;
        private TextView tv_auth;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            rl_place = (RelativeLayout) view.findViewById(R.id.rl_place);
            iv_is_place = (ImageView) view.findViewById(R.id.iv_is_place);
            tv_place_name = (TextView) view.findViewById(R.id.tv_place_name);
            tv_place_address = (TextView) view.findViewById(R.id.tv_place_address);
            rl_mod_btn = (RelativeLayout) view.findViewById(R.id.rl_mod_btn);
            tv_plug_count = (TextView) view.findViewById(R.id.tv_plug_count);
            tv_member_count = (TextView) view.findViewById(R.id.tv_member_count);
            tv_auth = (TextView) view.findViewById(R.id.tv_auth);
        }

        @Override
        public void onClick(View v) {

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getItem(getPosition()));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, PlaceVo theVo);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
