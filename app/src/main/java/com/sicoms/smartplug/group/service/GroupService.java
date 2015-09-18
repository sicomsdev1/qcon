package com.sicoms.smartplug.group.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.support.v7.app.ActionBarActivity;

import com.csr.mesh.PowerModelApi;
import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.common.SPConfig;
import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbGroupPlugMappingVo;
import com.sicoms.smartplug.dao.DbGroupUserMappingVo;
import com.sicoms.smartplug.dao.DbGroupPlugMappingVoDao;
import com.sicoms.smartplug.dao.DbGroupUserMappingVoDao;
import com.sicoms.smartplug.dao.DbGroupVo;
import com.sicoms.smartplug.dao.DbGroupVoDao;
import com.sicoms.smartplug.dao.DbPlaceVo;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.group.interfaces.EditGroupResultCallbacks;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.bluetooth.util.DeviceController;
import com.sicoms.smartplug.network.bluetooth.util.GroupListener;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.AsyncHttpUploadFile;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.network.udp.UDPClient;
import com.sicoms.smartplug.plug.service.PlugUDPService;
import com.sicoms.smartplug.util.SPUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 20..
 */
public class GroupService implements UDPClient.UDPResponseCallbacks, GroupListener {
    private static final String TAG = GroupService.class.getSimpleName();
    private final String ARG_GROUP_NAME = "group";
    private final String ARG_GROUP_LAST_NAME = "group_last";

    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mCallbacks;
    private EditGroupResultCallbacks mGroupCallbacks;

    private List<PlugVo> mUnGroupedPlugVoList;

    public GroupService(Context activity){
        mContext = activity;
        mDBHelper = new DBHelper(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
    public void setOnGroupResultCallbacks(EditGroupResultCallbacks callbacks){
        mGroupCallbacks = callbacks;
    }

    /*
     * Cloud Server Method
     */
    public void requestUpdateGroup(GroupVo groupVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_GROUP;
        try{
            if( groupVo == null){
                return;
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(groupVo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("placeId", placeVo.getPlaceId());

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_GROUP, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestUpdateGroupUserMapping(GroupVo groupVo, UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_GROUP_USER;
        try{
            if( groupVo == null){
                return;
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(groupVo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("userId", userVo.getUserId());
            params.put("auth", userVo.getAuth());
            params.put("placeId", placeVo.getPlaceId());

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_GROUP_USER, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestInsertGroupPlugMapping(GroupVo groupVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_INSERT_GROUP_PLUG;
        try{
            if( groupVo == null){
                return;
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(groupVo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("placeId", placeVo.getPlaceId());

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_INSERT_GROUP_PLUG, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestInsertGroupUserMapping(GroupVo groupVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_INSERT_GROUP_USER;
        try{
            if( groupVo == null){
                return;
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(groupVo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("placeId", placeVo.getPlaceId());

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_INSERT_GROUP_USER, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestDeleteGroupPlugList(GroupVo groupVo, List<PlugVo> plugVoList){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_DELETE_GROUP_PLUG;
        try{
            if( groupVo == null){
                return;
            }
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(plugVoList);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("placeId", placeVo.getPlaceId());
            params.put("groupId", groupVo.getGroupId());

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_DELETE_GROUP_PLUG, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /*
     * Local DB Method
     */
    public DbGroupUserMappingVo selectDbGroupUserMapping(GroupVo groupVo){
        UserVo loginVo = LoginService.loadLastLoginUser(mContext);
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            DbGroupUserMappingVo theDbVo = theDao.queryBuilder().where(DbGroupUserMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId()))
                                                                    .where(DbGroupUserMappingVoDao.Properties.UserId.eq(loginVo.getUserId()))
                                                                    .unique();

            mDBHelper.closeSession();
            return theDbVo;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        mDBHelper.closeSession();
        return null;
    }

    public boolean updateDbGroup(GroupVo groupVo){
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
                if (!updateDbGroupUserMapping(daoSession, groupVo, userVoList)) {
                    return false;
                }
            }
            List<PlugVo> plugVoList = groupVo.getPlugVoList();
            if( plugVoList != null) {
                if (!updateDbGroupPlugMapping(daoSession, groupVo, plugVoList)) {
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

    public boolean updateDbGroupPlugMapping(DaoSession daoSession, GroupVo groupVo, List<PlugVo> plugVoList){
        try{
            if( daoSession == null) {
                daoSession = mDBHelper.getSession(true);
            }
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupPlugMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();
            List<DbGroupPlugMappingVo> dbGroupPlugMappingVoList = new ArrayList<>();

            for( PlugVo plugVo : plugVoList){
                DbGroupPlugMappingVo dbGroupPlugMappingVo = new DbGroupPlugMappingVo();
                dbGroupPlugMappingVo.setGroupId(Long.parseLong(groupVo.getGroupId()));
                dbGroupPlugMappingVo.setPlugId(plugVo.getPlugId());
                dbGroupPlugMappingVo.setWh(Float.parseFloat(plugVo.getWh()));
                dbGroupPlugMappingVoList.add(dbGroupPlugMappingVo);
            }

            theDao.deleteInTx(theDbVoList);
            theDao.insertOrReplaceInTx(dbGroupPlugMappingVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDbGroupUserMapping(DaoSession daoSession, GroupVo groupVo, List<UserVo> userVoList){
        try{
            if( daoSession == null) {
                daoSession = mDBHelper.getSession(true);
            }
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            List<DbGroupUserMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupUserMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();
            List<DbGroupUserMappingVo> dbGroupUserMappingVoList = new ArrayList<>();

            for( UserVo userVo : userVoList){
                DbGroupUserMappingVo dbGroupUserMappingVo = new DbGroupUserMappingVo();
                dbGroupUserMappingVo.setGroupId(Long.parseLong(groupVo.getGroupId()));
                dbGroupUserMappingVo.setUserId(userVo.getUserId());
                dbGroupUserMappingVo.setAuth(String.valueOf(userVo.getAuth()));
                dbGroupUserMappingVoList.add(dbGroupUserMappingVo);
            }

            theDao.deleteInTx(theDbVoList);
            theDao.insertOrReplaceInTx(dbGroupUserMappingVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean insertDbGroupPlugMapping(GroupVo groupVo, List<PlugVo> plugVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupPlugMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();
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

    public boolean insertDbGroupUserMapping(GroupVo groupVo, List<UserVo> userVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            List<DbGroupUserMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupUserMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();
            List<DbGroupUserMappingVo> dbGroupUserMappingVoList = new ArrayList<>();

            for( UserVo userVo : userVoList){
                DbGroupUserMappingVo dbGroupUserMappingVo = new DbGroupUserMappingVo();
                dbGroupUserMappingVo.setGroupId(Long.parseLong(groupVo.getGroupId()));
                dbGroupUserMappingVo.setUserId(userVo.getUserId());
                dbGroupUserMappingVo.setAuth(String.valueOf(userVo.getAuth()));
                dbGroupUserMappingVoList.add(dbGroupUserMappingVo);
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

    public boolean deleteDbGroupPlugList(GroupVo groupVo, List<PlugVo> plugVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupPlugMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();
            List<DbGroupPlugMappingVo> deleteDbVoList = new ArrayList<>();
            for( int cnt=0; cnt<theDbVoList.size(); cnt++){
                DbGroupPlugMappingVo dbVo = theDbVoList.get(cnt);
                for(int plugCnt=0; plugCnt<plugVoList.size(); plugCnt++){
                    String deletePlugId = plugVoList.get(plugCnt).getPlugId();
                    if( dbVo.getPlugId().equalsIgnoreCase(deletePlugId)){
                        deleteDbVoList.add(dbVo);
                    }
                }
            }

            theDao.deleteInTx(deleteDbVoList);
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

    public boolean deleteDbGroupPlug(String groupId, PlugVo plugVo){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupPlugMappingVoDao.Properties.GroupId.eq(groupId)).list();
            List<DbGroupPlugMappingVo> deleteDbVoList = new ArrayList<>();
            for( int cnt=0; cnt<theDbVoList.size(); cnt++){
                DbGroupPlugMappingVo dbVo = theDbVoList.get(cnt);
                String deletePlugId = plugVo.getPlugId();
                if( dbVo.getPlugId().equalsIgnoreCase(deletePlugId)){
                    deleteDbVoList.add(dbVo);
                }
            }
            theDao.deleteInTx(deleteDbVoList);
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

    /*
     * Device Control Method
     */
    public void controlOnOffTypeGateway(List<PlugVo> plugVoList, String onoff){
        PlugUDPService udpService = new PlugUDPService(mContext);
        udpService.setOnUDPResponseCallbacks(this);
        udpService.requestAllOnOffMessageTypeGW(plugVoList, onoff);
    }

    public void controlOnOffTypeRouter(List<PlugVo> plugVoList, String onoff){
        PlugUDPService udpService = new PlugUDPService(mContext);
        udpService.setOnUDPResponseCallbacks(this);
        udpService.requestAllOnOffMessageTypeRouter(plugVoList, onoff);
    }

    public void controlOnOffTypeBluetooth(List<PlugVo> plugVoList, boolean isOn){
        try {
            if( !MainActivity.stBluetoothManager.isConnected()){
                SPUtil.showToast(mContext, "블루투스에 연결되지 않았습니다.");
                return;
            }
            for( int cnt=0; cnt<plugVoList.size(); cnt++) {
                PlugVo plugVo = plugVoList.get(cnt);
                int deviceId = Integer.parseInt(plugVo.getUuid());
                PowerModelApi.PowerState onoffState = isOn ? PowerModelApi.PowerState.ON : PowerModelApi.PowerState.OFF;
                MainActivity.stBluetoothManager.setSelectedDeviceId(deviceId);
                MainActivity.stBluetoothManager.setLightPower(onoffState);
            }
//            DeviceController controller = MainActivity.stBluetoothManager;
//
//            int deviceId = Integer.parseInt(groupId);
//            if (deviceId == 0) {
//                SPUtil.showToast(mContext, "Bluetooth 그룹 정보를 가져오는데 실패하였습니다.");
//                return;
//            }
//            if (!MainActivity.stBluetoothManager.isConnected()) {
//                SPUtil.showToast(mContext, "블루투스와 연결되지 않았습니다.");
//            }
//            controller.setSelectedDeviceId(deviceId);
//            controller.setLightPower(isStartOn ? PowerModelApi.PowerState.ON : PowerModelApi.PowerState.OFF);
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
        }
    }
    /*
     * Memory DB Method
     */
    // Navigation Fragment 에 전달하기 위해
    public void saveLastGroupVo(GroupVo groupVo){
        SharedPreferences preference = mContext.getSharedPreferences(ARG_GROUP_NAME, 0);
        SharedPreferences.Editor edit = preference.edit();

        edit.putString(ARG_GROUP_LAST_NAME, new Gson().toJson(groupVo));
        edit.commit();
    }

    public GroupVo loadLastGroup(){
        SharedPreferences preference = mContext.getSharedPreferences(ARG_GROUP_NAME, 0);
        GroupVo groupVo = new Gson().fromJson(preference.getString(ARG_GROUP_LAST_NAME, ""), GroupVo.class);
        if( groupVo == null){
            groupVo = new GroupVo();
        }
        return groupVo;
    }

    /*
     * Bluetooth
     */
    @Override
    public void groupsUpdated(int deviceId, boolean success, String msg) {
        if (success) {
            if( mUnGroupedPlugVoList == null || mUnGroupedPlugVoList.size() < 1){
                if( mGroupCallbacks != null) {
                    mGroupCallbacks.onCompleteEditPlug();
                    ((ActionBarActivity) mContext).getSupportFragmentManager().popBackStack();
                }
            } else {
                PlugVo plugVo = mUnGroupedPlugVoList.get(0);
                mUnGroupedPlugVoList.remove(plugVo);
                setGroup(plugVo);
            }
        } else {
            SPUtil.showToast(mContext, "Bluetooth 그룹을 수정하지 못했습니다.");
        }
    }

    private List<Integer> mGroupIdList;
    public boolean setGroup(PlugVo plugVo){
        int blAddress = Integer.parseInt(plugVo.getUuid());

        Integer editGroupId = 0;
        try {
            editGroupId = Integer.parseInt(mGroupVo.getGroupId());
        } catch (NumberFormatException nfe){
            nfe.printStackTrace();
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

        mGroupIdList = new CreateGroupService(mContext).selectDbGroupIdList(plugVo);
        for(int cnt=0; cnt<mGroupIdList.size(); cnt++){
            if( mGroupIdList.get(cnt) == editGroupId){
                mGroupIdList.remove(editGroupId);
            }
        }
        if( !mIsRemove) {
            mGroupIdList.add(editGroupId);
        }
        if( mGroupIdList == null){
            return false;
        }

        GroupList groupList = new GroupList(blAddress);
        groupList.groupIdList.addAll(mGroupIdList);

        MainActivity.stBluetoothManager.setSelectedDeviceId(groupList.deviceId);
        MainActivity.stBluetoothManager.setDeviceGroups(groupList.groupIdList, this);

        return true;
    }

    private GroupVo mGroupVo;
    private boolean mIsRemove = false;
    public boolean editMeshGroup(GroupVo groupVo, List<PlugVo> plugVoList){
        // Bluetooth 그룹 수정
        if( !MainActivity.stBluetoothManager.isConnected()){
            SPUtil.showToast(mContext, "블루투스에 연결되지 않았습니다.");
            return false;
        }

        mGroupVo = groupVo;
        mIsRemove = false;
        mUnGroupedPlugVoList = new ArrayList<>();
        for( int cnt=0; cnt<plugVoList.size(); cnt++) {
            mUnGroupedPlugVoList.add(plugVoList.get(cnt));
        }
        PlugVo editPlugVo = mUnGroupedPlugVoList.get(0);
        mUnGroupedPlugVoList.remove(0);

        setGroup(editPlugVo);

        return true;
    }

    public boolean removeMeshGroup(GroupVo groupVo, List<PlugVo> plugVoList){
        // Bluetooth 그룹 수정
        if( !MainActivity.stBluetoothManager.isConnected()){
            SPUtil.showToast(mContext, "블루투스에 연결되지 않았습니다.");
            return false;
        }

        mGroupVo = groupVo;
        mIsRemove = true;
        mUnGroupedPlugVoList = new ArrayList<>();
        for( int cnt=0; cnt<plugVoList.size(); cnt++) {
            mUnGroupedPlugVoList.add(plugVoList.get(cnt));
        }
        PlugVo editPlugVo = mUnGroupedPlugVoList.get(0);
        mUnGroupedPlugVoList.remove(0);

        setGroup(editPlugVo);

        return true;
    }

    @Override
    public void onUDPResponseResultStatus(int result, String response) {

    }

    private class GroupList {
        public int deviceId;
        public ArrayList<Integer> groupIdList = new ArrayList<Integer>();

        public GroupList(int deviceId) {
            this.deviceId = deviceId;
        }
    }
}
