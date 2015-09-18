package com.sicoms.smartplug.main.service;

import android.app.Activity;

import com.sicoms.smartplug.dao.DBHelper;
import com.sicoms.smartplug.dao.DaoSession;
import com.sicoms.smartplug.dao.DbLastDataVo;
import com.sicoms.smartplug.dao.DbLastDataVoDao;
import com.sicoms.smartplug.domain.NodeListDataVo;
import com.sicoms.smartplug.domain.PointListDataVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by pc-11-user on 2015-03-16.
 */
public class MainService {
    private Activity mActivity;

    public MainService(){}
    public MainService(Activity activity){
        mActivity = activity;
    }

    /*
     * Node List 데이터 DB 업데이트
     */
    public boolean updateNodeListDB(List<NodeListDataVo> dataList){
        return true;
    }
    /*
     * Point List 데이터 DB 업데이트
     */
    public boolean updatePointListDB(List<PointListDataVo> dataList){
        if (dataList != null && dataList.size() > 0) {
            DBHelper dbHelper = new DBHelper(mActivity);
            DaoSession daoSession = dbHelper.getSession(false);
            final DbLastDataVoDao theDao = daoSession.getDbLastDataVoDao();
            List<DbLastDataVo> dbLastDataVoList = new ArrayList<DbLastDataVo>();
            Date now = new Date();
            for (PointListDataVo oneVo:dataList) {
                DbLastDataVo lastDataVo = null;
                String on_off = oneVo.getS();
                String usage = oneVo.getW();
                //dbLastDataVoList.add(new DbLastDataVo(now,oneVo.getND_CODE(), Float.parseFloat(usage), on_off));
            }
            theDao.insertOrReplaceInTx(dbLastDataVoList);
            dbHelper.closeSession();
        }
        return true;
    }
    /*
     * Point List 관련 HTTP 요청에 대한 응답
     */
//    @Override
//    public void onHttpBitmapResponseResultStatus(int type, int result, String value) {
//        if (result != HttpConfig.HTTP_SUCCESS)
//            return;
//
//        if (type == HttpConfig.TYPE_POST) {
//            List<NodeListDataVo> dataList = new ArrayList<>();
//            NodeListResponseVo data = new Gson().fromJson(value, NodeListResponseVo.class);
//            dataList = data.getDp();
//            updateNodeListDB(dataList);
//        } else if (type == HttpConfig.TYPE_POST_SCHEDULE) {
//            List<PointListDataVo> dataList = new ArrayList<>();
//            PointListResponseVo data = new Gson().fromJson(value, PointListResponseVo.class);
//            dataList = data.getDp();
//            updatePointListDB(dataList);
//        }
//    }
}
