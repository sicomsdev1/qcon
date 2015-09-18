package com.sicoms.smartplug.group.service;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbGroupPlugMappingVo;
import com.sicoms.smartplug.dao.DbGroupPlugMappingVoDao;
import com.sicoms.smartplug.dao.DbGroupUserMappingVo;
import com.sicoms.smartplug.dao.DbGroupUserMappingVoDao;
import com.sicoms.smartplug.dao.DbGroupVo;
import com.sicoms.smartplug.dao.DbGroupVoDao;
import com.sicoms.smartplug.dao.DbPlaceVo;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.interfaces.CreateGroupResultCallbacks;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.bluetooth.util.Device;
import com.sicoms.smartplug.network.bluetooth.util.GroupListener;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.AssociatedDevicesResultCallbacks;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 15..
 */
public class CreateGroupService implements AssociatedDevicesResultCallbacks, GroupListener {
    private static final String TAG = CreateGroupService.class.getSimpleName();
    private final String ARG_GROUP_NAME = "group";
    private final String ARG_CREATE_GROUP = "create_group";

    private Activity mActivity;
    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mHttpCallbacks;
    private CreateGroupResultCallbacks mGroupCallbacks;
    private List<String> mCheckedPlugId;

    private Device mNewGroup;
    private List<PlugVo> mUnGroupedPlugVoList;

    public CreateGroupService(Activity activity){
        mActivity = activity;
        mContext = mActivity;
        mDBHelper = new DBHelper(mContext);
    }
    public CreateGroupService(Context context){
        mContext = context;
        mDBHelper = new DBHelper(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mHttpCallbacks = callbacks;
    }
    public void setOnGroupResultCallbacks(CreateGroupResultCallbacks callbacks){
        mGroupCallbacks = callbacks;
    }

    /*
     * Cloud Server Method
     */
    public void requestInsertCreateGroup(GroupVo groupVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_INSERT_GROUP;
        try{
            if( groupVo == null){
                return;
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(groupVo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("placeId", placeVo.getPlaceId());

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_INSERT_GROUP, "", params);
            if(mHttpCallbacks != null){
                post.setOnHttpResponseCallbacks(mHttpCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /*
     * Local DB Method
     */
    public boolean insertDbGroup(GroupVo groupVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao theDao = daoSession.getDbGroupVoDao();
            DbGroupVo dbGroupVo = new DbGroupVo();

            UserVo userVo = LoginService.loadLastLoginUser(mContext);
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            DbPlaceVo dbPlaceVo = new DbPlaceVo(placeVo.getPlaceId(), placeVo.getPlaceName(), placeVo.getPlaceImg(), placeVo.getAddress(), placeVo.getCoordinate(), placeVo.getAuth(), placeVo.getPlugCount(), placeVo.getMemberCount());
            dbGroupVo.setDbPlaceVo(dbPlaceVo);
            dbGroupVo.setPlaceId(placeVo.getPlaceId());
            dbGroupVo.setGroupId(Long.parseLong(groupVo.getGroupId()));
            dbGroupVo.setGroupName(groupVo.getGroupName());
            dbGroupVo.setGroupImg(groupVo.getGroupIconImg());
            dbGroupVo.setSuperId(userVo.getUserId());

            List<UserVo> userVoList = groupVo.getUserVoList();
            if( userVoList != null) {
                if (!insertDbGroupUserMapping(daoSession, groupVo, userVoList)) {
                    return false;
                }
            }
            List<PlugVo> plugVoList = groupVo.getPlugVoList();
            if( plugVoList != null) {
                if (!insertDbGroupPlugMapping(daoSession, groupVo, plugVoList)) {
                    return false;
                }
            }

            theDao.insertOrReplace(dbGroupVo);
            mDBHelper.closeSession();

            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        mDBHelper.closeSession();
        return false;
    }

    public boolean insertDbGroupUserMapping(DaoSession daoSession, GroupVo groupVo, List<UserVo> userVoList){
        try{
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            List<DbGroupUserMappingVo> dbGroupUserMappingVoList = new ArrayList<>();

            UserVo loginVo = LoginService.loadLastLoginUser(mContext);
            for( int cnt=0; cnt<userVoList.size(); cnt++){
                UserVo userVo = userVoList.get(cnt);
                int auth = SPConfig.MEMBER_USER;
                if( loginVo.getUserId().equalsIgnoreCase(userVo.getUserId())){
                    auth = SPConfig.MEMBER_MASTER;
                }
                DbGroupUserMappingVo groupUserMappingVo = new DbGroupUserMappingVo();
                groupUserMappingVo.setGroupId(Long.parseLong(groupVo.getGroupId()));
                groupUserMappingVo.setUserId(userVo.getUserId());
                groupUserMappingVo.setAuth(String.valueOf(auth));
                dbGroupUserMappingVoList.add(groupUserMappingVo);
            }

            theDao.insertOrReplaceInTx(dbGroupUserMappingVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public boolean insertDbGroupPlugMapping(DaoSession daoSession, GroupVo groupVo, List<PlugVo> plugVoList){
        try{
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> dbGroupPlugMappingVoList = new ArrayList<>();

            for( PlugVo plugVo : plugVoList){
                DbGroupPlugMappingVo dbGroupPlugMappingVo = new DbGroupPlugMappingVo();
                dbGroupPlugMappingVo.setGroupId(Long.parseLong(groupVo.getGroupId()));
                dbGroupPlugMappingVo.setPlugId(plugVo.getPlugId());
                dbGroupPlugMappingVo.setWh(Float.parseFloat(plugVo.getWh()));
                dbGroupPlugMappingVoList.add(dbGroupPlugMappingVo);
            }

            theDao.insertOrReplaceInTx(dbGroupPlugMappingVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteDbGroup(GroupVo groupVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao theDao = daoSession.getDbGroupVoDao();
            DbGroupVo dbGroupVo = theDao.queryBuilder().where(DbGroupVoDao.Properties.GroupId.eq(groupVo.getGroupId())).unique();

            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);

            List<UserVo> userVoList = groupVo.getUserVoList();
            if( !deleteDbGroupUserMapping(daoSession, groupVo, userVoList, placeVo)){
                return false;
            }
            List<PlugVo> plugVoList = groupVo.getPlugVoList();
            if( !deleteDbGroupPlugMapping(daoSession, groupVo, plugVoList, placeVo)){
                return false;
            }

            theDao.delete(dbGroupVo);
            mDBHelper.closeSession();

            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        mDBHelper.closeSession();
        return false;
    }
    public boolean deleteDbGroupUserMapping(DaoSession daoSession, GroupVo groupVo, List<UserVo> userVoList, PlaceVo placeVo){
        try{
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            List<DbGroupUserMappingVo> dbGroupUserMappingVoList = theDao.queryBuilder().where(DbGroupUserMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();

            theDao.deleteInTx(dbGroupUserMappingVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public boolean deleteDbGroupPlugMapping(DaoSession daoSession, GroupVo groupVo, List<PlugVo> plugVoList, PlaceVo placeVo){
        try{
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> dbGroupPlugMappingVoList = theDao.queryBuilder().where(DbGroupPlugMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();

            theDao.deleteInTx(dbGroupPlugMappingVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public List<Integer> selectDbGroupIdList(PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupPlugMappingVoDao.Properties.PlugId.eq(plugVo.getPlugId())).list();

            List<Integer> groupIdList = new ArrayList<>();
            for( int cnt=0; cnt<theDbVoList.size(); cnt++){
                groupIdList.add((int)theDbVoList.get(cnt).getGroupId());
            }

            mDBHelper.closeSession();
            return groupIdList;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        mDBHelper.closeSession();
        return null;
    }

    /*
     * Bluetooth
     */
    public void setGroupDevice(int groupId, String groupName){
        mNewGroup = MainActivity.stBluetoothManager.addLightGroup(groupName);
        if( groupId != 0){ // 0: Create Group Num
            mNewGroup.setDeviceId(groupId);
        }
    }

    @Override
    public void groupsUpdated(int deviceId, boolean success, String msg) {
        if (success) {
            if( mUnGroupedPlugVoList == null || mUnGroupedPlugVoList.size() < 1){
                if( mGroupCallbacks != null) {
                    mGroupCallbacks.onCompleteCreateBLGroup(mNewGroup.getDeviceId(), success);
                }
            } else {
                PlugVo plugVo = mUnGroupedPlugVoList.get(0);
                mUnGroupedPlugVoList.remove(plugVo);
                setGroup(plugVo);
            }
        } else {
            if( mGroupCallbacks != null) {
                mGroupCallbacks.onCompleteCreateBLGroup(0, success);
            }
        }
    }

    public boolean setGroup(PlugVo plugVo){
        int uuid = Integer.parseInt(plugVo.getUuid());
        List<Integer> groupIdList = selectDbGroupIdList(plugVo);
        if( groupIdList == null){
            return false;
        }
        if( mNewGroup != null){
            groupIdList.add(mNewGroup.getDeviceId());
        }

        GroupList groupList = new GroupList(uuid);
        groupList.groupIdList.addAll(groupIdList);

        MainActivity.stBluetoothManager.setSelectedDeviceId(groupList.deviceId);
        MainActivity.stBluetoothManager.setDeviceGroups(groupList.groupIdList, this);

        return true;
    }

    public boolean createMeshGroup(GroupVo groupVo, List<PlugVo> plugVoList){

        if( !MainActivity.stBluetoothManager.isConnected()){
            SPUtil.showToast(mActivity, "블루투스에 연결되지 않았습니다.");
            return false;
        }

        // Bluetooth 그룹 생성
        try {
            String createGroupName = groupVo.getGroupName();
            setGroupDevice(0, createGroupName); // 0 : Create Group Num
            mUnGroupedPlugVoList = plugVoList;
            setGroup(mUnGroupedPlugVoList.get(0));
            mUnGroupedPlugVoList.remove(0);

            //groupVo.setGroupId(String.valueOf(mNewGroup.getDeviceId()));
            return true;
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void onGetAssociatedDevice(String plugId, List<String> groupIdList) {
        if( groupIdList.size() >= 4){
            SPUtil.showToast(mContext, "하나의 Bluetooth 플러그는 최대 3개까지 그룹을 맺을 수 있습니다.");
            return;
        }
//        // Group 검사 다 끝났을 경우 그룹 생성
//        mCheckedPlugId.add(plugId);
//        int checkCount = 0;
//        if( mCheckedPlugId.size() == mGroupPlugVoList.size()){
//            for(int cnt=0; cnt<mGroupPlugVoList.size(); cnt++){
//                for( int idCnt=0; idCnt<mCheckedPlugId.size(); idCnt++){
//                    if( mCheckedPlugId.get(idCnt).equalsIgnoreCase(mGroupPlugVoList.get(cnt).getPlugId())){
//                        checkCount++;
//                    }
//                }
//            }
//            if( mGroupPlugVoList.size() != checkCount){
//                return;
//            }
//        } else {
//            return;
//        }
//
//        // Bluetooth 그룹 생성
//        if( !MainActivity.stBluetoothManager.isConnected()){
//            SPUtil.showToast(mActivity, "블루투스에 연결되지 않았습니다.");
//            return;
//        }
//        DeviceController controller = MainActivity.stBluetoothManager;
//        String createGroupId = mCreateGroupVo.getGroupName();
//        Device newGroup = controller.addLightGroup(createGroupId);
//
//        for(DbPlugVo plugVo : mGroupPlugVoList) {
//            int blAddress = Integer.parseInt(plugVo.getUuid());
//            List<Integer> deviceIdList = selectDbGroupIdList(plugVo);
//            for(int groupCnt=0; groupCnt<groupIdList.size(); groupCnt++) {
//                try {
//                    int existedGroupId = Integer.parseInt(groupIdList.get(groupCnt));
//                    deviceIdList.add(existedGroupId);
//                } catch (NumberFormatException nfe){
//                    nfe.printStackTrace();
//                }
//            }
//            deviceIdList.add(newGroup.getDeviceId());
//
//            GroupList groupList = new GroupList(blAddress);
//            groupList.groupIdList.addAll(deviceIdList);
//
//            if (createGroupId != null) {
//                controller.setSelectedDeviceId(groupList.deviceId);
//                controller.setDeviceGroups(groupList.groupIdList, this);
//            }
//        }
//        mCreateGroupVo.setGroupId(String.valueOf(newGroup.getDeviceId()));
//        if (insertDbGroup(mCreateGroupVo)) {
//            Toast.makeText(mActivity, "그룹을 생성하였습니다.", Toast.LENGTH_SHORT).show();
//            mActivity.finish();
//        } else {
//            Toast.makeText(mActivity, "그룹을 생성하지 못했습니다.", Toast.LENGTH_SHORT).show();
//        }
    }

    private class GroupList {
        public int deviceId;
        public ArrayList<Integer> groupIdList = new ArrayList<Integer>();

        public GroupList(int deviceId) {
            this.deviceId = deviceId;
        }
    }
    /*
     * Memory DB Method
     */
    public void saveCreateGroup(GroupVo vo){
        SharedPreferences preference = mContext.getSharedPreferences(ARG_GROUP_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        edit.putString(ARG_CREATE_GROUP, new Gson().toJson(vo));
        edit.commit();
    }

    public GroupVo loadCreateGroup(){
        SharedPreferences preference = mContext.getSharedPreferences(ARG_GROUP_NAME, 0);
        GroupVo groupVo = new Gson().fromJson(preference.getString(ARG_CREATE_GROUP, ""), GroupVo.class);
        if( groupVo == null){
            groupVo = new GroupVo();
        }
        return groupVo;
    }
}
