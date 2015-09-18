package com.sicoms.smartplug.group.adapter;

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
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.group.interfaces.PlugCheckResultCallbacks;
import com.sicoms.smartplug.group.service.CreateGroupService;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class PlugAddGroupAdapter extends RecyclerView.Adapter<PlugAddGroupAdapter.ViewHolder> implements HttpBitmapResponseCallbacks {

    private Context mContext;
    private CreateGroupService mService;
    private ArrayList<PlugVo> mVoList;
    private OnItemClickListener mItemClickListener;
    private PlugCheckResultCallbacks mCallbacks;
    private String mGroupType;

    public PlugAddGroupAdapter(Context context, PlugCheckResultCallbacks callbacks) {
        mContext = context;
        mService = new CreateGroupService(mContext);
        mVoList = new ArrayList<>();
        mCallbacks = callbacks;
        mGroupType = "";
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlugAddGroupAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_plug_add_group, null);
        // create ViewHolder
        ViewHolder viewHolder = new ViewHolder(itemLayoutView);


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        // - get data from your itemsData at this position
        // - replace the contents of the view with that itemsData
        final PlugVo plugVo = mVoList.get(position);
        viewHolder.tv_plug_name.setText(plugVo.getPlugName());
        Bitmap bitmap = BitmapFactory.decodeFile(SPConfig.FILE_PATH + plugVo.getPlugIconImg());
        if( bitmap != null) {
            viewHolder.iv_plug_icon.setImageBitmap(bitmap);
        } else {
            viewHolder.iv_plug_icon.setImageResource(R.drawable.dgpbg_00);
            if( SPUtil.isNetwork(mContext)) {
                CommonService service = new CommonService(mContext);
                service.setOnHttpBitmapResponseCallbacks(this);
                service.requestDownloadImage(new ImgFileVo(plugVo.getPlugIconImg()));
            }
        }
        final String type = plugVo.getNetworkType();
        if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
            return;
        }
        viewHolder.iv_checkbox.setVisibility(View.INVISIBLE);
        if( plugVo.isCheck()){
            viewHolder.iv_checkbox.setVisibility(View.VISIBLE);
            mGroupType = type;
        }

        viewHolder.rl_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGroupType = type;
                plugVo.setIsCheck(!plugVo.isCheck());
                if( mCallbacks != null) {
                    if (plugVo.isCheck()) {
                        mCallbacks.onCheckedPlug(plugVo);
                        viewHolder.iv_checkbox.setVisibility(View.VISIBLE);
                    } else {
                        mCallbacks.onUnCheckedPlug(plugVo);
                        viewHolder.iv_checkbox.setVisibility(View.INVISIBLE);
                    }
                    //notifyDataSetChanged();
                    mGroupType = "";
                }
            }
        });
    }

    // Return the size of your itemsData (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mVoList.size();
    }

    public void addAll(List<PlugVo> voList) {
        mVoList.addAll(voList);
    }

    public void addItem(PlugVo vo) {
        mVoList.add(vo);
        notifyItemInserted(0);
    }

    public PlugVo getItem(int position){
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
    public void setType(String type){
        mGroupType = type;
    }

    @Override
    public void onHttpBitmapResponseResultStatus(int type, int result, String fileName, Bitmap bitmap) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            SPUtil.saveBitmapImage(fileName, bitmap);
            notifyDataSetChanged();
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RelativeLayout rl_btn;
        public TextView tv_plug_name;
        public ImageView iv_plug_icon;
        public ImageView iv_checkbox;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            rl_btn = (RelativeLayout) view.findViewById(R.id.rl_btn);
            tv_plug_name = (TextView) view.findViewById(R.id.tv_plug_name);
            iv_plug_icon = (ImageView) view.findViewById(R.id.iv_plug_icon);
            iv_checkbox = (ImageView) view.findViewById(R.id.iv_checkbox);
        }

        @Override
        public void onClick(View v) {

            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getItem(getPosition()));
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View view, PlugVo theVo);
    }

    public void SetOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
