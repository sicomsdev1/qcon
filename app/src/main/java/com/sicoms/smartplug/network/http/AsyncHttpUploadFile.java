package com.sicoms.smartplug.network.http;

import android.content.Context;

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
public class AsyncHttpUploadFile extends AsyncHttpManager {
    private static final String TAG = AsyncHttpUploadFile.class.getSimpleName();

    private CookieStore mCookieStore;

    private Context mContext;
    private HttpResponseCallbacks mCallbacks;
    private RequestParams mParams;

    public AsyncHttpUploadFile(Context context, String ip, String port, String contextPath, String header, RequestParams params){
        mContext = context;
        mParams = params;
        setHttpEntity(ip, port, contextPath, header, params);

        mCookieStore = new PersistentCookieStore(mContext.getApplicationContext());
        getAsyncHttpClient().setCookieStore(mCookieStore);
    }

    @Override
    public RequestHandle executeHttpClient(AsyncHttpClient client, String URL, Header[] headers, HttpEntity entity, ResponseHandlerInterface responseHandler) {
        return client.post(mContext, URL, mParams, responseHandler);
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
                debugHeaders(TAG, headers);
                debugStatusCode(TAG, statusCode);
                debugResponse(TAG, new String(response));
                if( mCallbacks != null)
                    mCallbacks.onHttpResponseResultStatus(HttpConfig.TYPE_POST, HttpConfig.HTTP_SUCCESS, new String(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                debugHeaders(TAG, headers);
                debugStatusCode(TAG, statusCode);
                debugThrowable(TAG, e);
                if (errorResponse != null) {
                    debugResponse(TAG, new String(errorResponse));
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
