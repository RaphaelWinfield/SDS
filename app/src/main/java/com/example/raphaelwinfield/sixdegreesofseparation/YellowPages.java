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
        yellowPages.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if(url == null) return false;
                try {
                    if(url.startsWith("tel:")){   //其他自定义的scheme，如微信、支付宝
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                } catch (Exception e) { //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                    return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
                }
                wv.loadUrl(url);//处理http和https开头的url
                return true;
            }
        });
        yellowPages.loadUrl("http://m.114best.com");
    }
}
