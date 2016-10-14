package cl.niclabs.speedtest;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import fr.bmartel.speedtest.SpeedTestMode;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity implements MainTest{

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
    private EditText editTextFileSize;
    private WebView webView;
    private int i = 0;

/*
144p: &vq=tiny
240p: &vq=small
360p: &vq=medium
480p: &vq=large
720p: &vq=hd720

https://developers.google.com/youtube/iframe_api_reference#Examples
 */

    private ArrayList<String> urls;
    private long[] loadingTime;
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
        webView = (WebView) findViewById(R.id.webView);
        //webView.addJavascriptInterface(new JSInterface(webView), "JSInterface");
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
        if (editTextFileSize.getText().toString().equals("")){
            editTextFileSize.setText("1");
        }
        final int fileOctetSize = Integer.parseInt(editTextFileSize.getText().toString());

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

        urlsTime.setText("");
        urls = new ArrayList<>();

        //startSpeedTest(fileOctetSize);
        startWebPagesTest();
        View mView = this.getCurrentFocus();
        if (mView != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mView.getWindowToken(), 0);
        }
        /*
        //host = serverUrl.getText().toString();
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
            PingResults pingResults = SpeedTest1.ping("ping.online.net", 1);
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
        PingResults pingResults = SpeedTest1.ping("ping.online.net", count);
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

        //new SpeedTestTask().execute();
    }

    public void updateDownloadGraph(final int downloadPercent, final float transferRateBit){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadTransferRate.setText((int) (transferRateBit / 1000) + " ");
                //speedTestGauge.setValue((int) transferRateBit);
                if (downloadSeries.isEmpty()){
                    DataPoint firstPoint = new DataPoint(0, transferRateBit/1000);
                    downloadSeries.appendData(firstPoint, false, 40000);
                }

                DataPoint newPoint = new DataPoint(downloadPercent, transferRateBit/1000);
                downloadSeries.appendData(newPoint, false, 40000);

                downloadGraph.getViewport().setMaxY( (int) (downloadGraph.getViewport().getMaxY(true) + 50) / 50 *50);
            }
        });
    }

    public void updateUploadGraph(final int uploadPercent, final float transferRateBit){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                uploadTransferRate.setText((int) (transferRateBit / 1000) + " ");
                //speedTestGauge.setValue((int) transferRateBit);
                if (uploadSeries.isEmpty()){
                    DataPoint firstPoint = new DataPoint(0, transferRateBit/1000);
                    uploadSeries.appendData(firstPoint, false, 40000);
                }

                DataPoint newPoint = new DataPoint(uploadPercent, transferRateBit/1000);
                uploadSeries.appendData(newPoint, false, 4000);

                uploadGraph.getViewport().setMaxY( (int) (uploadGraph.getViewport().getMaxY(true) + 50) / 50 *50);
            }
        });
    }

    public void startSpeedTest(int fileOctetSize) {
        new SpeedTest(this, fileOctetSize).start();
    }

    public void startWebPagesTest() {
        new WebPagesTest(this, webView).start();
    }

    @Override
    public void onSpeedTestFinish() {
        startWebPagesTest();
    }

    @Override
    public void onSpeedTestProgress(SpeedTestMode mode, int progressPercent, float transferRateBit) {
        switch (mode){
            case DOWNLOAD:
                updateDownloadGraph(progressPercent, transferRateBit);
                break;
            case UPLOAD:
                updateUploadGraph(progressPercent, transferRateBit);
                break;
        }
    }

    @Override
    public void onWebPageLoaded(String url, long loadingTime){
        urlsTime.setText(urlsTime.getText() + "\n" + url + ": " + loadingTime + " ms");
    }

    @Override
    public void onWebPageTestFinish() {
        //startVideoTest();
    }

    @Override
    public void onVideoEnded(final String quality, final int timesBuffering) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                urlsTime.setText(urlsTime.getText() + "\nTime buffering in " + quality + ": " + timesBuffering+ "ms");
            }
        });
    }

    @Override
    public void onVideoTestFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("about:blank");
            }
        });
    }

    private void startVideoTest() {
        new VideoTest(this, webView).start();
    }

}
