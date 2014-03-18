package com.example.helloandroidhttp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import android.util.Log;

public class HttpUtilsJDK {
    private static final String LOG_TAG = "Http->JDK";
    private static final int CONNECT_TIME_OUT = 3000;
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    /**
     * Default encoding for POST or PUT parameters. See
     * {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    public static String getParamsEncoding() {
        return DEFAULT_PARAMS_ENCODING;
    }

    public static String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset="
                + getParamsEncoding();
    }

    public static String performGetRequest(String baseUrl) {
        String result = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl);
            if (null != url) {

                // 获取HttpURLConnection类型的对象
                connection = (HttpURLConnection) url.openConnection();
                // 设置连接的最大等待时间
                connection.setConnectTimeout(CONNECT_TIME_OUT);

                // Sets the maximum time to wait for an input stream read to
                // complete before giving up.
                connection.setReadTimeout(3000);
                // 设置为GET方法
                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                if (200 == connection.getResponseCode()) {
                    InputStream inputStream = connection.getInputStream();

                    result = getResultString(inputStream, getParamsEncoding());
                }
                else {
                    Log.e(LOG_TAG,
                            "Connection failed: "
                                    + connection.getResponseCode());
                }

            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            connection.disconnect();
        }

        return result;
    }

    public static String performPostRequest(String baseUrl,
            Map<String, String> params) {
        String result = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(baseUrl);
            if (null != url) {
                // 获取HttpURLConnection类型的对象
                connection = (HttpURLConnection) url.openConnection();
                // 设置响应超时限制
                connection.setConnectTimeout(CONNECT_TIME_OUT);
                // 设置为POST方法
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                // 有请求体则setDoOutput(true)必须设定
                connection.setDoOutput(true);

                // 为了性能考虑，如果包含请求体，那么最好调用 setFixedLengthStreamingMode(int)或者
                // setChunkedStreamingMode(int)
                // connection.setChunkedStreamingMode(0);// 参数为0时使用默认值

                byte[] data = getParamsData(params);

                connection.setRequestProperty(HEADER_CONTENT_TYPE,
                        getBodyContentType());
                if (null != data) {
                    connection.setFixedLengthStreamingMode(data.length);
                    connection.setRequestProperty(HEADER_CONTENT_LENGTH,
                            String.valueOf(data.length));
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(data);
                }

                // 得到返回值
                int responseCode = connection.getResponseCode();
                if (200 == responseCode) {
                    result = getResultString(connection.getInputStream(),
                            getParamsEncoding());

                }
                else {
                    Log.e(LOG_TAG,
                            "Connection failed: "
                                    + connection.getResponseCode());
                }

            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            connection.disconnect();
        }

        return result;
    }

    private static byte[] getParamsData(Map<String, String> params) {
        byte[] data = null;

        try {
            if (null != params && !params.isEmpty()) {
                StringBuffer buffer = new StringBuffer();

                for (Map.Entry<String, String> entry : params.entrySet()) {

                    buffer.append(entry.getKey())
                            .append("=")
                            .append(URLEncoder.encode(entry.getValue(),
                                    getParamsEncoding())).append("&");// 请求的参数之间使用&分割。

                }
                // 最后一个&要去掉
                buffer.deleteCharAt(buffer.length() - 1);

                data = buffer.toString().getBytes(getParamsEncoding());
            }
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        }

        return data;
    }

    private static String getResultString(InputStream inputStream, String encode) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        String result = "";
        if (inputStream != null) {
            try {
                while ((len = inputStream.read(data)) != -1) {
                    outputStream.write(data, 0, len);
                }
                result = new String(outputStream.toByteArray(), encode);

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
