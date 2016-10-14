package cl.niclabs.speedtest;

import android.content.Context;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class VideoTest {
    private MainTest mainTest;
    private WebView webView;
    public VideoTest(MainTest mainTest, WebView webView) {
        this.mainTest = mainTest;
        this.webView = webView;
    }

    public void start() {
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new SpeedTestJavascriptInterface(this), "JSInterface");
        webView.loadUrl("file:///android_asset/frameVideo.js");
    }

    public void onVideoEnded(String quality, int timesBuffering) {
        mainTest.onVideoEnded(getQuality(quality), timesBuffering);
    }

    public String getQuality(String quality){
        String text;
        switch (quality){
            case "tiny":
                text = "144p";
                break;
            case "small":
                text = "240p";
                break;
            case "medium":
                text = "360p";
                break;
            case "large":
                text = "480p";
                break;
            case "hd720":
                text = "720p";
                break;
            default:
                text = "unknown";
                break;
        }
        return text;
    }


    public Context getContext() {
        return webView.getContext();
    }

    public void finish() {
        mainTest.onVideoTestFinish();
    }
}
