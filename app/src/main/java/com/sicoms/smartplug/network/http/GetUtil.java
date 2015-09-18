package com.sicoms.smartplug.network.http;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * Created by pc-11-user on 2015-03-13.
 */
public class GetUtil extends AsyncHttpManager {
    private static final String LOG_TAG = "GetUtil";
    private Context mContext;
    private HttpResponseCallbacks mCallbacks;

    public GetUtil(Context context, String url, String header, String body){
        mContext = context;
        setHttpEntity(url, header, body);
    }
    public GetUtil(Context context, String url, String port, String header, String body){
        mContext = context;
        setHttpEntity(url, port, header, body);
    }


    @Override
    public RequestHandle executeHttpClient(AsyncHttpClient client, String URL, Header[] headers, HttpEntity entity, ResponseHandlerInterface responseHandler) {
        return client.get(mContext, URL, headers, null, responseHandler);
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
                    mCallbacks.onHttpResponseResultStatus(HttpConfig.TYPE_GET, HttpConfig.HTTP_SUCCESS, new String(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                debugHeaders(LOG_TAG, headers);
                debugStatusCode(LOG_TAG, statusCode);
                debugThrowable(LOG_TAG, e);
                if (errorResponse != null) {
                    debugResponse(LOG_TAG, new String(errorResponse));
                }
                if( mCallbacks != null)
                    mCallbacks.onHttpResponseResultStatus(HttpConfig.TYPE_GET, HttpConfig.HTTP_FAIL, new String(errorResponse));
            }
        };
    }

    public void setOnHttpResponseCallbacks(final HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
