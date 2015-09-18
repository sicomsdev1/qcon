package com.sicoms.smartplug.plug.service;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.RequestParams;
import com.sicoms.smartplug.domain.PlaceVo;
import com.sicoms.smartplug.domain.PlugVo;
import com.sicoms.smartplug.menu.service.PlaceService;
import com.sicoms.smartplug.network.http.AsyncHttpPost;
import com.sicoms.smartplug.network.http.CloudManager;
import com.sicoms.smartplug.network.http.ContextPathStore;
import com.sicoms.smartplug.network.http.HttpConfig;
import com.sicoms.smartplug.network.http.HttpResponseCallbacks;

/**
 * Created by gudnam on 2015. 6. 17..
 */
public class PlugHttpService {

    private Context mContext;
    private HttpResponseCallbacks mCallbacks;

    public PlugHttpService(Context context, HttpResponseCallbacks callbacks){
        mContext = context;
        mCallbacks = callbacks;
    }
    /*
     * Cloud Server Method
     */
    public void requestUpdateDevice(PlugVo plugVo){
        CloudManager.CLOUD_REQUEST_NUM = ContextPathStore.REQUEST_UPDATE_PLUG;

        try{
            PlaceVo placeVo = PlaceService.loadLastPlace(mContext);
            String jsonBody = new Gson().toJson(plugVo);

            RequestParams params = new RequestParams();
            params.put("jsonStr", jsonBody);
            params.put("placeId", placeVo.getPlaceId());

            AsyncHttpPost post = new AsyncHttpPost(mContext, HttpConfig.CLOUD_HTTP_IP, HttpConfig.CLOUD_HTTP_PORT, ContextPathStore.CLOUD_UPDATE_PLUG, "", params);
            if(mCallbacks != null){
                post.setOnHttpResponseCallbacks(mCallbacks);
            }
            post.run();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
