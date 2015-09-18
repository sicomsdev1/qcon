package com.sicoms.smartplug.common;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;

import com.google.gson.Gson;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.activity.GroupActivity;
import com.sicoms.smartplug.group.activity.GroupCreatorActivity;
import com.sicoms.smartplug.group.activity.GroupMemberEditActivity;
import com.sicoms.smartplug.login.activity.LoginActivity;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.member.activity.MemberActivity;
import com.sicoms.smartplug.menu.activity.PlaceActivity;
import com.sicoms.smartplug.plug.activity.PlugActivity;
import com.sicoms.smartplug.plug.activity.PlugMainActivity;
import com.sicoms.smartplug.plug.activity.RegDeviceActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc-11-user on 2015-02-24.
 */
public class SPActivity extends ActionBarActivity {

    public static final String ARG_PLUG_NAME = "plug";
    public static final String ARG_PLUG_VO_NAME = "plug_vo";
    public static final String ARG_GROUP_VO_NAME = "group_vo";
    public static final String ARG_USER_VO_NAME = "user_vo";

    public Activity activity;
    public static List<Activity> actList = new ArrayList<Activity>();

    public void setActivity(Activity activity){
        this.activity = activity;
    }
    public Activity getActivity(){
        return this.activity;
    }

    public static void intentLoginActivity(Activity activity){
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
    public static void intentMainActivity(Activity activity) {
        for(int cnt=0; cnt<actList.size(); cnt++) {
            Activity act = actList.get(cnt);
            act.finish();
            actList.remove(act);
        }
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }
    public static void intentPlaceActivity(Activity activity) {
        Intent intent = new Intent(activity, PlaceActivity.class);
        activity.startActivity(intent);
    }
    public static void intentMemberActivity(Activity activity) {
        Intent intent = new Intent(activity, MemberActivity.class);
        activity.startActivity(intent);
    }
    public static void intentPlugMainActivity(Activity activity) {
        Intent intent = new Intent(activity, PlugMainActivity.class);
        activity.startActivity(intent);
    }
    public static void intentPlugActivity(Activity activity, PlugVo plugVo) {
        Intent intent = new Intent(activity, PlugActivity.class);
        intent.putExtra(ARG_PLUG_VO_NAME, new Gson().toJson(plugVo));
        activity.startActivity(intent);
    }
    public static void intentRegDeviceActivity(Activity activity) {
        Intent intent = new Intent(activity, RegDeviceActivity.class);
        activity.startActivity(intent);
    }
    public static void intentGroupActivity(Activity activity, GroupVo groupVo) {
        Intent intent = new Intent(activity, GroupActivity.class);
        intent.putExtra(ARG_GROUP_VO_NAME, new Gson().toJson(groupVo));
        activity.startActivity(intent);
    }
    public static void intentGroupCreatorActivity(Activity activity) {
        Intent intent = new Intent(activity, GroupCreatorActivity.class);
        activity.startActivity(intent);
    }
    public static void intentGroupMemberEditActivity(Activity activity, UserVo userVo) {
        Intent intent = new Intent(activity, GroupMemberEditActivity.class);
        intent.putExtra(ARG_USER_VO_NAME, new Gson().toJson(userVo));
        activity.startActivity(intent);
    }
}
