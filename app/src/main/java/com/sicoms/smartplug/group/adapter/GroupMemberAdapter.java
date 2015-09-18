package com.sicoms.smartplug.group.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> implements HttpBitmapResponseCallbacks {

    private Context mContext;
    private ArrayList<UserVo> mVoList;
    private OnItemClickListener mItemClickListener;
    private int mMode = SPConfig.MODE_NORMAL;

    // Usage : Member List
    public GroupMemberAdapter(Context context) {
        mContext = context;
        mVoList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_group_member, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData

        viewHolder.tv_member_name.setText(mVoList.get(position).getUserName());
        String imagePath = mVoList.get(position).getUserProfileImg();
        Bitmap bitmap = BitmapFactory.decodeFile(SPConfig.FILE_PATH + imagePath);
        if( bitmap != null) {
            viewHolder.iv_member_icon.setImageBitmap(bitmap);
        } else {
            viewHolder.iv_member_icon.setImageResource(R.drawable.btn_icon_member_on);
            if( SPUtil.isNetwork(mContext)) {
                CommonService service = new CommonService(mContext);
                service.setOnHttpBitmapResponseCallbacks(this);
                service.requestDownloadImage(new ImgFileVo(mVoList.get(position).getUserProfileImg()));
            }
        }
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVoList.size();
    }

    public void addAll(List<UserVo> voList) {
        mVoList.addAll(voList);
    }

    public void addItem(UserVo vo) {
        mVoList.add(vo);
        notifyItemInserted(0);
    }

    public UserVo getItem(int position){
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

    public void setMode(int mode){
        mMode = mode;
    }

    @Override
    public void onHttpBitmapResponseResultStatus(int type, int result, String fileName, Bitmap bitmap) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            SPUtil.saveBitmapImage(fileName, bitmap);
            notifyDataSetChanged();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, UserVo theVo);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }



    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tv_member_name;
        public ImageView iv_member_icon;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            tv_member_name = (TextView) view.findViewById(R.id.tv_member_email);
            iv_member_icon = (ImageView) view.findViewById(R.id.iv_member_icon);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getItem(getPosition()));
            }
        }
    }
}
