package com.sicoms.smartplug.common;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;

import com.sicoms.smartplug.common.interfaces.ConfirmCallbacks;
import com.sicoms.smartplug.common.interfaces.OutCallbacks;
import com.sicoms.smartplug.common.interfaces.PictureMenuCallbacks;
import com.sicoms.smartplug.common.popup.ConfirmDialogFragment;
import com.sicoms.smartplug.common.popup.OutDialogFragment;
import com.sicoms.smartplug.common.popup.PictureMenuDialogFragment;
import com.sicoms.smartplug.domain.CutoffVo;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.RegDeviceVo;
import com.sicoms.smartplug.domain.ScheduleVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.domain.WifiVo;
import com.sicoms.smartplug.group.fragment.CreateGroupFragment;
import com.sicoms.smartplug.group.fragment.GroupAllFragment;
import com.sicoms.smartplug.group.fragment.GroupFragment;
import com.sicoms.smartplug.group.fragment.GroupGalleryFragment;
import com.sicoms.smartplug.group.fragment.MemberAddGroupListFragment;
import com.sicoms.smartplug.group.fragment.MemberEditAuthFragment;
import com.sicoms.smartplug.group.fragment.MemberEditGroupListFragment;
import com.sicoms.smartplug.group.fragment.PlugAddGroupListFragment;
import com.sicoms.smartplug.group.fragment.PlugEditGroupListFragment;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.group.interfaces.EditGroupResultCallbacks;
import com.sicoms.smartplug.group.popup.EditGroupNameDialogFragment;
import com.sicoms.smartplug.login.interfaces.LoginResultCallbacks;
import com.sicoms.smartplug.login.popup.MembershipDialogFragment;
import com.sicoms.smartplug.login.popup.PersonalInfoDialogFragment;
import com.sicoms.smartplug.login.popup.TermsDialogFragment;
import com.sicoms.smartplug.main.popup.EditNicknameDialogFragment;
import com.sicoms.smartplug.member.fragment.AddMemberFragment;
import com.sicoms.smartplug.member.fragment.EditMemberFragment;
import com.sicoms.smartplug.member.fragment.MemberFragment;
import com.sicoms.smartplug.menu.fragment.MypageFragment;
import com.sicoms.smartplug.menu.fragment.PlaceGalleryFragment;
import com.sicoms.smartplug.menu.fragment.PlaceListFragment;
import com.sicoms.smartplug.menu.fragment.PlaceMapFragment;
import com.sicoms.smartplug.menu.fragment.PlaceSettingFragment;
import com.sicoms.smartplug.menu.interfaces.ImageSelectedResultCallbacks;
import com.sicoms.smartplug.menu.interfaces.PlaceResultCallbacks;
import com.sicoms.smartplug.plug.fragment.AddCutoffFragment;
import com.sicoms.smartplug.plug.fragment.CutoffFragment;
import com.sicoms.smartplug.plug.fragment.EditCutoffFragment;
import com.sicoms.smartplug.plug.fragment.PlugAllFragment;
import com.sicoms.smartplug.plug.fragment.PlugFragment;
import com.sicoms.smartplug.plug.fragment.PlugGalleryFragment;
import com.sicoms.smartplug.plug.fragment.RegDeviceFragment;
import com.sicoms.smartplug.plug.fragment.ScheduleFragment;
import com.sicoms.smartplug.plug.interfaces.CutoffResultCallbacks;
import com.sicoms.smartplug.plug.interfaces.DialogFinishCallbacks;
import com.sicoms.smartplug.plug.interfaces.EditNameFinishCallbacks;
import com.sicoms.smartplug.plug.interfaces.ScheduleResultCallbacks;
import com.sicoms.smartplug.plug.popup.BLSecurityDialogFragment;
import com.sicoms.smartplug.plug.popup.EditNameDialogFragment;
import com.sicoms.smartplug.plug.popup.StationModeDialogFragment;
import com.sicoms.smartplug.plug.popup.WifiSecurityDialogFragment;

/**
 * Created by pc-11-user on 2015-02-25.
 */
public class SPFragment extends Fragment {

    /*
     * Plug
     */
    public static void intentPlugAllFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlugAllFragment.newInstance());
        ft.commit();
    }
    public static void intentPlugFragment(Activity activity, PlugVo plugVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlugFragment.newInstance(plugVo));
        ft.commit();
    }
    public static void intentRegDeviceFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, RegDeviceFragment.newInstance());
        ft.commit();
    }
    public static void intentScheduleFragment(Activity activity, PlugVo plugVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, ScheduleFragment.newInstance(plugVo));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentCutoffFragment(Activity activity, PlugVo plugVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, CutoffFragment.newInstance(plugVo));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentAddCutoffFragment(Activity activity, CutoffResultCallbacks callbacks, PlugVo plugVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, AddCutoffFragment.newInstance(plugVo, callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentEditCutoffFragment(Activity activity, CutoffVo cutoffVo, CutoffResultCallbacks callbacks, PlugVo plugVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, EditCutoffFragment.newInstance(plugVo, cutoffVo, callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentPlugGalleryFragment(Activity activity, ImageSelectedResultCallbacks callbacks){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlugGalleryFragment.newInstance(callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }

    /*
     * Group
     */
    public static void intentGroupAllFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, GroupAllFragment.newInstance());
        ft.commit();
    }
    public static void intentGroupFragment(Activity activity, GroupVo groupVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, GroupFragment.newInstance(groupVo));
        ft.commit();
    }
    public static void intentCreateGroupFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, CreateGroupFragment.newInstance());
        ft.commit();
    }
    public static void intentPlugAddGroupListFragment(Activity activity, CreateGroupResultCallbacks callbacks, GroupVo groupVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlugAddGroupListFragment.newInstance(groupVo, callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentPlugEditGroupListFragment(Activity activity, EditGroupResultCallbacks callbacks, GroupVo groupVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlugEditGroupListFragment.newInstance(groupVo, callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentMemberAddGroupListFragment(Activity activity, CreateGroupResultCallbacks callbacks, GroupVo groupVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, MemberAddGroupListFragment.newInstance(groupVo, callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentMemberEditGroupListFragment(Activity activity, EditGroupResultCallbacks callbacks, GroupVo groupVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, MemberEditGroupListFragment.newInstance(groupVo, callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentMemberEditAuthFragment(Activity activity, UserVo userVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, MemberEditAuthFragment.newInstance(userVo));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentGroupGalleryFragment(Activity activity, ImageSelectedResultCallbacks callbacks){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, GroupGalleryFragment.newInstance(callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }

    /*
     * Member
     */
    public static void intentMemberFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, MemberFragment.newInstance());
        ft.commit();
    }
    public static void intentAddMemberFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, AddMemberFragment.newInstance());
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentEditMemberFragment(Activity activity, UserVo userVo){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, EditMemberFragment.newInstance(userVo));
        ft.addToBackStack(null);
        ft.commit();
    }

    /*
     * Menu
     */
    // Place
    public static void intentPlaceListFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlaceListFragment.newInstance());
        ft.commit();
    }
    public static void intentAddPlaceFragment(Activity activity, PlaceResultCallbacks callbacks){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlaceMapFragment.newInstance(callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentAddPlaceFragment(Activity activity, PlaceVo placeVo, PlaceResultCallbacks callbacks){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlaceMapFragment.newInstance(placeVo, callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    public static void intentPlaceGalleryFragment(Activity activity, ImageSelectedResultCallbacks callbacks){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlaceGalleryFragment.newInstance(callbacks));
        ft.addToBackStack(null);
        ft.commit();
    }
    // My Page
    public static void intentMypageListFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, MypageFragment.newInstance());
        ft.commit();
    }
    // Place Setting
    public static void intentPlaceSettingFragment(Activity activity){
        FragmentTransaction ft = ((ActionBarActivity)activity).getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, PlaceSettingFragment.newInstance());
        ft.commit();
    }


    // Dialog
    public static void intentMembershipFragmentDialog(Activity activity, LoginResultCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        MembershipDialogFragment dialog = MembershipDialogFragment.newInstance(callbacks);
        dialog.show(fm, null);
    }
    public static void intentEditNicknameFragmentDialog(Activity activity, UserVo userVo, EditNameFinishCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        EditNicknameDialogFragment dialog = EditNicknameDialogFragment.newInstance(userVo, callbacks);
        dialog.show(fm, null);
    }
    public static void intentEditGroupNameFragmentDialog(Activity activity, GroupVo groupVo, EditNameFinishCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        EditGroupNameDialogFragment dialog = EditGroupNameDialogFragment.newInstance(groupVo, callbacks);
        dialog.show(fm, null);
    }
    public static void intentWifiSecurityFragmentDialog(Activity activity, RegDeviceVo regDeviceVo, DialogFinishCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        WifiSecurityDialogFragment dialog = WifiSecurityDialogFragment.newInstance(regDeviceVo, callbacks);
        dialog.show(fm, null);
    }
    public static void intentWifiConnectWaitFragmentDialog(Activity activity, DialogFinishCallbacks callbacks, WifiVo wifiVo){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        StationModeDialogFragment dialog = StationModeDialogFragment.newInstance(callbacks, wifiVo);
        dialog.show(fm, null);
    }
    public static void intentBLSecurityFragmentDialog(Activity activity, RegDeviceVo regDeviceVo, DialogFinishCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        BLSecurityDialogFragment dialog = BLSecurityDialogFragment.newInstance(regDeviceVo, callbacks);
        dialog.show(fm, null);
    }
    public static void intentEditPlugNameFragmentDialog(Activity activity, PlugVo plugVo, EditNameFinishCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        EditNameDialogFragment dialog = EditNameDialogFragment.newInstance(plugVo, callbacks);
        dialog.show(fm, null);
    }
    public static void intentPictureMenuFragmentDialog(Activity activity, PictureMenuCallbacks callbacks, int type){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        PictureMenuDialogFragment dialog = PictureMenuDialogFragment.newInstance(callbacks, type);
        dialog.show(fm, null);
    }
    public static void intentTermsFragmentDialog(Activity activity){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        TermsDialogFragment dialog = TermsDialogFragment.newInstance();
        dialog.show(fm, null);
    }
    public static void intentPersonalInfoFragmentDialog(Activity activity){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        PersonalInfoDialogFragment dialog = PersonalInfoDialogFragment.newInstance();
        dialog.show(fm, null);
    }
    public static void intentOutFragmentDialog(Activity activity, OutCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        OutDialogFragment dialog = OutDialogFragment.newInstance(callbacks);
        dialog.show(fm, null);
    }
    public static void intentOutFragmentDialog(Activity activity, OutCallbacks callbacks, String topicName, String btnName){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        OutDialogFragment dialog = OutDialogFragment.newInstance(callbacks, topicName, btnName);
        dialog.show(fm, null);
    }
    public static void intentConfirmFragmentDialog(Activity activity, RegDeviceVo regDeviceVo, ConfirmCallbacks callbacks){
        FragmentManager fm = ((ActionBarActivity) activity).getSupportFragmentManager();
        ConfirmDialogFragment dialog = ConfirmDialogFragment.newInstance(callbacks, regDeviceVo);
        dialog.show(fm, null);
    }
}
