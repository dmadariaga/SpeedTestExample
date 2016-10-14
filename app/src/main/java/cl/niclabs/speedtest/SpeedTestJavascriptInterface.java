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
    public void onVideoEnded(final String quality, final int timesBuffering){
        videoTest.onVideoEnded(quality, timesBuffering);
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
    public void startBytes(){
        videoTest.startBytes();
    }

}
