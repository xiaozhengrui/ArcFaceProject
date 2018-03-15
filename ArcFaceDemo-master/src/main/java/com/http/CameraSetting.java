package com.http;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.arcsoft.sdk_demo_1.R;

/**
 * Created by techwinxiao on 18-3-8.
 */

public class CameraSetting extends Activity {
    private WebView webView;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_setting);
        //获取控件
        webView = (WebView) findViewById(R.id.webView);
        //装载URL
        /*webView.loadUrl("http://192.168.1.88");
        // 设置WebViewClient来接收处理请求和通知
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                // 返回true则表明使用的是WebView
                return true;
            }
        });*/
        webView.loadUrl("file:///camera_setting.html");
        // 获取焦点
        webView.requestFocus();
    }
}
