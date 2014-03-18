package com.example.helloandroidhttp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

public class HelloHttpActivity extends Activity {

    private Button mJdkGetBtn = null;
    private Button mJdkPostBtn = null;
    private Button mApacheGetBtn = null;
    private Button mApachePostBtn = null;
    private Button mCleanButton = null;
    private TextView mResulTextView = null;
    private WebView mWebView = null;

    private static final String TEST_URL = "http://www.cnblogs.com/mengdd/";

    // private static final String TEST_URL =
    // "http://10.129.156.136:8080/HelloServer/HelloServer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello_http);

        mJdkGetBtn = (Button) findViewById(R.id.get_jdk);
        mJdkPostBtn = (Button) findViewById(R.id.post_jdk);
        mApacheGetBtn = (Button) findViewById(R.id.get_apache);
        mApachePostBtn = (Button) findViewById(R.id.post_apache);
        mResulTextView = (TextView) findViewById(R.id.result_text);
        mCleanButton = (Button) findViewById(R.id.clean);

        mWebView = (WebView) findViewById(R.id.webView);

        mJdkGetBtn.setOnClickListener(mOnClickListener);
        mJdkPostBtn.setOnClickListener(mOnClickListener);
        mApacheGetBtn.setOnClickListener(mOnClickListener);
        mApachePostBtn.setOnClickListener(mOnClickListener);
        mCleanButton.setOnClickListener(mOnClickListener);

    }

    private OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {

            if (v.getId() == R.id.clean) {
                mResulTextView.setText(null);

                mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8",
                        null);
            }
            else {
                new NetworkTestTask().execute(v);
            }

        }
    };

    private class NetworkTestTask extends AsyncTask<View, Void, String> {

        @Override
        protected String doInBackground(View... params) {

            String result = null;
            switch (params[0].getId()) {
            case R.id.get_apache:

                result = HttpUtilsApache.performGetRequest(TEST_URL);

                break;
            case R.id.post_apache:
                result = HttpUtilsApache.performPostRequest(TEST_URL, null);
                break;
            case R.id.get_jdk:
                result = HttpUtilsJDK.performGetRequest(TEST_URL);
                break;
            case R.id.post_jdk:
                result = HttpUtilsJDK.performPostRequest(TEST_URL, null);
                break;
            default:
                break;
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // UI线程
            mResulTextView.setText(result);
            mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
            mWebView.loadDataWithBaseURL(null, result, "text/html", "utf-8",
                    null);
        }

    }
}
