package cl.niclabs.speedtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.TrafficStats;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class WebPagesTest {
    private MainTest mainTest;
    private WebView webView;
    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private long loadingTime[];
    private long sizeBytes[];
    private int i = 0;

    public WebPagesTest(MainTest mainTest, WebView webView) {
        this.mainTest = mainTest;
        this.webView = webView;
    }

    public void start() {
        Log.d("JSON", "API STARTING...");

        AsyncHttpClient client = new AsyncHttpClient();

        client.get("http://blasco.duckdns.org:5000/pingSites/", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray urlsArray = response.getJSONArray("data");
                    for (int i=0; i<urlsArray.length(); i++){
                        JSONObject server = urlsArray.getJSONObject(i);
                        urls.add(server.getString("url"));
                        names.add(server.getString("name"));
                    }
                    loadingTime = new long[urls.size()];
                    sizeBytes = new long[urls.size()];

                } catch (JSONException e) {
                    Log.d("JSON", "API JSONException");
                    e.printStackTrace();
                }
                startLoading();
            };
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //manejo en caso de falla
                Log.d("JSON", "API FAIL...");
                mainTest.onWebPageTestFinish();
            }
            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }
        });
    }

    private void loadNextPage(){
        if (i<urls.size()) {
            new WebPagesTestTask(this).execute(urls.get(i));
        }
        else {
            mainTest.onWebPageTestFinish();
        }
    }

    private void startLoading() {
        Activity testActivity = (Activity) webView.getContext();
        testActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setUpWebView();
                loadNextPage();
            }
        });
    }

    private void setUpWebView() {
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            private long startTime;
            private long finishTime;
            private long previousRxBytes;
            private long previousTxBytes;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                startTime = System.currentTimeMillis();
                previousRxBytes = TrafficStats.getUidRxBytes(Process.myUid());
                Log.d("ASDASD", "INICIAL "+previousRxBytes);
                previousTxBytes = TrafficStats.getUidTxBytes(Process.myUid());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Log.d("ERRORview", request.toString() + " " + error.toString());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                finishTime = System.currentTimeMillis();
                loadingTime[i] = finishTime - startTime;

                long currentRxBytes = TrafficStats.getUidRxBytes(Process.myUid());
                Log.d("ASDASD", "FINAL "+currentRxBytes);

                long currentTxBytes = TrafficStats.getUidTxBytes(Process.myUid());
                sizeBytes[i] = (currentRxBytes - previousRxBytes) + (currentTxBytes - previousTxBytes);

                mainTest.onWebPageLoaded(names.get(i), loadingTime[i], sizeBytes[i]);
                i++;
                loadNextPage();

            }
        });
        /*
        webView.setWebChromeClient(new WebChromeClient(){
            private long previousBytes = TrafficStats.getUidRxBytes(Process.myUid());
            private long previousBytess = TrafficStats.getUidTxBytes(Process.myUid());

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                long currentBytes = TrafficStats.getUidRxBytes(Process.myUid());
                long currentBytess = TrafficStats.getUidTxBytes(Process.myUid());
                long totalBytes = currentBytes - previousBytes;
                long totalBytess = currentBytess - previousBytess;

                Log.d("PROGRESS", "Current Bytes ==>   " + totalBytes + " " + totalBytess
                        + "   New Progress ==>   " + newProgress);
             }
        });*/
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.clearCache(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
    }

    protected void onResponseReceived(final int responseCode) {
        Activity testActivity = (Activity) webView.getContext();
        testActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (responseCode >= 200 && responseCode < 400) {
                    webView.loadUrl(urls.get(i));
                }
                else{
                    loadingTime[i] = -1;
                    mainTest.onWebPageLoaded(names.get(i), loadingTime[i], sizeBytes[i]);
                    i++;
                    loadNextPage();
                }
            }
        });

    }

}


