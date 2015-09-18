package com.sicoms.smartplug.menu.service;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.domain.UserVo;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

/**
 * Created by gudnam on 2015. 8. 25..
 */
public class MypageService {
    private static final String TAG = MypageService.class.getSimpleName();

    private Context mContext;
    private HttpResponseCallbacks mCallbacks;

    public MypageService(Context context){
        mContext = context;
    }
    public void setOnHttpResponseCallbacks(HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }

    /*
     * Cloud Server Method
     */
    // 비밀번호 변경
    public void requestUpdatePassword(UserVo userVo){
        try {
            CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_MEMBERSHIP_PASSWORD;
            String jsonBody = new Gson().toJson(userVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_MEMBERSHIP_PASSWORD, "", params);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void requestUpdateMembershipProfile(UserVo userVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_MEMBERSHIP_PROFILE;
        try {
            String jsonBody = new Gson().toJson(userVo);
            RequestParams params = new RequestParams();
            params.add("jsonStr", jsonBody);
            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_MEMBERSHIP_PROFILE, "", params);
            post.setContentType(HttpConfig.CONTENT_TYPE_URLENCODED);
            if (mCallbacks != null) {
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
