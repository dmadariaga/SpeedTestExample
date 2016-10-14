package cl.niclabs.speedtest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class WebPagesTest {
    private MainTest mainTest;
    private WebView webView;
    private ArrayList<String> urls = new ArrayList<>();
    private long loadingTime[];
    private int i = 0;

    public WebPagesTest(MainTest mainTest, WebView webView) {
        this.mainTest = mainTest;
        this.webView = webView;
    }

    public void start() {
        Log.d("JSON", "API STARTING...");

        AsyncHttpClient client = new AsyncHttpClient();

        client.get("http://users.dcc.uchile.cl/~dmadaria/urls1", new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray urlsArray = response.getJSONArray("urls");
                    for (int i=0; i<urlsArray.length(); i++){
                        JSONObject server = urlsArray.getJSONObject(i);
                        urls.add(server.getString("url"));
                    }
                    loadingTime = new long[urls.size()];

                } catch (JSONException e) {
                    Log.d("JSON", "API JSONException");
                    e.printStackTrace();
                }
                startLoading();
            };
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("JSON", "API FAIL...");

            }
            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }
        });
    }

    private void loadNextPage(){
        AsyncHttpClient client = new AsyncHttpClient();
        Log.d("TIMEOUT", client.getConnectTimeout()+ " " + client.getResponseTimeout());
        client.setMaxRetriesAndTimeout(0,0);
        if (i<urls.size()) {

            new RetrieveFeedTask().execute();
            webView.loadUrl(urls.get(i));

            /*client.get(urls.get(i), new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    webView.loadUrl(urls.get(i));
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    loadingTime[i] = -1;
                    mainTest.onWebPageLoaded(urls.get(i), loadingTime[i]);

                    //urlsTime.setText(urlsTime.getText() + "\n" + urls.get(i) + ": ERROR AL CARGAR, CODE: " + statusCode);
                    i++;
                    loadNextPage();
                }
            });*/
        }
        else {
            mainTest.onWebPageTestFinish();
            i = 0;
        }
    }

    private void startLoading() {
        Activity testActivity = (Activity) webView.getContext();
        testActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        startTime = System.currentTimeMillis();
                    }

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        //startTime = -1;
                        Log.d("ERRORview", request.toString() + " " + error.toString());
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        finishTime = System.currentTimeMillis();
                        //if (startTime == -1)
                        //    urlsTime.setText(urlsTime.getText() + "\n" + urls.get(i) + ": ERROR AL CARGAR");
                        loadingTime[i] = finishTime - startTime;
                        mainTest.onWebPageLoaded(urls.get(i), loadingTime[i]);
                        i++;
                        loadNextPage();
                    }


                });
                webView.getSettings().setAppCacheEnabled(false);
                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

                //new httpStatus().execute(urls[i]);

                loadNextPage();
            }
        });
    }
    class RetrieveFeedTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls.get(i));
                urlConnection = (HttpURLConnection) url.openConnection();
                int responseCode = urlConnection.getResponseCode();
                runOn
                webView.loadUrl(urls.get(i));

                Log.d("STATUS", urls.get(i)+ " " + responseCode);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                urlConnection.disconnect();
            }
            return null;
        }
    }

}


