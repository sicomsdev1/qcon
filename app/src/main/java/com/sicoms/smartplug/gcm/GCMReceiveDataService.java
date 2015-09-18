package com.sicoms.smartplug.gcm;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DbBluetoothVo;
import com.sicoms.smartplug.dao.DbGroupUserMappingVo;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserPlaceMappingVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.service.CreateGroupService;
import com.sicoms.smartplug.group.service.GroupService;
import com.sicoms.smartplug.login.activity.IntroActivity;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.member.service.MemberService;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.plug.service.RegDeviceService;
import com.sicoms.smartplug.util.NotificationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 26..
 */
public class GCMReceiveDataService {

    Context mContext;

    public GCMReceiveDataService(Context context){
        mContext = context;
    }

    public void setData(int num, String data){
        try {
            // Place
            if (num == ContextPathStore.REQUEST_UPDATE_PLACE) {
                PlaceVo placeVo = new Gson().fromJson(data, PlaceVo.class);
                if (placeVo == null) {
                    return;
                }
                PlaceService service = new PlaceService(mContext);
                service.updateDbPlace(placeVo);
                PlaceVo lastPlaceVo = PlaceService.loadLastPlace(mContext);
                if( lastPlaceVo.getPlaceId().equalsIgnoreCase(placeVo.getPlaceId())){
                    PlaceService.saveLastPlace(mContext, placeVo);
                }
            } else if (num == ContextPathStore.REQUEST_OUT_PLACE) {
                UserVo userVo = new Gson().fromJson(data, UserVo.class);
                if (userVo == null) {
                    return;
                }
                MemberService service = new MemberService(mContext);
                service.deleteDbMember(userVo);
            } else if (num == ContextPathStore.REQUEST_INSERT_USER) {
                List<UserPlaceMappingVo> userPlaceMappingVoList = new Gson().fromJson(data, new TypeToken<List<UserPlaceMappingVo>>() {
                }.getType());
                if (userPlaceMappingVoList == null) {
                    return;
                }
                MemberService memberService = new MemberService(mContext);
                PlaceService placeService = new PlaceService(mContext);

                List<UserVo> userVoList = new ArrayList<>();
                for(int voCnt=0; voCnt<userPlaceMappingVoList.size(); voCnt++){
                    UserVo userVo = userPlaceMappingVoList.get(voCnt).getUserVo();
                    userVoList.add(userVo);
                }
                PlaceVo placeVo = userPlaceMappingVoList.get(0).getPlaceVo();
                List<UserVo> currentUserInPlace = memberService.selectDbMemberList(placeVo);
                if( currentUserInPlace == null || currentUserInPlace.size() < 1){
                    UserVo loginVo = LoginService.loadLastLoginUser(mContext);
                    for( int cnt=0; cnt<userVoList.size(); cnt++){
                        UserVo userVo = userVoList.get(cnt);
                        if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){ // 수신한 플레이스에 자신이 속해있을 경우 수정. (한 폰에 두개 이상의 gcm id 를 가질 수 있으므로 현재 로그인 된 사용자가 아니어도 gcm 을 수신 할 수 있음)
                            sendResultToUser(mContext, "플레이스 초대", loginVo.getUserName() + "님, " + placeVo.getPlaceName() + " 플레이스에 초대되었습니다.");
                        }
                    }
                }
                memberService.insertDbMemberList(userVoList, placeVo);
                placeService.updateDbPlace(placeVo);
            } else if( num == ContextPathStore.REQUEST_UPDATE_USER){
                UserPlaceMappingVo userPlaceMappingVo = new Gson().fromJson(data, UserPlaceMappingVo.class);
                if (userPlaceMappingVo == null) {
                    return;
                }
                UserVo userVo = userPlaceMappingVo.getUserVo();
                PlaceVo changedPlaceVo = userPlaceMappingVo.getPlaceVo();
                String loginId = LoginService.loadLastLoginUser(mContext).getUserId();
                if( loginId.equalsIgnoreCase(userVo.getUserId())) {
                    new PlaceService(mContext).updateDbPlace(changedPlaceVo);

                    PlaceVo currentPlaceVo = PlaceService.loadLastPlace(mContext);
                    if( changedPlaceVo.getPlaceId().equalsIgnoreCase(currentPlaceVo.getPlaceId())) {
                        PlaceService.saveLastPlace(mContext, changedPlaceVo);
                    }
                }
                /*
                    MemberService service = new MemberService(mContext);if( service.insertDbMember(userVo)){
                    String loginId = LoginService.loadLastLoginUser(mContext).getUserId();
                    if( loginId.equalsIgnoreCase(userVo.getUserId())) {
                        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
                        placeVo.setAuth(String.valueOf(userVo.getAuth()));
                        PlaceService.saveLastPlace(mContext, placeVo);
                        new PlaceService(mContext).updateDbPlace(placeVo);
                    }
                }*/
            } else if( num == ContextPathStore.REQUEST_DELETE_USER){
                List<UserPlaceMappingVo> userPlaceMappingVoList = new Gson().fromJson(data, new TypeToken<List<UserPlaceMappingVo>>() {
                }.getType());
                if (userPlaceMappingVoList == null) {
                    return;
                }
                PlaceVo placeVo = userPlaceMappingVoList.get(0).getPlaceVo();
                List<UserVo> userVoList = new ArrayList<>();
                for(int voCnt=0; voCnt<userPlaceMappingVoList.size(); voCnt++){
                    userVoList.add(userPlaceMappingVoList.get(0).getUserVo());
                }
                MemberService memberService = new MemberService(mContext);
                memberService.deleteDbMemberList(userVoList, placeVo);

                UserVo loginVo = LoginService.loadLastLoginUser(mContext);
                for(int voCnt=0; voCnt<userVoList.size(); voCnt++) {
                    // 삭제된 사용자가 자신일 경우 해당 장소를 삭제한다.
                    if (userVoList.get(voCnt).getUserId().equalsIgnoreCase(loginVo.getUserId())){
                        PlaceVo currentPlaceVo = PlaceService.loadLastPlace(mContext);
                        PlaceService placeService = new PlaceService(mContext);
                        placeService.removeDbPlace(placeVo);

                        // 삭제된 장소가 현재 장소일 경우 메모리 DB를 초기화한다.
                        if( currentPlaceVo.getPlaceId().equalsIgnoreCase(placeVo.getPlaceId())){
                            PlaceService.saveLastPlace(mContext, null);
                        }
                    }
                }
            } else if (num == ContextPathStore.REQUEST_INSERT_PLUG) {
                List<PlugVo> plugVoList = new Gson().fromJson(data, new TypeToken<List<PlugVo>>() {
                }.getType());
                if (plugVoList == null) {
                    return;
                }
                PlugAllService service = new PlugAllService(mContext);
                service.insertDbPlugList(plugVoList);
            } else if (num == ContextPathStore.REQUEST_UPDATE_PLUG) {
                PlugVo plugVo = new Gson().fromJson(data, PlugVo.class);
                if (plugVo == null) {
                    return;
                }
                PlugAllService service = new PlugAllService(mContext);
                service.insertDbPlugList(Arrays.asList(plugVo));
            } else if (num == ContextPathStore.REQUEST_DELETE_PLUG) {
                List<PlugVo> plugVoList = new Gson().fromJson(data, new TypeToken<List<PlugVo>>() {
                }.getType());
                if (plugVoList == null) {
                    return;
                }
                PlugAllService service = new PlugAllService(mContext);
                service.deleteDbPlugList(plugVoList);
            } else if (num == ContextPathStore.REQUEST_INSERT_BLUETOOTH) {
                DbBluetoothVo dbBluetoothVo = new Gson().fromJson(data, DbBluetoothVo.class);
                if (dbBluetoothVo == null) {
                    return;
                }
                RegDeviceService service = new RegDeviceService(mContext);
                service.updateDbBluetooth(dbBluetoothVo);
            } else if( num == ContextPathStore.REQUEST_INSERT_GROUP ||
                    num == ContextPathStore.REQUEST_INSERT_GROUP_USER ){
                GroupVo groupVo = new Gson().fromJson(data, GroupVo.class);
                if (groupVo == null) {
                    return;
                }
                UserVo loginVo = LoginService.loadLastLoginUser(mContext);

                GroupService groupService = new GroupService(mContext);
                DbGroupUserMappingVo currentGroupUserMappingVo = groupService.selectDbGroupUserMapping(groupVo);
                if( currentGroupUserMappingVo == null){
                    sendResultToUser(mContext, "그룹 초대", loginVo.getUserName() + "님, " + groupVo.getGroupName() + " 그룹에 초대되었습니다.");
                }

                List<UserVo> userVoList = groupVo.getUserVoList();
                for( int cnt=0; cnt<userVoList.size(); cnt++){
                    UserVo userVo = userVoList.get(cnt);
                    if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){ // 수신한 그룹에 자신이 속해있을 경우 수정. (한 폰에 두개 이상의 gcm id 를 가질 수 있으므로 현재 로그인 된 사용자가 아니어도 gcm 을 수신 할 수 있음)
                        CreateGroupService service = new CreateGroupService(mContext);
                        service.insertDbGroup(groupVo);
                    }
                }
            } else if( num == ContextPathStore.REQUEST_INSERT_GROUP_PLUG){
                GroupVo groupVo = new Gson().fromJson(data, GroupVo.class);
                if (groupVo == null) {
                    return;
                }
                UserVo loginVo = LoginService.loadLastLoginUser(mContext);
                List<UserVo> userVoList = groupVo.getUserVoList();
                for( int cnt=0; cnt<userVoList.size(); cnt++){
                    UserVo userVo = userVoList.get(cnt);
                    if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){ // 수신한 그룹에 자신이 속해있을 경우 수정. (한 폰에 두개 이상의 gcm id 를 가질 수 있으므로 현재 로그인 된 사용자가 아니어도 gcm 을 수신 할 수 있음)
                        CreateGroupService service = new CreateGroupService(mContext);
                        service.insertDbGroup(groupVo);
                    }
                }

            } else if( num == ContextPathStore.REQUEST_UPDATE_GROUP ||
                    num == ContextPathStore.REQUEST_UPDATE_GROUP_USER){
                GroupVo groupVo = new Gson().fromJson(data, GroupVo.class);
                if (groupVo == null) {
                    return;
                }
                UserVo loginVo = LoginService.loadLastLoginUser(mContext);
                List<UserVo> userVoList = groupVo.getUserVoList();
                for( int cnt=0; cnt<userVoList.size(); cnt++){
                    UserVo userVo = userVoList.get(cnt);
                    if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){ // 수신한 그룹에 자신이 속해있을 경우 수정. (한 폰에 두개 이상의 gcm id 를 가질 수 있으므로 현재 로그인 된 사용자가 아니어도 gcm 을 수신 할 수 있음)
                        GroupService service = new GroupService(mContext);
                        service.updateDbGroup(groupVo);
                    }
                }

            } else if (num == ContextPathStore.REQUEST_DELETE_GROUP_LIST) {
                List<GroupVo> groupVoList = new Gson().fromJson(data, new TypeToken<List<GroupVo>>() {
                }.getType());
                if (groupVoList == null) {
                    return;
                }
                CreateGroupService createService = new CreateGroupService(mContext);

                for(int cnt=0; cnt<groupVoList.size(); cnt++){
                    GroupVo groupVo = groupVoList.get(cnt);
                    UserVo loginVo = LoginService.loadLastLoginUser(mContext);
                    List<UserVo> userVoList = groupVo.getUserVoList();
                    for( int ucnt=0; ucnt<userVoList.size(); ucnt++){
                        UserVo userVo = userVoList.get(ucnt);
                        if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){ // 수신한 그룹에 자신이 속해있을 경우 수정. (한 폰에 두개 이상의 gcm id 를 가질 수 있으므로 현재 로그인 된 사용자가 아니어도 gcm 을 수신 할 수 있음)
                            if( !createService.deleteDbGroup(groupVo)){
                                return;
                            }
                            if( !createService.insertDbGroup(groupVo)){
                                return;
                            }
                        }
                    }
                }
            } else if( num == ContextPathStore.REQUEST_DELETE_GROUP_PLUG){
                GroupVo groupVo = new Gson().fromJson(data, GroupVo.class);
                GroupService service = new GroupService(mContext);

                UserVo loginVo = LoginService.loadLastLoginUser(mContext);
                List<UserVo> userVoList = groupVo.getUserVoList();
                for( int cnt=0; cnt<userVoList.size(); cnt++){
                    UserVo userVo = userVoList.get(cnt);
                    if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){ // 수신한 그룹에 자신이 속해있을 경우 수정. (한 폰에 두개 이상의 gcm id 를 가질 수 있으므로 현재 로그인 된 사용자가 아니어도 gcm 을 수신 할 수 있음)
                        service.deleteDbGroupPlugList(groupVo, groupVo.getPlugVoList());
                    }
                }
            }
        } catch (JsonParseException jpe) {
            jpe.printStackTrace();
        } catch ( Exception ex){
            ex.printStackTrace();
        }
    }

    private void sendResultToUser(Context context, String title, String message) {
        Log.i("INFO", "[sendResultToUser] start");

        HashMap<String, String> extraMap = new HashMap<String, String>();
        NotificationUtil.sendNotification(context, IntroActivity.class, title, message, extraMap);

        Log.i("INFO", "[sendResultToUser] end");
    }
}
