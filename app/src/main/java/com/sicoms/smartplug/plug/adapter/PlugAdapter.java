package com.sicoms.smartplug.plug.adapter;

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
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DbGatewayVo;
import com.sicoms.smartplug.dao.DbRouterVo;
import com.sicoms.smartplug.domain.ImgFileVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.WifiVo;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.network.http.HttpBitmapResponseCallbacks;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.wifi.WifiConnectionManager;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.plug.service.PlugDBService;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class PlugAdapter extends RecyclerView.Adapter<PlugAdapter.ViewHolder> implements HttpBitmapResponseCallbacks {

    private Activity mActivity;
    private CommonService mService;
    private PlugDBService mDbService;
    private ArrayList<PlugVo> mVoList;
    private OnItemClickListener mItemClickListener;
    private OnItemLongClickListener mItemLongClickListener;
    private int mMode = SPConfig.MODE_NORMAL;

    public PlugAdapter(Context context) {
        mActivity = (Activity) context;
        mService = new CommonService(mActivity);
        mService.setOnHttpBitmapResponseCallbacks(this);
        mDbService = new PlugDBService(mActivity);
        mVoList = new ArrayList<>();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlugAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_plug, null);
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
        if( mMode == SPConfig.MODE_NORMAL){
            viewHolder.iv_plug_setting.setVisibility(View.VISIBLE);
            viewHolder.cb_plug.setVisibility(View.INVISIBLE);
            viewHolder.cb_plug.setChecked(false);
            viewHolder.rl_btn.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.iv_plug_setting.setVisibility(View.GONE);
            viewHolder.cb_plug.setVisibility(View.VISIBLE);
            viewHolder.rl_btn.setVisibility(View.VISIBLE);
            viewHolder.rl_btn.setSelected(plugVo.isCheck());
            viewHolder.rl_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    View parentView = (ViewGroup) v.getParent();
                    CheckBox cb = (CheckBox) parentView.findViewById(R.id.cb_plug);
                    cb.setChecked(v.isSelected());
                    plugVo.setIsCheck(v.isSelected());
                }
            });
        }

        viewHolder.tv_plug_name.setText(plugVo.getPlugName());

        String imageName = plugVo.getPlugIconImg();
        if( imageName.contains(SPConfig.PLUG_DEFAULT_IMAGE_NAME)){
            int resId = SPUtil.getDrawableResourceId(mActivity, imageName);
            viewHolder.iv_plug_icon.setImageResource(resId);
        } else {
            String imagePath = SPConfig.FILE_PATH + imageName;
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap != null) {
                viewHolder.iv_plug_icon.setImageBitmap(bitmap);
            } else {
                viewHolder.iv_plug_icon.setImageResource(R.drawable.dppbg_00);
                if (SPUtil.isNetwork(mActivity)) {
                    mService.requestDownloadImage(new ImgFileVo(plugVo.getPlugIconImg()));
                }
            }
        }
        if( plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
            viewHolder.iv_network_type.setImageResource(R.drawable.icon_bluetooth); // Default
            if( MainActivity.stBluetoothManager.isConnected()) {
                PlugAllService service = new PlugAllService(mActivity);
                if( service.isEqualBluetoothPassword(plugVo)) {
                    viewHolder.iv_network_type.setImageResource(R.drawable.icon_bluetooth_point);
                }
            } else {
                viewHolder.iv_network_type.setImageResource(R.drawable.icon_bluetooth);
            }
        } else if( plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)){
            WifiConnectionManager wifiConnectionManager = new WifiConnectionManager(mActivity);
            WifiVo wifiVo = wifiConnectionManager.getConnectedWifiInfo();

            DbRouterVo dbRouterVo = mDbService.selectDbRouterData(plugVo);
            if( dbRouterVo != null && dbRouterVo.getSsId().equalsIgnoreCase(wifiVo.getSsid())){
                viewHolder.iv_network_type.setImageResource(R.drawable.icon_wifi_point);
            } else {
                viewHolder.iv_network_type.setImageResource(R.drawable.icon_wifi);
            }
        } else if( plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)){
            WifiConnectionManager wifiConnectionManager = new WifiConnectionManager(mActivity);
            WifiVo wifiVo = wifiConnectionManager.getConnectedWifiInfo();

            DbGatewayVo dbGatewayVo = mDbService.selectDbGatewayData(plugVo);
            if( dbGatewayVo != null && dbGatewayVo.getSsId().equalsIgnoreCase(wifiVo.getSsid())){
                viewHolder.iv_network_type.setImageResource(R.drawable.icon_wifi_point);
            } else {
                viewHolder.iv_network_type.setImageResource(R.drawable.icon_wifi);
            }
        } else if( plugVo.getNetworkType().equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_AP)){
            WifiConnectionManager wifiConnectionManager = new WifiConnectionManager(mActivity);
            WifiVo wifiVo = wifiConnectionManager.getConnectedWifiInfo();
            if(plugVo.getBssid().equalsIgnoreCase(wifiVo.getBssid())){
                viewHolder.iv_network_type.setImageResource(R.drawable.icon_wifi_point);
            } else {
                viewHolder.iv_network_type.setImageResource(R.drawable.icon_wifi);
            }
        }
        if( plugVo.isOn()) {
            viewHolder.rl_is_onoff.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.rl_is_onoff.setVisibility(View.VISIBLE);
        }
        viewHolder.iv_plug_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SPActivity.intentPlugActivity(mActivity, mVoList.get(position));
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

    public List<PlugVo> getAll() {
        return mVoList;
    }
    public PlugVo getItem(int position){
        return mVoList.get(position);
    }

    public void setInitCheck(){
        for(int voCnt=0; voCnt<mVoList.size(); voCnt++){
            mVoList.get(voCnt).setIsCheck(false);
        }
    }
    public List<PlugVo> getCheckedItem(){
        ArrayList<PlugVo> deleteVoList = new ArrayList<>();
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

        ArrayList<PlugVo> deleteVoList = new ArrayList<>();
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

    public void setMode(int mode){
        mMode = mode;
    }

    public int getMode(){
        return mMode;
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
        private TextView tv_plug_name;
        private ImageView iv_plug_icon;
        private ImageView iv_plug_setting;
        private RelativeLayout rl_is_onoff;
        private CheckBox cb_plug;
        private ImageView iv_network_type;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);

            rl_btn = (RelativeLayout) view.findViewById(R.id.rl_btn);
            tv_plug_name = (TextView) view.findViewById(R.id.tv_plug_name);
            iv_plug_icon = (ImageView) view.findViewById(R.id.iv_plug_icon);
            iv_plug_setting = (ImageView) view.findViewById(R.id.iv_plug_setting);
            rl_is_onoff = (RelativeLayout) view.findViewById(R.id.rl_is_checked);
            cb_plug = (CheckBox) view.findViewById(R.id.cb_plug);
            iv_network_type = (ImageView) view.findViewById(R.id.iv_network_type);
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
        void onItemClick(View view, PlugVo theVo);
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
