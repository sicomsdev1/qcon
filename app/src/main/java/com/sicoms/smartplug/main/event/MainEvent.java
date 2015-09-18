package com.sicoms.smartplug.main.event;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

import com.sicoms.smartplug.R;
import com.sicoms.smartplug.common.SPActivity;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.common.SPFragment;
import com.sicoms.smartplug.domain.HomeMenuVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.main.adapter.HomeMenuAdapter;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.main.service.MainService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.util.SPUtil;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class MainEvent implements View.OnClickListener, HomeMenuAdapter.OnItemClickListener {

    private Activity mActivity;
    private Fragment mFragment;
    private MainService mService;
    private UserVo mUserVo;

    public MainEvent(Fragment fragment){
        mActivity = fragment.getActivity();
        mFragment = fragment;
    }
    public MainEvent(Fragment fragment, UserVo userVo){
        mActivity = fragment.getActivity();
        mFragment = fragment;
        mUserVo = userVo;
    }

    @Override
    public void onClick(View v) {
        PlaceVo lastPlaceVo = PlaceService.loadLastPlace(mActivity);
        switch(v.getId()){
            case R.id.rl_menu_group_member :
                if( lastPlaceVo != null){
                    SPActivity.intentMemberActivity(mActivity);
                } else {
                    SPUtil.showToast(mActivity, "플레이스를 선택해주세요.");
                    SPActivity.intentPlaceActivity(mActivity);
                }
                break;
            case R.id.rl_menu_smart_plug :
                if( lastPlaceVo != null) {
                    SPActivity.intentPlugMainActivity(mActivity);
                } else {
                    SPUtil.showToast(mActivity, "플레이스를 선택해주세요.");
                    SPActivity.intentPlaceActivity(mActivity);
                }
                break;
            case R.id.rl_menu_smart_socket :
                Toast.makeText(mActivity, "곧 출시 됩니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rl_menu_smart_bulbs :
                Toast.makeText(mActivity, "곧 출시 됩니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rl_menu_smart_switch :
                Toast.makeText(mActivity, "곧 출시 됩니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onItemClick(View view, HomeMenuVo theVo) {
        PlaceVo lastPlaceVo = PlaceService.loadLastPlace(mActivity);
        if( theVo.getMenuNum() == SPConfig.MENU_GROUP_MEMBER){
            if( lastPlaceVo != null){
                SPActivity.intentMemberActivity(mActivity);
            } else {
                SPUtil.showToast(mActivity, "플레이스를 선택해주세요.");
                SPActivity.intentPlaceActivity(mActivity);
            }
        } else if( theVo.getMenuNum() == SPConfig.MENU_SMART_PLUG){
            if( lastPlaceVo != null) {
                SPActivity.intentPlugMainActivity(mActivity);
            } else {
                SPUtil.showToast(mActivity, "플레이스를 선택해주세요.");
                SPActivity.intentPlaceActivity(mActivity);
            }
        } else {
            Toast.makeText(mActivity, "곧 출시 됩니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
