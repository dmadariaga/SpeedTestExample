package cl.niclabs.speedtest;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestError;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    private CustomGauge speedTestGauge;
    private GraphView downloadGraph;
    private GraphView uploadGraph;
    private GraphView pingGraph;
    private LineGraphSeries<DataPoint> downloadSeries;
    private LineGraphSeries<DataPoint> uploadSeries;
    private BarGraphSeries<DataPoint> pingSeries;
    private TextView downloadTransferRate;
    private TextView uploadTransferRate;
    private TextView latency;
    private TextView jitter;
    private TextView urlsTime;
    private EditText serverUrl;
    private String host;
    private int fileOctetSize;
    private EditText editTextFileSize;
    private WebView webView;
    private long startTime, finishTime;
    private int i = 0;

/*
144p: &vq=tiny
240p: &vq=small
360p: &vq=medium
480p: &vq=large
720p: &vq=hd720

https://developers.google.com/youtube/iframe_api_reference#Examples
 */

    String frameVideo = "<html><body>" +
                /*"<iframe id='player' class=\"youtube-player\" style=\"border: 0; width: 100%; height: 100%; padding:0px; margin:0px\" id=\"ytplayer\" type=\"text/html\" src=\"http://www.youtube.com/embed/" +
                "Io7AbxE9hYk?start=266&enablejsapi=1" +
                "\" frameborder=\"0\">\n" +
                "</iframe>\n" + */
            "<div id=\"player\"></div>\n" +
            "<script type=\"text/javascript\">\n" +
            "   var tag = document.createElement('script');\n" +
            "   tag.src = 'https://www.youtube.com/iframe_api';\n" +
            "   var firstScriptTag = document.getElementsByTagName('script')[0];\n" +
            "   firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);\n" +
            "   var player;\n" +
            "   var quality = ['tiny', 'small', 'medium', 'large', 'hd720'];\n" +
            "   var lastState = -1;\n" +
            "   var i = 0;\n" +
            "   function onYouTubeIframeAPIReady() {\n" +
            "       player = new YT.Player('player', {\n" +
            "           height: '100%',\n" +
            "           width: '100%',\n" +
            "       playerVars: {\n" +
            "           autoplay: 1,\n" +
            "           controls: 0,\n" +
            "           showinfo: 0\n" +
            "       }," +
            //"           videoId: '6pxRHBw-k8M',\n" +
            "       events: {\n" +
            "            'onReady': onPlayerReady,\n" +
            "            'onStateChange': onPlayerStateChange,\n" +
            "            'onPlaybackQualityChange': onPlayerPlaybackQualityChange\n" +
            "          }\n" +
            "        });\n\n" +
            "   }\n" +

            "   function testEcho(message) {\n" +
            "       window.JSInterface.doEchoTest(message);\n" +
            "   }\n" +
            "   function onPlayerReady(event) {\n" +
            "       player.loadVideoById({'videoId': 'Io7AbxE9hYk',\n" +
            "               'startSeconds': 265,\n" +
            "               'endSeconds': 275,\n" +
            "               'suggestedQuality': quality[i]});\n" + //\"Io7AbxE9hYk\", 265, \"medium\");\n" +
            //"       player.playVideo();\n" +
            //"           player.setPlaybackQuality('tiny');\n" +
            "   }\n" +
            "   function onPlayerStateChange(event){\n" +
            "       testEcho(event.data);\n" +
            "       if (event.data == YT.PlayerState.ENDED && i < quality.length - 1) {\n" +
            "           if (lastState == YT.PlayerState.PAUSED)" +
            "               i = i+1;\n" +
            "           testEcho(player.getPlaybackQuality());\n" +
            "           player.loadVideoById({'videoId': 'Io7AbxE9hYk',\n" +
            "                   'startSeconds': 265,\n" +
            "                   'endSeconds': 275,\n" +
            "                   'suggestedQuality': quality[i]});\n" +
            "       }\n" +
            "       lastState = event.data;\n" +
            "   }\n" +
            "   function onPlayerPlaybackQualityChange(event){\n" +
            "       testEcho('quality: ' + event.data);\n" +
            //"       player.setPlaybackQuality('tiny');\n" +
            //"       testEcho('quality2: ' + event.data);\n" +
            "   }\n" +
            "</script>" +
            "</body></html>";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speedTestGauge = (CustomGauge) findViewById(R.id.gauge3);
        downloadTransferRate = (TextView) findViewById(R.id.downloadTransferRate);
        uploadTransferRate = (TextView) findViewById(R.id.uploadTransferRate);
        latency = (TextView) findViewById(R.id.latency);
        jitter = (TextView) findViewById(R.id.jitter);
        editTextFileSize = (EditText) findViewById(R.id.editText);
        serverUrl = (EditText) findViewById(R.id.server_url);
        urlsTime = (TextView) findViewById(R.id.urls);
        final String[] urls = {"http://www.facebook.com",
                                "http://www.google.cl",
                                "http://172.30.65.56:5000",
                                "http://anakena.dcc.uchile.cl:5000",
                                "http://www.niclabs.cl",
                                "http://www.yapo.cl"
        };
        webView = (WebView) findViewById(R.id.webView);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                startTime = System.currentTimeMillis();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                startTime = -1;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                finishTime = System.currentTimeMillis();

                if (startTime == -1)
                    urlsTime.setText(urlsTime.getText() + "\n" + urls[i] + ": ERROR AL CARGAR");
                else
                    urlsTime.setText(urlsTime.getText() + "\n" + urls[i] + ": " + (finishTime-startTime) + " ms");

                if (i<urls.length-1)
                    webView.loadUrl(urls[++i]);
                else{
                    webView.setWebViewClient(new WebViewClient());
                    webView.loadData(frameVideo, "text/html", "utf-8");
                }
            }


        });
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.addJavascriptInterface(new JSInterface(webView), "JSInterface");
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        webView.loadUrl(urls[i]);
        //webView.loadUrl("https://www.youtube.com/embed/-3OvswCDfpY?showinfo=0&controls=0&rel=0&vq=tiny");

        pingGraph = (GraphView) findViewById(R.id.graph3);
        pingGraph.getViewport().setXAxisBoundsManual(true);
        pingGraph.getViewport().setYAxisBoundsManual(true);
        pingGraph.getViewport().setMinX(0);
        pingGraph.getViewport().setMaxX(10);
        pingGraph.getViewport().setMinY(0);
        pingGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        pingGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

        downloadGraph = (GraphView) findViewById(R.id.graph1);
        downloadGraph.getViewport().setXAxisBoundsManual(true);
        downloadGraph.getViewport().setYAxisBoundsManual(true);
        downloadGraph.getViewport().setMinX(0);
        downloadGraph.getViewport().setMaxX(100);

        downloadGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        downloadGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        uploadGraph = (GraphView) findViewById(R.id.graph2);
        uploadGraph.getViewport().setXAxisBoundsManual(true);
        uploadGraph.getViewport().setYAxisBoundsManual(true);
        uploadGraph.getViewport().setMinX(0);
        uploadGraph.getViewport().setMaxX(100);

        uploadGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        uploadGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
    }
    private void emulateClick(final WebView webview) {
        long delta = 100;
        long downTime = SystemClock.uptimeMillis();
        float x = webview.getLeft() + webview.getWidth()/2; //in the middle of the webview
        float y = webview.getTop() + webview.getHeight()/2;

        final MotionEvent motionEvent = MotionEvent.obtain( downTime, downTime + delta, MotionEvent.ACTION_DOWN, x, y, 0 );
        final MotionEvent motionEvent2 = MotionEvent.obtain( downTime + delta + 1, downTime + delta * 2, MotionEvent.ACTION_UP, x, y, 0 );
        Runnable tapdown = new Runnable() {
            @Override
            public void run() {
                if (webview != null) {
                    webview.dispatchTouchEvent(motionEvent);
                }
            }
        };

        Runnable tapup = new Runnable() {
            @Override
            public void run() {
                if (webview != null) {
                    webview.dispatchTouchEvent(motionEvent2);
                }
            }
        };

        int toWait = 0;
        int delay = 100;
        webview.postDelayed(tapdown, delay);
        delay += 100;
        webview.postDelayed(tapup, delay);

    }
    public void onClickStart(View view) throws Exception {

        View mView = this.getCurrentFocus();
        if (mView != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        }

        host = serverUrl.getText().toString();
        if (editTextFileSize.getText().toString().equals("")){
            editTextFileSize.setText("1");
        }
        fileOctetSize = Integer.parseInt(editTextFileSize.getText().toString());
        downloadGraph.removeAllSeries();
        uploadGraph.removeAllSeries();
        pingGraph.removeAllSeries();

        downloadSeries = new LineGraphSeries<DataPoint>();
        uploadSeries = new LineGraphSeries<DataPoint>();
        downloadGraph.addSeries(downloadSeries);
        uploadGraph.addSeries(uploadSeries);
        pingSeries = new BarGraphSeries<>();
        pingGraph.addSeries(pingSeries);
        pingSeries.setSpacing(20);

        ArrayList<Double> pingRtt = new ArrayList<>();
        double sumRtt = 0;
        double maxRtt = 0d;
        int count = 10;
        /*
        for (int i=0; i<count; i++){
            PingResults pingResults = SpeedTest.ping("ping.online.net", 1);
            Double rtt = pingResults.getMax();
            pingRtt.add(rtt);
            sumRtt += rtt;
            maxRtt = (rtt > maxRtt ? (rtt.intValue()+100)/100*100 : maxRtt);
            pingGraph.getViewport().setMaxY(maxRtt);

            DataPoint point = new DataPoint(i + 0.5, rtt);
            pingSeries.appendData(point, false, 10);
            pingGraph.getGridLabelRenderer().setVerticalLabelsVisible(true);
        }
        latency.setText(" " + sumRtt/10);
        //jitter.setText(" " + pingResults.getMdev());
        */

        /*
        PingResults pingResults = SpeedTest.ping("ping.online.net", count);
        int maxPing = 0;
        latency.setText(" " + pingResults.getAvg());
        jitter.setText(" " + pingResults.getMdev());
        for (int i=0; i<pingResults.getRttList().size(); i++){
            maxPing = (pingResults.getRttList().get(i) > maxPing ? (pingResults.getRttList().get(i).intValue()+100)/100*100 : maxPing);
            DataPoint point = new DataPoint(i + 0.5, pingResults.getRttList().get(i));
            pingSeries.appendData(point, false, count);
        }
        pingGraph.getViewport().setMaxY(maxPing);
        pingGraph.getGridLabelRenderer().setVerticalLabelsVisible(true);*/

        new SpeedTestTask().execute();
    }

    public class SpeedTestTask extends AsyncTask<Void, Void, String> {
        public int downloadPercent = -1;
        public boolean first = true;
        @Override
        protected String doInBackground(final Void... params) {

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onDownloadPacketsReceived(long packetSize,
                                                      float transferRateBitPerSeconds,
                                                      float transferRateOctetPerSeconds) {
                    Log.i("speed-test-app","download transfer rate  : " + transferRateBitPerSeconds + "Bps");
                    new SpeedTestTask1().execute();
                }

                @Override
                public void onDownloadError(SpeedTestError errorCode, String message) {
                    Log.i("speed-test-app","Download error " + errorCode + " occured with message : " + message);
                }

                @Override
                public void onUploadPacketsReceived(long packetSize,
                                                    float transferRateBitPerSeconds,
                                                    float transferRateOctetPerSeconds) {
                    Log.i("speed-test-app","download transfer rate  : " + transferRateOctetPerSeconds + "Bps");
                }

                @Override
                public void onUploadError(SpeedTestError errorCode, String message) {
                    Log.i("speed-test-app","Upload error " + errorCode + " occured with message : " + message);
                }

                @Override
                public void onDownloadProgress(final float percent, final SpeedTestReport downloadReport) {
                    //Log.i("speed-test-app","percentdown"+ percent);
                    if (Math.round(percent) > downloadPercent) {
                        downloadPercent = Math.round(percent);
                        final float transferRateBit = downloadReport.getTransferRateBit();

                        if (first){
                            if (downloadPercent > 0){
                                setData1(0, transferRateBit);
                            }
                            first = false;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                downloadTransferRate.setText((int) (transferRateBit / 1000) + " ");
                                speedTestGauge.setValue((int) transferRateBit);
                                //setData1(downloadReport.getReportTime() - downloadReport.getStartTime(), transferRateBit);
                                setData1(downloadPercent, transferRateBit);
                                //Log.d("SetData", downloadReport.getReportTime() - downloadReport.getStartTime() + " " + transferRateBit / 1000);
                            }
                        });
                    }
                }

                @Override
                public void onUploadProgress(float percent,SpeedTestReport uploadReport) {
                    Log.i("speed-test-app","percentup"+ percent);

                }

            });

            //speedTestSocket.startDownload("ping.online.net", 80, "/1Mo.dat");
            speedTestSocket.startDownload(host, 5000, "/speedtest/" + fileOctetSize);

            return null;
        }
    }

    public class SpeedTestTask1 extends AsyncTask<Void, Void, String> {
        public int uploadPercent = -1;
        public float max = 0;
        public boolean first = true;

        @Override
        protected String doInBackground(final Void... params) {

            SpeedTestSocket speedTestSocket = new SpeedTestSocket();
            speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

                @Override
                public void onDownloadPacketsReceived(long packetSize,
                                                      float transferRateBitPerSeconds,
                                                      float transferRateOctetPerSeconds) {
                    Log.i("speed-test-app","download transfer rate  : " + transferRateBitPerSeconds + "Bps");
                }

                @Override
                public void onDownloadError(SpeedTestError errorCode, String message) {
                    Log.i("speed-test-app","Download error " + errorCode + " occured with message : " + message);
                }

                @Override
                public void onUploadPacketsReceived(long packetSize,
                                                    float transferRateBitPerSeconds,
                                                    float transferRateOctetPerSeconds) {
                    Log.i("speed-test-app","download transfer rate  : " + transferRateOctetPerSeconds + "Bps");
                }

                @Override
                public void onUploadError(SpeedTestError errorCode, String message) {
                    Log.i("speed-test-app","Upload error " + errorCode + " occured with message : " + message);

                }

                @Override
                public void onDownloadProgress(final float percent, final SpeedTestReport downloadReport) {
                    //Log.i("speed-test-app","percentdown"+ percent);
                    final float transferRateBit = downloadReport.getTransferRateBit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //percentText.setText(transferRateBit +" ");
                        }
                    });
                }

                @Override
                public void onUploadProgress(float percent,final SpeedTestReport uploadReport) {
                    if (Math.round(percent) > uploadPercent) {
                        /*if (first){
                            uploadGraph.getViewport().setMinX((uploadReport.getReportTime() - uploadReport.getStartTime())/1000);
                            Log.d("SetData", uploadReport.getReportTime() - uploadReport.getStartTime() + " ");
                            first = false;
                        }*/
                        uploadPercent = Math.round(percent);
                        final float transferRateBit = uploadReport.getTransferRateBit();
                        Log.i("speed-test-app", "percentup" + uploadPercent + " bps " + transferRateBit);
                        max = (transferRateBit > max ? transferRateBit : max);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                uploadTransferRate.setText((int) (transferRateBit / 1000) + " ");
                                speedTestGauge.setValue((int) transferRateBit);
                                //setData2(uploadReport.getReportTime() - uploadReport.getStartTime(), transferRateBit);
                                setData2(uploadPercent, transferRateBit);
                                //Log.d("SetData", uploadReport.getReportTime() - uploadReport.getStartTime() + " " + transferRateBit);
                            }
                        });
                    }
                }

            });
            //Log.d("PING", speedTestSocket.getSocketTimeout() + " TIMEOUT");
            //speedTestSocket.setSocketTimeout(5000);
            //speedTestSocket.startUpload("1.testdebit.info", 80, "/", 1000000);
            speedTestSocket.startUpload(host, 5000, "/speedtest/", fileOctetSize*1000000);
            Log.d("MAX", max + " bit/s");
            return null;
        }
    }

    private void setData1(long time, float transferRateBit) {
        DataPoint newPoint = new DataPoint(time, transferRateBit/1000);
        downloadSeries.appendData(newPoint, false, 40000);
        //downloadGraph.getViewport().setMaxX(time/1000.0);
        //Log.d("GetMAX", transferRateBit/1000+" "+downloadGraph.getViewport().getMaxY(true));
        downloadGraph.getViewport().setMaxY( (int) (downloadGraph.getViewport().getMaxY(true) + 50) / 50 *50);

        //mGraph.addSeries(new LineGraphSeries<DataPoint>(new DataPoint[] {newPoint}));
    }
    private void setData2(long time, float transferRateBit) {
        DataPoint newPoint = new DataPoint(time, transferRateBit/1000);
        uploadSeries.appendData(newPoint, false, 4000);
        //uploadGraph.getViewport().setMaxX(time/1000.0);
        uploadGraph.getViewport().setMaxY( (int) (uploadGraph.getViewport().getMaxY(true) + 50) / 50 *50);

        //mGraph.addSeries(new LineGraphSeries<DataPoint>(new DataPoint[] {newPoint}));
    }


    public class JSInterface{

        private WebView mAppView;
        public JSInterface  (WebView appView) {
            this.mAppView = appView;
        }

        @JavascriptInterface
        public void doEchoTest(String echo){
            Log.d("VideoView", echo);
        }

        @JavascriptInterface
        public void playVideo(){
            emulateClick(webView);
        }
    }
}
