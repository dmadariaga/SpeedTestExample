package cl.niclabs.speedtest;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class SpeedTestJavascriptInterface {
    private VideoTest videoTest;
    public SpeedTestJavascriptInterface(VideoTest videoTest) {
        this.videoTest = videoTest;
    }

    @JavascriptInterface
    public void doEchoTest(String echo){
        Log.d("VideoView", echo);
    }

    @JavascriptInterface
    public void playVideo(){
        //emulateClick(webView);
    }

    @JavascriptInterface
    public void onVideoEnded(String quality, int timesBuffering, float loadedFraction){
        videoTest.onVideoEnded(quality, timesBuffering, loadedFraction);
    }
    @JavascriptInterface
    public void onVideoTestFinish(){
        videoTest.finish();
    }
    @JavascriptInterface
    public void makeToast(String quality){
        String text = videoTest.getQuality(quality);
        Toast.makeText(videoTest.getContext(), text,
                Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void startCountingBytes(){
        videoTest.startCountingBytes();
    }

}
