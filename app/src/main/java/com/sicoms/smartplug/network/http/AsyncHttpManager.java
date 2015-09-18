/*
    Android Asynchronous Http Client Sample
    Copyright (c) 2014 Marek Sebera <marek.sebera@gmail.com>
    http://loopj.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.sicoms.smartplug.network.http;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpRequest;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public abstract class AsyncHttpManager implements AsyncHttpInterface {

    private AsyncHttpClient asyncHttpClient = new AsyncHttpClient() {

        @Override
        protected AsyncHttpRequest newAsyncHttpRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context) {
            AsyncHttpRequest httpRequest = getHttpRequest(client, httpContext, uriRequest, contentType, responseHandler, context);
            return httpRequest == null
                    ? super.newAsyncHttpRequest(client, httpContext, uriRequest, contentType, responseHandler, context)
                    : httpRequest;
        }
    };

    private final List<RequestHandle> requestHandles = new LinkedList<RequestHandle>();
    private static final String LOG_TAG = "SampleParentActivity";

    private static final int MENU_USE_HTTPS = 0;
    private static final int MENU_CLEAR_VIEW = 1;

    private boolean useHttps = true;

    protected static String PROTOCOL = HttpConfig.PROTOCOL;

    private String mUrl;
    private String mHeader;
    private String mBody;           // Body
    private RequestParams mParams;  // Body

    @Override
    public AsyncHttpRequest getHttpRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, ResponseHandlerInterface responseHandler, Context context) {
        return null;
    }

    public List<RequestHandle> getRequestHandles() {
        return requestHandles;
    }

    @Override
    public void addRequestHandle(RequestHandle handle) {
        if (null != handle) {
            requestHandles.add(handle);
        }
    }

    @Override
    public void run() {
        addRequestHandle(executeHttpClient(getAsyncHttpClient(),
                getUrlText(getDefaultURL()),
                getRequestHeaders(),
                getRequestEntity(),
                getResponseHandler()));
    }

    @Override
    public void cancel(Context context) {
        asyncHttpClient.cancelRequests(context, true);
    }

    public List<Header> getRequestHeadersList(String header) {
        List<Header> headers = new ArrayList<Header>();
        String headersRaw = header;

        if (headersRaw != null && headersRaw.length() > 3) {
            String[] lines = headersRaw.split("\\r?\\n");
            for (String line : lines) {
                try {
                    int equalSignPos = line.indexOf('=');
                    if (1 > equalSignPos) {
                        throw new IllegalArgumentException("Wrong header format, may be 'Key=Value' only");
                    }

                    String headerName = line.substring(0, equalSignPos).trim();
                    String headerValue = line.substring(1 + equalSignPos).trim();
                    Log.d(LOG_TAG, String.format("Added header: [%s:%s]", headerName, headerValue));

                    headers.add(new BasicHeader(headerName, headerValue));
                } catch (Throwable t) {
                    Log.e(LOG_TAG, "Not a valid header line: " + line, t);
                }
            }
        }
        return headers;
    }

    public Header[] getRequestHeaders(String header) {
        List<Header> headers = getRequestHeadersList(header);
        return headers.toArray(new Header[headers.size()]);
    }

    public HttpEntity getRequestEntity() {
        String bodyText;
        if (isRequestBodyAllowed() && (bodyText = getBodyText()) != null) {
            try {
                return new StringEntity(bodyText);
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "cannot create String entity", e);
            }
        }
        return null;
    }

    public void setHttpEntity(String ip, String header, String body) {
        mUrl = PROTOCOL + ip + ":" + HttpConfig.HTTP_PORT + HttpConfig.HTTP_CONTEXT_PATH;
        mHeader = header;
        mBody = body;
    }

    public void setHttpEntity(String ip, String port, String header, String body) {
        mUrl = PROTOCOL + ip + ":" + port + HttpConfig.HTTP_CONTEXT_PATH;
        mHeader = header;
        mBody = body;
    }

    public void setHttpEntity(String ip, String port, String contextPath, String header, String body) {
        mUrl = PROTOCOL + ip + ":" + port + contextPath;
        mHeader = header;
        mBody = body;
    }

    public void setHttpEntity(String ip, String port, String contextPath, String header, RequestParams params) {
        mUrl = PROTOCOL + ip + ":" + port + contextPath;
        mHeader = header;
        mParams = params;
    }

    public String getUrlText(String defaultUrl) {
        return mUrl != null || mUrl == "" ? mUrl : defaultUrl;
    }

    public String getBodyText() {
        return mBody;
    }

    protected final void debugHeaders(String TAG, Header[] headers) {
        if (headers != null) {
            Log.d(TAG, "Return Headers:");
            StringBuilder builder = new StringBuilder();
            for (Header h : headers) {
                String _h = String.format(Locale.US, "%s : %s", h.getName(), h.getValue());
                Log.d(TAG, _h);
                builder.append(_h);
                builder.append("\n");
            }
            Log.d(TAG, "Return Headers:" + builder.toString());
        }
    }

    protected static String throwableToString(Throwable t) {
        if (t == null)
            return null;

        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    protected final void debugThrowable(String TAG, Throwable t) {
        if (t != null) {
            Log.e(TAG, "AsyncHttpClient returned error", t);
        }
    }

    protected final void debugResponse(String TAG, String response) {
        if (response != null) {
            Log.d(TAG, "Response data:");
            Log.d(TAG, response);
        }
    }

    protected final void debugStatusCode(String TAG, int statusCode) {
        String msg = String.format(Locale.US, "Return Status Code: %d", statusCode);
        Log.d(TAG, msg);
    }

    @Override
    public String getDefaultHeaders() {
        return null;
    }

    public boolean isCancelButtonAllowed() {
        return false;
    }

    public AsyncHttpClient getAsyncHttpClient() {
        asyncHttpClient.setSSLSocketFactory(MySSLSocketFactory.getFixedSocketFactory());
        asyncHttpClient.setTimeout(HttpConfig.HTTP_TIMEOUT);
        return this.asyncHttpClient;
    }

    @Override
    public void setAsyncHttpClient(AsyncHttpClient client) {
        this.asyncHttpClient = client;
    }
}
