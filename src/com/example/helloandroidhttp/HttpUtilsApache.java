package com.example.helloandroidhttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class HttpUtilsApache {

    private static final String LOG_TAG = "Http->Apache";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    /**
     * Default encoding for POST or PUT parameters. See
     * {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    /**
     * Returns which encoding should be used when converting POST or PUT
     * parameters returned by {@link #getParams()} into a raw POST or PUT body.
     *
     * <p>
     * This controls both encodings:
     * <ol>
     * <li>The string encoding used when converting parameter names and values
     * into bytes prior to URL encoding them.</li>
     * <li>The string encoding used when converting the URL encoded parameters
     * into a raw byte array.</li>
     * </ol>
     */
    public static String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public static String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset="
                + getParamsEncoding();
    }

    public static String performGetRequest(String url) {

        String result = null;
        // 生成一个请求对象
        HttpGet httpGet = new HttpGet(url);

        // 1.生成一个Http客户端对象(带参数的)
        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 10 * 1000);// 设置请求超时10秒
        HttpConnectionParams.setSoTimeout(httpParameters, 10 * 1000); // 设置等待数据超时10秒
        HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);

        HttpClient httpClient = new DefaultHttpClient(httpParameters); // 此时构造DefaultHttpClient时将参数传入
        // 2.默认实现：
        // HttpClient httpClient = new DefaultHttpClient();
        httpGet.addHeader(HEADER_CONTENT_TYPE, getBodyContentType());

        // 下面使用Http客户端发送请求，并获取响应内容

        HttpResponse httpResponse = null;

        try {
            // 发送请求并获得响应对象
            httpResponse = httpClient.execute(httpGet);

            final int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (200 == statusCode) {
                result = getResponseString(httpResponse);
            }
            else {
                Log.e(LOG_TAG, "Connection failed: " + statusCode);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {

        }

        return result;
    }

    public static String performPostRequest(String baseURL, String postData) {
        String result = "";
        HttpResponse response = null;
        try {

            // URL使用基本URL即可，其中不需要加参数
            HttpPost httpPost = new HttpPost(baseURL);
            // 设置ContentType
            httpPost.addHeader(HEADER_CONTENT_TYPE, getBodyContentType());

            // 将请求体内容加入请求中
            HttpEntity requestHttpEntity = prepareHttpEntity(postData);

            if (null != requestHttpEntity) {
                httpPost.setEntity(requestHttpEntity);
            }

            // 需要客户端对象来发送请求
            HttpClient httpClient = new DefaultHttpClient();
            // 发送请求
            response = httpClient.execute(httpPost);

            final int statusCode = response.getStatusLine().getStatusCode();
            if (200 == statusCode) {
                // 显示响应
                result = getResponseString(response);
            }
            else {
                Log.e(LOG_TAG, "Connection failed: " + statusCode);
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {

        }

        return result;

    }

    /**
     * 直接利用String生成HttpEntity，String应该已经是key=value&key2=value2的形式
     *
     * @param postData
     * @return
     */
    private static HttpEntity prepareHttpEntity(String postData) {

        HttpEntity requestHttpEntity = null;

        try {

            if (null != postData) {
                // 去掉所有的换行
                postData = postData.replace("\n", "");
                // one way
                // requestHttpEntity = new ByteArrayEntity(
                // postData.getBytes(getParamsEncoding()));

                // another way
                requestHttpEntity = new StringEntity(postData,
                        getParamsEncoding());
                ((StringEntity) requestHttpEntity)
                        .setContentEncoding(getParamsEncoding());
                ((StringEntity) requestHttpEntity)
                        .setContentType(getBodyContentType());

            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return requestHttpEntity;
    }

    /**
     * 利用Map结构的参数生成HttpEntity，使用UrlEncodedFormEntity对参数对进行编码
     *
     * @param params
     * @return
     */
    private static HttpEntity prepareHttpEntity1(Map<String, String> params) {
        // 需要将String里面的key value拆分出来

        HttpEntity requestHttpEntity = null;
        try {

            if (null != params) {
                List<NameValuePair> pairList = new ArrayList<NameValuePair>(
                        params.size());
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    NameValuePair pair = new BasicNameValuePair(entry.getKey(),
                            entry.getValue());
                    pairList.add(pair);
                }
                requestHttpEntity = new UrlEncodedFormEntity(pairList,
                        getParamsEncoding());

            }

        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return requestHttpEntity;
    }

    /**
     * 利用Map结构的参数生成HttpEntity，使用自己的方法对参数进行编码合成字符串
     *
     * @param params
     * @return
     */
    private static HttpEntity prepareHttpEntity2(Map<String, String> params) {
        // 需要将String里面的key value拆分出来

        HttpEntity requestHttpEntity = null;
        byte[] body = encodeParameters(params, getParamsEncoding());
        requestHttpEntity = new ByteArrayEntity(body);

        return requestHttpEntity;
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded
     * encoded string.
     */
    private static byte[] encodeParameters(Map<String, String> params,
            String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(),
                        paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(),
                        paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        }
        catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: "
                    + paramsEncoding, uee);
        }
    }

    public static String getResponseString(HttpResponse response) {
        String result = null;
        if (null == response) {
            return result;
        }

        HttpEntity httpEntity = response.getEntity();
        InputStream inputStream = null;
        try {
            inputStream = httpEntity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    inputStream));
            result = "";
            String line = "";
            while (null != (line = reader.readLine())) {
                result += line;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;

    }
}
