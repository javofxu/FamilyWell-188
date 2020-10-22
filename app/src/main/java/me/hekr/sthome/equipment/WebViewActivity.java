package me.hekr.sthome.equipment;

import android.annotation.SuppressLint;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.hekr.sthome.R;
import me.hekr.sthome.common.TopbarSuperActivity;

/**
 * @author skygge
 * @date 2020/10/22.
 * GitHub：javofxu@github.com
 * email：skygge@yeah.net
 * description：
 */
public class WebViewActivity extends TopbarSuperActivity {

    private int mUrlType;
    private String mTitleName;
    private String mInstructionUrl;
    private WebView mWebView;

    @Override
    protected void onCreateInit() {
        mUrlType = getIntent().getIntExtra("instructions_type", 0);
        if (mUrlType == 1){
            mTitleName = getString(R.string.user_agreement_hint);
        }else if (mUrlType == 2){
            mTitleName = getString(R.string.privacy_policy_hint);
        }else {
            mTitleName = getIntent().getStringExtra("instructions_name");
        }
        mInstructionUrl = getIntent().getStringExtra("instructions_urls");
        getTopBarView().setTopBarStatus(1, 1, mTitleName, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        },null);
        mWebView = findViewById(R.id.web_view);
        initWebView();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_web_view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        mWebView.setInitialScale(25);
        if (mUrlType == 1 || mUrlType == 2){
            mWebView.setWebViewClient(new WebViewClient());
            mWebView.loadUrl(mInstructionUrl);
        }else {
            webSettings.setSupportZoom(true);
            webSettings.setAllowFileAccess(true);
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
            mWebView.loadUrl("file:///android_asset/index.html?" + mInstructionUrl);
        }
    }
}
