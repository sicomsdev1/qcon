package com.sicoms.smartplug.group.adapter;

import android.app.Activity;
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
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.interfaces.MemberCheckResultCallbacks;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class MemberEditGroupAdapter extends RecyclerView.Adapter<MemberEditGroupAdapter.ViewHolder> implements HttpBitmapResponseCallbacks {

    private Activity mActivity;
    private ArrayList<UserVo> mVoList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private MemberCheckResultCallbacks mCallbacks;

    // Usage : Group - Create Group - Add Member List
    public MemberEditGroupAdapter(Activity activity, MemberCheckResultCallbacks callbacks) {
        mActivity = activity;
        mVoList = new ArrayList<>();
        mCallbacks = callbacks;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public MemberEditGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_member, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        String imageName = mVoList.get(position).getUserProfileImg();
        if( imageName.contains(SPConfig.USER_DEFAULT_IMAGE_NAME)){
            int resId = SPUtil.getDrawableResourceId(mActivity, imageName);
            viewHolder.iv_member_icon.setImageResource(resId);
        } else {
            String imagePath = SPConfig.FILE_PATH + imageName;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(imagePath);
            } catch (Exception e) {

            }
            if (bitmap != null) {
                viewHolder.iv_member_icon.setImageBitmap(bitmap);
            } else {
                //viewHolder.iv_member_icon.setImageResource(R.drawable.btn_icon_member_on);
                CommonService service = new CommonService(mActivity);
                service.setOnHttpBitmapResponseCallbacks(this);
                service.requestDownloadImage(new ImgFileVo(mVoList.get(position).getUserProfileImg()));
            }
        }

        viewHolder.tv_member_name.setText(mVoList.get(position).getUserName());

        if( mVoList.get(position).getAuth() == SPConfig.MEMBER_MASTER) {
            viewHolder.iv_member_auth.setVisibility(View.VISIBLE);
        } else {
            viewHolder.iv_member_auth.setVisibility(View.INVISIBLE);
        }
        if( mVoList.get(position).isOn()) {
            viewHolder.rl_is_onoff.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.rl_is_onoff.setVisibility(View.VISIBLE);
        }

        viewHolder.cb_member.setChecked(false);
        if( mVoList.get(position).isCheck()){
            viewHolder.cb_member.setChecked(true);
            viewHolder.rl_is_onoff.setVisibility(View.VISIBLE);
            viewHolder.rl_btn.setOnClickListener(null);
            return;
        }

        viewHolder.rl_btn.setSelected(mVoList.get(position).isCheck());
        viewHolder.rl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setSelected(!v.isSelected());
                View parentView = (ViewGroup) v.getParent();
                CheckBox cb = (CheckBox) parentView.findViewById(R.id.cb_member);
                cb.setChecked(v.isSelected());
                mVoList.get(position).setAuth(SPConfig.MEMBER_USER);
                mVoList.get(position).setIsCheck(v.isSelected());

                if (mCallbacks != null) {
                    if (mVoList.get(position).isCheck()) {
                        mCallbacks.onCheckedMember(mVoList.get(position));
                    } else {
                        mCallbacks.onUnCheckedMember(mVoList.get(position));
                    }
                }
            }
        });
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
    public List<UserVo> getCheckedItems(){
        ArrayList<UserVo> deleteVoList = new ArrayList<>();
        for(int voCnt=0; voCnt<mVoList.size(); voCnt++){
            if( mVoList.get(voCnt).isCheck()){
                deleteVoList.add(mVoList.get(voCnt));
            }
        }
        return deleteVoList;
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

        ArrayList<UserVo> deleteVoList = new ArrayList<>();
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
        private TextView tv_member_name;
        private ImageView iv_member_icon;
        private ImageView iv_member_auth;
        private RelativeLayout rl_is_onoff;
        private CheckBox cb_member;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            rl_btn = (RelativeLayout) view.findViewById(R.id.rl_btn);
            tv_member_name = (TextView) view.findViewById(R.id.tv_member_email);
            iv_member_icon = (ImageView) view.findViewById(R.id.iv_member_icon);
            iv_member_auth = (ImageView) view.findViewById(R.id.iv_member_rank);
            rl_is_onoff = (RelativeLayout) view.findViewById(R.id.rl_is_onoff);
            cb_member = (CheckBox) view.findViewById(R.id.cb_member);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getItem(getPosition()));
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if( mItemLongClickListener != null){
                mItemLongClickListener.onItemLongClick(v, getPosition());
                return true;
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, UserVo theVo);
    }

    public void SetOnItemClickListener(final OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    public void SetOnItemLongClickListener(final OnItemLongClickListener itemLongClickListener){
        mItemLongClickListener = itemLongClickListener;
    }
}
