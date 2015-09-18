package com.sicoms.smartplug.group.service;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Handler;

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
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.dao.DbPlugVo;
import com.sicoms.smartplug.dao.DbPlugVoDao;
import com.sicoms.smartplug.dao.DbUserVo;
import com.sicoms.smartplug.dao.DbUserVoDao;
import com.sicoms.smartplug.domain.GroupVo;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.login.service.LoginService;
import com.sicoms.smartplug.main.activity.MainActivity;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.bluetooth.BLMessage;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;
import com.sicoms.smartplug.plug.interfaces.AssociatedDevicesResultCallbacks;
import com.sicoms.smartplug.plug.service.PlugAllService;
import com.sicoms.smartplug.util.SPUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gudnam on 2015. 6. 15..
 */
public class GroupAllService implements AssociatedDevicesResultCallbacks {
    private static final String TAG = GroupAllService.class.getSimpleName();
    private final String ARG_GROUP_NAME = "group";
    private final String ARG_CREATE_GROUP = "create_group";

    private Context mContext;
    private DBHelper mDBHelper;
    private HttpResponseCallbacks mCallbacks;

    public GroupAllService(Context context){
        mContext = context;
        mDBHelper= new DBHelper(mContext);
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /*
     * Cloud Server Method
     */
    public void requestSelectGroupList(){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_SELECT_GROUP_LIST;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(placeVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_SELECT_GROUP_LIST, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestDeleteGroupList(List<GroupVo> groupVoList){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_DELETE_GROUP_LIST;
        try {
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(groupVoList);

            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            params.add("placeId", placeVo.getPlaceId());
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_DELETE_GROUP_LIST, "", params);
            if (mCallbacks != null) {
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
    public List<GroupVo> selectDbGroupList(){
        List<GroupVo> groupVoList = new ArrayList<>();
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao dbGroupVoDao = daoSession.getDbGroupVoDao();
            List<DbGroupVo> dbGroupVoList = dbGroupVoDao.queryBuilder()
                    .where(DbPlugVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();

            if (dbGroupVoList != null) {
                for (DbGroupVo dbGroupVo : dbGroupVoList) {
                    List<PlugVo> plugVoList = selectDbGroupPlugList(daoSession, dbGroupVo);
                    float wh = 0;
                    boolean isOn = false;
                    if( plugVoList != null) {
                        for (PlugVo plugVo : plugVoList) {
                            wh += Float.parseFloat(plugVo.getWh());
                            if( plugVo.isOn()){
                                isOn = plugVo.isOn(); // 하나라도 On 상태가 있다면 해당 그룹은 On상태
                            }
                        }
                    }
                    GroupVo groupVo = new GroupVo(String.valueOf(dbGroupVo.getGroupId()), dbGroupVo.getGroupImg(), dbGroupVo.getGroupName(), String.valueOf(wh), isOn);
                    groupVo.setPlugVoList(plugVoList);

                    List<UserVo> userVoList = selectDbGroupUserList(daoSession, dbGroupVo);
                    groupVo.setUserVoList(userVoList);
                    groupVoList.add(groupVo);
                }

                mDBHelper.closeSession();
                return groupVoList;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }
    private DbGroupVo selectDbGroup(DaoSession daoSession, Long groupId){
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try {
            DbGroupVoDao dbGroupVoDao = daoSession.getDbGroupVoDao();
            DbGroupVo dbGroupVo = dbGroupVoDao.queryBuilder()
                    .where(DbGroupVoDao.Properties.GroupId.eq(groupId))
                    .where(DbGroupVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).unique();

            if (dbGroupVo != null) {
                return dbGroupVo;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public GroupVo selectDbGroup(Long groupId){
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try {
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao dbGroupVoDao = daoSession.getDbGroupVoDao();
            DbGroupVo dbGroupVo = dbGroupVoDao.queryBuilder()
                    .where(DbGroupVoDao.Properties.GroupId.eq(groupId))
                    .where(DbGroupVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).unique();

            if (dbGroupVo != null) {
                List<PlugVo> plugVoList = selectDbGroupPlugList(daoSession, dbGroupVo);
                float wh = 0;
                if( plugVoList != null) {
                    for (PlugVo plugVo : plugVoList) {
                        wh += Float.parseFloat(plugVo.getWh());
                    }
                }
                GroupVo groupVo = new GroupVo(String.valueOf(dbGroupVo.getGroupId()), dbGroupVo.getGroupImg(), dbGroupVo.getGroupName(), String.valueOf(wh), false);
                groupVo.setPlugVoList(plugVoList);

                List<UserVo> userVoList = selectDbGroupUserList(daoSession, dbGroupVo);
                groupVo.setUserVoList(userVoList);

                mDBHelper.closeSession();

                return groupVo;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        mDBHelper.closeSession();
        return null;
    }
    public List<PlugVo> selectDbGroupPlugList(DaoSession daoSession, DbGroupVo dbGroupVo){
        List<PlugVo> plugVoList = new ArrayList<>();
        try {
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> theDbVoList = theDao.queryBuilder()
                    .where(DbGroupPlugMappingVoDao.Properties.GroupId.eq(dbGroupVo.getGroupId())).list();

            if (theDbVoList != null) {
                int offset = 0;
                for (DbGroupPlugMappingVo vo : theDbVoList) {
                    DbPlugVoDao dbPlugVoDao = daoSession.getDbPlugVoDao();
                    DbPlugVo dbPlugVo = dbPlugVoDao.queryBuilder().where(DbPlugVoDao.Properties.PlugId.eq(vo.getPlugId())).unique();

                    // Group 할 수 있는 타입은 Gateway, Router, Bluetooth
                    PlugVo plugVo = new PlugVo(dbPlugVo.getPlugName(), dbPlugVo.getPlugId(), dbPlugVo.getPlugType(), dbPlugVo.getPlugImg(), false);
                    PlugAllService service = new PlugAllService(mContext);
                    DbLastDataVo dbLastDataVo = service.selectDbLastData(plugVo);
                    if( dbLastDataVo != null) {
                        plugVo.setIsOn(dbLastDataVo.getOnOff().equalsIgnoreCase(SPConfig.STATUS_ON));
                    }
                    plugVo.setBssid(dbPlugVo.getBssId());
                    plugVo.setRouterIp(dbPlugVo.getRouterIp());
                    plugVo.setGatewayIp(dbPlugVo.getGatewayIp());
                    plugVo.setUuid(dbPlugVo.getUuid());

                    plugVoList.add(plugVo);
                }

                return plugVoList;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public List<UserVo> selectDbGroupUserList(DaoSession daoSession, DbGroupVo dbGroupVo){
        List<UserVo> userVoList = new ArrayList<>();
        try {
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            List<DbGroupUserMappingVo> theDbVoList = theDao.queryBuilder()
                    .where(DbGroupUserMappingVoDao.Properties.GroupId.eq(String.valueOf(dbGroupVo.getGroupId()))).list();

            if (theDbVoList != null) {
                for (DbGroupUserMappingVo vo : theDbVoList) {
                    DbUserVoDao dbUserVoDao = daoSession.getDbUserVoDao();
                    DbUserVo dbUserVo = dbUserVoDao.queryBuilder().where(DbUserVoDao.Properties.UserId.eq(vo.getUserId())).unique();
                    DbGroupUserMappingVoDao dbGroupUserVoDao = daoSession.getDbGroupUserMappingVoDao();
                    DbGroupUserMappingVo dbGroupUserMappingVo = dbGroupUserVoDao.queryBuilder().where(DbGroupUserMappingVoDao.Properties.GroupId.eq(dbGroupVo.getGroupId()))
                                                                                                .where(DbGroupUserMappingVoDao.Properties.UserId.eq(vo.getUserId()))
                                                                                                .unique();
                    try {
                        userVoList.add(new UserVo(vo.getUserId(), "", dbUserVo.getUserName(), Integer.parseInt(dbGroupUserMappingVo.getAuth()), dbUserVo.getProfileImg(), true));
                    } catch (NumberFormatException nfe){
                        nfe.printStackTrace();
                    }
                }

                return userVoList;
            }
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteDbGroupList(List<GroupVo> groupVoList){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao theDao = daoSession.getDbGroupVoDao();
            List<DbGroupVo> theDbVoList = new ArrayList<>();

            for( GroupVo groupVo : groupVoList){
                DbGroupVo dbGroupVo = selectDbGroup(daoSession, Long.parseLong(groupVo.getGroupId()));
                theDbVoList.add(dbGroupVo);

                if( !deleteDbGroupUserMappingList(daoSession, dbGroupVo.getGroupId())){
                    return false;
                }
                if( !deleteDbGroupPlugMappingList(daoSession, dbGroupVo.getGroupId())){
                    return false;
                }
            }

            theDao.deleteInTx(theDbVoList);
            mDBHelper.closeSession();

            return true;
        } catch(SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    private boolean deleteDbGroupPlugMappingList(DaoSession daoSession, Long groupId){
        try{
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupPlugMappingVoDao.Properties.GroupId.eq(groupId)).list();
            if( theDbVoList == null){
                return true;
            }
            theDao.deleteInTx(theDbVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    private boolean deleteDbGroupUserMappingList(DaoSession daoSession, Long groupId){
        try{
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            List<DbGroupUserMappingVo> theDbVoList = theDao.queryBuilder().where(DbGroupUserMappingVoDao.Properties.GroupId.eq(groupId)).list();
            if( theDbVoList == null){
                return false;
            }
            theDao.deleteInTx(theDbVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public boolean updateDbGroupList(List<GroupVo> groupVoList){
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        UserVo loginVo = LoginService.loadLastLoginUser(mContext);
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao theDao = daoSession.getDbGroupVoDao();
            List<DbGroupVo> theDbVoList = theDao.queryBuilder().where(DbGroupVoDao.Properties.PlaceId.eq(placeVo.getPlaceId())).list();
            if( !deleteDbGroupList_paramDbVo(daoSession, theDbVoList)){
                return false;
            }

            theDbVoList = new ArrayList<>();
            for( GroupVo groupVo : groupVoList){
                DbGroupVo dbGroupVo = new DbGroupVo(placeVo.getPlaceId(), Long.parseLong(groupVo.getGroupId()), groupVo.getGroupName(), loginVo.getUserId(), groupVo.getGroupIconImg());
                if( !updateDbGroupPlugMapping(daoSession, groupVo, groupVo.getPlugVoList(), placeVo)){
                    return false;
                }
                if( !updateDbGroupUserMapping(daoSession, groupVo, groupVo.getUserVoList(), placeVo)){
                    return false;
                }
                theDbVoList.add(dbGroupVo);
            }

              theDao.insertOrReplaceInTx(theDbVoList);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateDbGroup(GroupVo groupVo){
        PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao theDao = daoSession.getDbGroupVoDao();
            DbGroupVo dbGroupVo = theDao.queryBuilder().where(DbGroupVoDao.Properties.PlaceId.eq(placeVo.getPlaceId()))
                                                       .where(DbGroupVoDao.Properties.GroupId.eq(groupVo.getGroupId()))
                                                       .unique();

            dbGroupVo.setGroupName(groupVo.getGroupName());
            dbGroupVo.setGroupImg(groupVo.getGroupIconImg());
            if( !updateDbGroupPlugMapping(daoSession, groupVo, groupVo.getPlugVoList(), placeVo)){
                return false;
            }
            if( !updateDbGroupUserMapping(daoSession, groupVo, groupVo.getUserVoList(), placeVo)){
                return false;
            }

            theDao.insertOrReplaceInTx(dbGroupVo);
            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    public boolean updateDbGroupUserMapping(DaoSession daoSession, GroupVo groupVo, List<UserVo> userVoList, PlaceVo placeVo){
        try{
            DbGroupUserMappingVoDao theDao = daoSession.getDbGroupUserMappingVoDao();
            List<DbGroupUserMappingVo> dbGroupUserMappingVoList = theDao.queryBuilder().where(DbGroupUserMappingVoDao.Properties.GroupId.eq(groupVo.getGroupId())).list();

            if( userVoList == null || userVoList.size() < 1){
                return false;
            }

            for( UserVo userVo : userVoList){
                DbGroupUserMappingVo groupUserMappingVo = new DbGroupUserMappingVo();
                groupUserMappingVo.setGroupId(Long.parseLong(groupVo.getGroupId()));
                groupUserMappingVo.setUserId(userVo.getUserId());
                groupUserMappingVo.setAuth(String.valueOf(userVo.getAuth()));
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
    public boolean updateDbGroupPlugMapping(DaoSession daoSession, GroupVo groupVo, List<PlugVo> plugVoList, PlaceVo placeVo){
        try{
            DbGroupPlugMappingVoDao theDao = daoSession.getDbGroupPlugMappingVoDao();
            List<DbGroupPlugMappingVo> dbGroupPlugMappingVoList = new ArrayList<>();

            if( plugVoList == null || plugVoList.size() < 1){
                return true; // Plug는 없어도 됨
            }

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

    private boolean deleteDbGroupList_paramDbVo(DaoSession daoSession, List<DbGroupVo> groupVoList){
        try{
            DbGroupVoDao theDao = daoSession.getDbGroupVoDao();

            for( DbGroupVo dbGroupVo : groupVoList) {
                if (!deleteDbGroupPlugMappingList(daoSession, dbGroupVo.getGroupId())) {
                    return false;
                }
                if (!deleteDbGroupUserMappingList(daoSession, dbGroupVo.getGroupId())) {
                    return false;
                }
            }
            theDao.deleteInTx(groupVoList);

            return true;
        } catch(SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deleteAllDbGroupAll(){
        try{
            DaoSession daoSession = mDBHelper.getSession(true);
            DbGroupVoDao theDbGroupVoDao = daoSession.getDbGroupVoDao();
            DbGroupPlugMappingVoDao theDbGroupPlugMappingVoDao = daoSession.getDbGroupPlugMappingVoDao();
            DbGroupUserMappingVoDao theDbGroupUserMappingVoDao = daoSession.getDbGroupUserMappingVoDao();

            theDbGroupVoDao.deleteAll();
            theDbGroupPlugMappingVoDao.deleteAll();
            theDbGroupUserMappingVoDao.deleteAll();

            return true;
        } catch (SQLiteConstraintException se){
            se.printStackTrace();
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /*
     * Request Bluetooth Method
     */
    private List<PlugVo> mBLPlugVoList;
    public void requestBLGroupList(List<PlugVo> blPlugVoList){
        mBLPlugVoList = blPlugVoList;
        MainActivity.stBluetoothManager.setOnAssociatedDevicesResultCallbacks(this);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String requestMessage = BLMessage.getDeviceIdRequestMessage(MainActivity.stBluetoothManager);
                if( MainActivity.stBluetoothManager.isConnected()) {
                    MainActivity.stBluetoothManager.sendData(SPUtil.getByte(requestMessage), false);
                }
            }
        });


    }

    @Override
    public void onGetAssociatedDevice(String plugId, List<String> groupIdList) {

        boolean isCreateId = false;
        for( int voCnt=0; voCnt<mBLPlugVoList.size(); voCnt++){
            PlugVo blPlugVo = mBLPlugVoList.get(voCnt);
            if( blPlugVo.getPlugId().equalsIgnoreCase(plugId)){
                isCreateId = true;
            }
        }
        if( !isCreateId){
            return;
        }

        for( int idCnt=0; idCnt<groupIdList.size(); idCnt++) {
            String groupId = groupIdList.get(idCnt);
            String groupIconImg = SPConfig.FILE_PATH + SPConfig.GROUP_DEFAULT_IMAGE_NAME + "_00";
            GroupVo groupVo = selectDbGroup(Long.parseLong(groupId));
            groupVo.setGroupName(groupVo.getGroupName() + ", " + plugId);
            if( groupVo == null) {
                groupVo = new GroupVo(groupId, groupIconImg, groupId + " : " + plugId, "0", false);
            }
            updateDbGroup(groupVo);
        }
    }
}
