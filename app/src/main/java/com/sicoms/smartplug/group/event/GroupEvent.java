package com.sicoms.smartplug.group.event;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.HttpResponseVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.adapter.GroupMemberAdapter;
import com.sicoms.smartplug.group.interfaces.EditGroupResultCallbacks;
import com.sicoms.smartplug.group.service.GroupAllService;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.ControlResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 19..
 */
public class GroupEvent implements View.OnClickListener, HttpResponseCallbacks, GroupMemberAdapter.OnItemClickListener, View.OnKeyListener {
    private static final String TAG = GroupEvent.class.getSimpleName();

    private Activity mActivity;
    private Fragment mFragment;
    private GroupVo mGroupVo;
    private GroupService mService;
    private EditGroupResultCallbacks mCallbacks;
    private ControlResultCallbacks mControlCallbacks;
    private EditNameFinishCallbacks mEditNameCallbacks;
    private PictureMenuCallbacks mPictureCallbacks;
    private ImageSelectedResultCallbacks mImageCallbacks;

    public GroupEvent(Context context, GroupVo groupVo){
        mActivity = (Activity)context;
        mGroupVo = groupVo;
        mService = new GroupService(mActivity);
    }
    public void setFragment(Fragment fragment){
        mFragment = fragment;
    }
    public void setGroupVo(GroupVo groupVo){
        mGroupVo = groupVo;
    }
    public void setOnEditGroupResultCallbacks(EditGroupResultCallbacks callbacks){
        mCallbacks = callbacks;
    }
    public void setOnControlResultCallbacks(ControlResultCallbacks callbacks){
        mControlCallbacks = callbacks;
    }
    public void setOnEditNameFinishCallbacks(EditNameFinishCallbacks callbacks){
        mEditNameCallbacks = callbacks;
    }
    public void setOnPictureMenuCallbacks(PictureMenuCallbacks callbacks){
        mPictureCallbacks = callbacks;
    }
    public void setOnImageSelectedResultCallbacks(ImageSelectedResultCallbacks callbacks){
        mImageCallbacks = callbacks;
    }

    @Override
    public void onClick(View v) {
        List<PlugVo> gwPlugVoList = new ArrayList<>();
        List<PlugVo> stPlugVoList = new ArrayList<>();
        List<PlugVo> blPlugVoList = new ArrayList<>();
        switch (v.getId()){
            case R.id.rl_all_power_btn :
                boolean isOn = !v.isSelected();
                String onoff = isOn ? HttpConfig.CONTROL_ON_OFF_DP_S_ON : HttpConfig.CONTROL_ON_OFF_DP_S_OFF;
                if( mGroupVo.getPlugVoList() == null){
                    return;
                }
                for( PlugVo plugVo : mGroupVo.getPlugVoList()){
                    String type = plugVo.getNetworkType();
                    if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_GW)){
                        gwPlugVoList.add(plugVo);
                    } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_WIFI_ROUTER)){
                        stPlugVoList.add(plugVo);
                    } else if( type.equalsIgnoreCase(SPConfig.PLUG_TYPE_BLUETOOTH)){
                        blPlugVoList.add(plugVo);
                    }
                }

                if( gwPlugVoList.size() > 0){
                    mService.controlOnOffTypeGateway(gwPlugVoList, onoff);
                    if( mControlCallbacks != null){
                        mControlCallbacks.onGroupControlOnOffResult(gwPlugVoList, isOn);
                    }
                }
                if( stPlugVoList.size() > 0){
                    mService.controlOnOffTypeRouter(stPlugVoList, onoff);
                    if( mControlCallbacks != null){
                        mControlCallbacks.onGroupControlOnOffResult(stPlugVoList, isOn);
                    }
                }
                if( blPlugVoList.size() > 0){
                    if(MainActivity.stBluetoothManager.isConnected()){
                        mService.controlOnOffTypeBluetooth(blPlugVoList, isOn);
                        if( mControlCallbacks != null){
                            mControlCallbacks.onGroupControlOnOffResult(blPlugVoList, isOn);
                        }
                    } else {
                        SPUtil.showToast(mActivity, "블루투스에 연결되지 않았습니다.");
                    }
                }
                v.setSelected(isOn);
                break;

            case R.id.fab_add_device_btn :
                if( mCallbacks != null) {
                    SPFragment.intentPlugEditGroupListFragment(mActivity, mCallbacks, mGroupVo);
                }
                break;

            case R.id.rl_add_member_btn :
                SPFragment.intentMemberEditGroupListFragment(mActivity, mCallbacks, mGroupVo);
                break;

            case R.id.rl_group_out_btn :
                GroupAllService service = new GroupAllService(mActivity);
                service.setOnHttpResponseCallbacks(this);
                service.requestDeleteGroupList(Arrays.asList(mGroupVo));
                break;
            case R.id.iv_group_icon :
                SPFragment.intentPictureMenuFragmentDialog(mActivity, mPictureCallbacks, SPConfig.PICTURE_MENU_TYPE_PLACE);
                break;
            case R.id.iv_edit_group_name_btn :
                SPFragment.intentEditGroupNameFragmentDialog(mActivity, mGroupVo, mEditNameCallbacks);
                break;
        }
    }

    @Override
    public void onHttpResponseResultStatus(int type, int result, String value) {
        if( result == HttpConfig.HTTP_SUCCESS) {
            try {
                HttpResponseVo response = new Gson().fromJson(value, HttpResponseVo.class);
                int resultNum = Integer.parseInt(response.getResult());
                if (CloudManager.CLOUD_REQUEST_NUM == ContextPathStore.REQUEST_DELETE_GROUP_LIST) {
                    if( resultNum == HttpConfig.HTTP_SUCCESS) {
                        List<GroupVo> groupVoList = new Gson().fromJson(response.getJsonStr(), new TypeToken<List<GroupVo>>() {
                        }.getType());
                        if (groupVoList != null) {
                            GroupAllService service = new GroupAllService(mActivity);
                            service.deleteDbGroupList(groupVoList);
                            mActivity.finish();
                        }
                    } else if( resultNum == -1){
                        SPUtil.showToast(mActivity, "그룹에는 1명 이상의 마스터 권한의 사용자가 있어야 합니다.");
                    } else {
                        SPUtil.showToast(mActivity, "그룹정보 요청에 실패하였습니다.");
                    }
                }
            } catch (JsonParseException jpe) {
                jpe.printStackTrace();
            } catch (NumberFormatException nfe){
                nfe.printStackTrace();
            }
        } else {
            Toast.makeText(mActivity, "서버 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onItemClick(View view, UserVo theVo) {
        SPActivity.intentGroupMemberEditActivity(mActivity, theVo);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(this);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK;
    }
}
