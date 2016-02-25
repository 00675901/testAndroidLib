package com.fangstar.multipart;
//package com.fangstar.broker.network.multipart;

import android.os.Environment;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.HttpEntity;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 表单格式请求
 * Created by G.
 */
public class MultipartRequest extends Request<String> {
    private Response.ErrorListener errorListener = null;
    private Response.Listener mListener = null;
    private MultipartRequestParams params = null;
    private HttpEntity httpEntity = null;


    public MultipartRequest(int method, MultipartRequestParams params, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.params = params;
        this.mListener = listener;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (params != null) {
            httpEntity = params.getEntity();
            try {
                httpEntity.writeTo(baos);

                Log.e("MultipartRequest", String.format("%d", baos.size()));
                String tempPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FormLog.txt";
                OutputStream outputStream = new FileOutputStream(tempPath);
                baos.writeTo(outputStream);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return baos.toByteArray();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();
        if (null == headers || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }
        return headers;
    }

    public String getBodyContentType() {
        String str = httpEntity.getContentType().getValue();
        return str;
    }


    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }


    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
