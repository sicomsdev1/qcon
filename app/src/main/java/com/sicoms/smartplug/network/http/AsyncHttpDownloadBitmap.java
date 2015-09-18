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
public class AsyncHttpDownloadBitmap extends AsyncHttpManager {
    private static final String LOG_TAG = "AsyncHttpDownloadBitmap";

    private CookieStore mCookieStore;

    private Context mContext;
    private HttpBitmapResponseCallbacks mCallbacks;
    private RequestParams mParams;
    private String mContentType;

    public AsyncHttpDownloadBitmap(Context context, String ip, String port, String contextPath, String header, RequestParams params){
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
                Bitmap bitmap = BitmapFactory.decodeByteArray(response, 0, response.length);
                if( bitmap == null){
                    return;
                }
                try {
                    String fileName = headers[2].getValue().split("\"")[1];
                    if (mCallbacks != null)
                        mCallbacks.onHttpBitmapResponseResultStatus(HttpConfig.TYPE_POST, HttpConfig.HTTP_SUCCESS, fileName, bitmap);
                } catch (ArrayIndexOutOfBoundsException aie){
                    aie.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                debugHeaders(LOG_TAG, headers);
                debugStatusCode(LOG_TAG, statusCode);
                debugThrowable(LOG_TAG, e);
                    mCallbacks.onHttpBitmapResponseResultStatus(HttpConfig.TYPE_POST, HttpConfig.HTTP_FAIL, null, null);
            }
        };
    }

    public void setOnHttpBitmapResponseCallbacks(final HttpBitmapResponseCallbacks callbacks){
        mCallbacks = callbacks;
    }
}
