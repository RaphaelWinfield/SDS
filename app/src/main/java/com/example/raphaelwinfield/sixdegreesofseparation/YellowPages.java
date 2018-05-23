package com.example.raphaelwinfield.sixdegreesofseparation;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class YellowPages extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yellow_pages);
        WebView yellowPages = (WebView) findViewById(R.id.wv_yellow_pages);
        yellowPages.getSettings().setJavaScriptEnabled(true);
        yellowPages.loadUrl("http://m.114best.com");
        yellowPages.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if(url == null) return false;
                try {
                    if(url.startsWith("tel:")){   //处理其他scheme，如微信、支付宝
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) {
                    //防止crash，即如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash
                    //返回true表示拦截自定义链接不跳转，避免弹出上面的错误页面
                    return true;
                }
                wv.loadUrl(url);//正常处理http和https开头的url
                return true;
            }
        });

    }
}
