package com.sicoms.smartplug.group.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.CommonService;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.util.SPUtil;
import com.sicoms.smartplug.util.profile.CircleImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> implements HttpBitmapResponseCallbacks {

    private Context mContext;
    private ArrayList<GroupVo> mVoList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private int mMode = SPConfig.MODE_NORMAL;

    public GroupAdapter(Context context) {
        mContext = context;
        mVoList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_group, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        if( mMode == SPConfig.MODE_NORMAL){
            viewHolder.cb_group.setVisibility(View.INVISIBLE);
            viewHolder.arrow.setVisibility(View.VISIBLE);
            viewHolder.cb_group.setChecked(false);
            mVoList.get(position).setIsCheck(false);
            viewHolder.rl_btn.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.arrow.setVisibility(View.INVISIBLE);
            viewHolder.cb_group.setVisibility(View.VISIBLE);
            viewHolder.rl_btn.setVisibility(View.VISIBLE);
            viewHolder.rl_btn.setSelected(mVoList.get(position).isCheck());
            viewHolder.cb_group.setChecked(mVoList.get(position).isCheck());
            viewHolder.rl_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentView = (ViewGroup) v.getParent();
                    CheckBox cb = (CheckBox) parentView.findViewById(R.id.cb_group);
                    cb.setChecked(!cb.isChecked());
                    mVoList.get(position).setIsCheck(cb.isChecked());
                }
            });
        }

        String imageName = mVoList.get(position).getGroupIconImg();
        if( imageName.contains(SPConfig.GROUP_DEFAULT_IMAGE_NAME)){
            int resId = SPUtil.getDrawableResourceId(mContext, imageName);
            viewHolder.iv_group_icon.setImageResource(resId);
        } else {
            File file = new File(SPConfig.FILE_PATH + imageName);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bitmap != null) {
                viewHolder.iv_group_icon.setImageBitmap(bitmap);
            } else {
                viewHolder.iv_group_icon.setImageResource(R.drawable.dgpbg_00);
                if (SPUtil.isNetwork(mContext)) {
                    CommonService service = new CommonService(mContext);
                    service.setOnHttpBitmapResponseCallbacks(this);
                    service.requestDownloadImage(new ImgFileVo(mVoList.get(position).getGroupIconImg()));
                }
            }
        }

        viewHolder.tv_group_name.setText(mVoList.get(position).getGroupName());
        viewHolder.tv_group_usage.setText(mVoList.get(position).getUsage());
        if( mVoList.get(position).isOn()) {
            // 사진 흑백 효과
            viewHolder.tv_group_name.setTextColor(mContext.getResources().getColor(R.color.color01));
            viewHolder.tv_group_usage.setTextColor(mContext.getResources().getColor(R.color.color01));
        } else {
            // 사진 흑백 효과
            viewHolder.tv_group_name.setTextColor(mContext.getResources().getColor(R.color.off));
            viewHolder.tv_group_usage.setTextColor(mContext.getResources().getColor(R.color.off));
        }
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVoList.size();
    }

    public void setMode(int mode){
        mMode = mode;
    }

    public void addAll(List<GroupVo> voList) {
        mVoList.addAll(voList);
    }

    public void addItem(GroupVo vo) {
        mVoList.add(vo);
        notifyItemInserted(0);
    }

    public List<GroupVo> getAll(){
        return mVoList;
    }

    public GroupVo getItem(int position){
        return mVoList.get(position);
    }

    public List<GroupVo> getCheckedItem(){
        ArrayList<GroupVo> checkedVoList = new ArrayList<>();
        for(int voCnt=0; voCnt<mVoList.size(); voCnt++){
            if( mVoList.get(voCnt).isCheck()){
                checkedVoList.add(mVoList.get(voCnt));
            }
        }

        return checkedVoList;
    }

    public void removeAll(){
        mVoList.clear();
    }
    public void removeItem(int position) {
        if (position >= mVoList.size()) return;

        mVoList.remove(position);
        notifyItemRemoved(position);
    }
    public void removeCheckedItem(){
        ArrayList<GroupVo> deleteVoList = new ArrayList<>();
        for(int voCnt=0; voCnt<mVoList.size(); voCnt++){
            if( mVoList.get(voCnt).isCheck()){
                deleteVoList.add(mVoList.get(voCnt));
            }
        }
        for(int voCnt=0; voCnt<deleteVoList.size(); voCnt++) {
            mVoList.remove(deleteVoList.get(voCnt));
            notifyItemRemoved(voCnt);
        }
    }

    @Override
    public void onHttpBitmapResponseResultStatus(int type, int result, String fileName, Bitmap bitmap) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            SPUtil.saveBitmapImage(fileName, bitmap);
            notifyDataSetChanged();
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private RelativeLayout rl_btn;
        private CircleImageView iv_group_icon;
        private TextView tv_group_name;
        private TextView tv_group_usage;
        private CheckBox cb_group;
        private ImageView arrow; // Visible, Invisible 용으로만 사용

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            rl_btn = (RelativeLayout) view.findViewById(R.id.rl_btn);
            iv_group_icon = (CircleImageView) view.findViewById(R.id.iv_group_icon_btn);
            tv_group_name = (TextView) view.findViewById(R.id.tv_group_name);
            tv_group_usage = (TextView) view.findViewById(R.id.tv_group_usage);
            cb_group = (CheckBox) view.findViewById(R.id.cb_group);
            arrow = (ImageView) view.findViewById(R.id.arrow);
        }

        @Override
        public void onClick(View v) {

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getItem(getPosition()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (mItemLongClickListener != null) {
                mItemLongClickListener.onItemLongClick(v, getPosition());
                return true;
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, GroupVo theVo);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public void SetOnItemLongClickListener(final OnItemLongClickListener itemLongClickListener){
        mItemLongClickListener = itemLongClickListener;
    }

}
