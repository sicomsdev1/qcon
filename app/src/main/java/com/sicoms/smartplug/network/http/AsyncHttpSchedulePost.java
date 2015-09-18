package com.sicoms.smartplug.network.http;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by pc-11-user on 2015-03-13.
 */
public class AsyncHttpSchedulePost extends AsyncHttpManager {

    private static final String LOG_TAG = "AsyncHttpPost";

    private Context mContext;
    private HttpResponseCallbacks mCallbacks;

    int mDelay;
    int mPeriod;

    Timer mTimer;
    TimerTask mTask;

    public AsyncHttpSchedulePost(Context context, String ip, String header, String body, int delay, int period){
        mContext = context;
        setHttpEntity(ip, HttpConfig.HTTP_PORT, header, body);
        mDelay = delay;
        mPeriod = period;
    }
    public AsyncHttpSchedulePost(Context context, String ip, String port, String header, String body, int delay, int period){
        mContext = context;
        setHttpEntity(ip, port, header, body);
        mDelay = delay;
        mPeriod = period;
    }

    @Override
    public RequestHandle executeHttpClient(final AsyncHttpClient client, final String URL, final Header[] headers, final HttpEntity entity, final ResponseHandlerInterface responseHandler) {

        mTask = new TimerTask(){

            @Override
            public void run() {
                client.post(mContext, URL, headers, entity, null, responseHandler);
            }
        };
        mTimer = new Timer();
        mTimer.schedule(mTask, mDelay, mPeriod); // 1초 후에 실행, 60초 간격 반복

        return null;
    }

    public void stopSchedule(){
        if( mTimer != null)
            mTimer.cancel();
    }

    @Override
    public boolean isRequestBodyAllowed() {
        return true;
    }

    @Override
    public boolean isRequestHeadersAllowed() {
        return true;
    }

    @Override
    public String getDefaultURL() {
        return PROTOCOL + HttpConfig.HTTP_CONTEXT_PATH;
    }

    @Override
    public Header[] getRequestHeaders() {
        return new Header[0];
    }

    @Override
    public ResponseHandlerInterface getResponseHandler() {
        return new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                debugHeaders(LOG_TAG, headers);
                debugStatusCode(LOG_TAG, statusCode);
                debugResponse(LOG_TAG, new String(response));
                if( mCallbacks != null)
                    mCallbacks.onHttpResponseResultStatus(HttpConfig.TYPE_POST_SCHEDULE, HttpConfig.HTTP_SUCCESS, new String(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                debugHeaders(LOG_TAG, headers);
                debugStatusCode(LOG_TAG, statusCode);
                debugThrowable(LOG_TAG, e);
                if (errorResponse != null) {
                    debugResponse(LOG_TAG, new String(errorResponse));
                }
                if( mCallbacks != null) {
                    String errorMessage = "";
                    if( errorResponse != null)
                        errorMessage = new String(errorResponse);
                    mCallbacks.onHttpResponseResultStatus(HttpConfig.TYPE_POST_SCHEDULE, HttpConfig.HTTP_FAIL, errorMessage);
                }
            }
        };
    }

    public void setOnHttpResponseCallbacks(final HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
