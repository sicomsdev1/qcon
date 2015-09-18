package com.sicoms.smartplug.network.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;

/**
 * Created by pc-11-user on 2015-03-13.
 */
public class AsyncHttpPost extends AsyncHttpManager {
    private static final String LOG_TAG = "AsyncHttpPost";

    private CookieStore mCookieStore;

    private Context mContext;
    private HttpResponseCallbacks mCallbacks;
    private RequestParams mParams;
    private String mContentType;

    public AsyncHttpPost(Context context, String ip, String port, String contextPath, String header, RequestParams params){
        mContext = context;
        mParams = params;
        setHttpEntity(ip, port, contextPath, header, params);

        mCookieStore = new PersistentCookieStore(mContext.getApplicationContext());
        getAsyncHttpClient().setCookieStore(mCookieStore);
    }

    public void setContentType(String contentType){
        mContentType = contentType;
    }

    @Override
    public RequestHandle executeHttpClient(AsyncHttpClient client, String URL, Header[] headers, HttpEntity entity, ResponseHandlerInterface responseHandler) {
        return client.post(mContext, URL, headers, mParams, mContentType, responseHandler);
    }

    @Override
    public boolean isRequestBodyAllowed() {
        return false;
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
                    mCallbacks.onHttpResponseResultStatus(HttpConfig.TYPE_POST, HttpConfig.HTTP_SUCCESS, new String(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                debugHeaders(LOG_TAG, headers);
                debugStatusCode(LOG_TAG, statusCode);
                debugThrowable(LOG_TAG, e);
                if (errorResponse != null) {
                    debugResponse(LOG_TAG, new String(errorResponse));
                }
                if( mCallbacks != null){
                    String errorMessage = "";
                    if( errorResponse != null)
                        errorMessage = new String(errorResponse);
                    mCallbacks.onHttpResponseResultStatus(HttpConfig.TYPE_POST, HttpConfig.HTTP_FAIL, errorMessage);
                }
            }
        };
    }

    public void setOnHttpResponseCallbacks(final HttpResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
