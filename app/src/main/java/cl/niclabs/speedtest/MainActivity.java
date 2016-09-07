package cl.niclabs.speedtest;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
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
                                "http://www.yapo.cl"};
        webView = (WebView) findViewById(R.id.webView);
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
            }
        });
        webView.getSettings().setAppCacheEnabled(false);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.loadUrl(urls[i]);

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
                        /*
                        if (first){
                            downloadGraph.getViewport().setMinX((downloadReport.getReportTime() - downloadReport.getStartTime())/1000);
                            Log.d("SetData", downloadReport.getReportTime() - downloadReport.getStartTime() + " ");

                            first = false;
                        }
                        Log.d("PING", "MIN X" + downloadGraph.getViewport().getMinX(true));
                        */
                        downloadPercent = Math.round(percent);
                        final float transferRateBit = downloadReport.getTransferRateBit();
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

}
