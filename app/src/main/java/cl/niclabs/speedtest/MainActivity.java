package cl.niclabs.speedtest;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;

import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestError;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    private CustomGauge speedTestGauge;
    private GraphView downloadGraph;
    private GraphView uploadGraph;
    private GraphView latencyGraph;
    private LineGraphSeries<DataPoint> downloadSeries;
    private LineGraphSeries<DataPoint> uploadSeries;
    private BarGraphSeries<DataPoint> latencySeries;
    private TextView downloadTransferRate;
    private TextView uploadTransferRate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speedTestGauge = (CustomGauge) findViewById(R.id.gauge3);
        downloadTransferRate = (TextView) findViewById(R.id.downloadTransferRate);
        uploadTransferRate = (TextView) findViewById(R.id.uploadTransferRate);

        latencyGraph = (GraphView) findViewById(R.id.graph3);

        downloadGraph = (GraphView) findViewById(R.id.graph1);
        downloadGraph.getViewport().setXAxisBoundsManual(true);
        downloadGraph.getViewport().setYAxisBoundsManual(true);
        downloadGraph.getViewport().setMinX(0);
        downloadGraph.getViewport().setMaxX(1);

        downloadGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        downloadGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        uploadGraph = (GraphView) findViewById(R.id.graph2);
        uploadGraph.getViewport().setXAxisBoundsManual(true);
        uploadGraph.getViewport().setYAxisBoundsManual(true);
        uploadGraph.getViewport().setMinX(0);
        uploadGraph.getViewport().setMaxX(1);

        uploadGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        uploadGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        downloadSeries = new LineGraphSeries<DataPoint>();
        uploadSeries = new LineGraphSeries<DataPoint>();
        downloadGraph.addSeries(downloadSeries);
        uploadGraph.addSeries(uploadSeries);
        setData1(0,0);
        setData2(0,0);
        //new SpeedTestTask().execute();
    }

    public void onClickStart(View view) throws Exception {

        StringBuffer echo = new StringBuffer();
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("ping -c 1 " + "ping.online.net");
        proc.waitFor();
        int exit = proc.exitValue();
        if (exit == 0) {
            InputStreamReader reader = new InputStreamReader(proc.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line = "";
            while ((line = buffer.readLine()) != null) {
                echo.append(line + "\n");
            }
            Log.d("PING", echo.toString());
        } else if (exit == 1) {
            Log.d("PING", "failed, exit = 1");
        } else {
            Log.d("PING", "failed, exit = 2");
        }

        new SpeedTestTask().execute();
    }

    public class SpeedTestTask extends AsyncTask<Void, Void, String> {

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
                    final float transferRateBit = downloadReport.getTransferRateBit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            downloadTransferRate.setText( (int)(transferRateBit/1000) + " ");
                            speedTestGauge.setValue((int) transferRateBit);
                            setData1(downloadReport.getReportTime()-downloadReport.getStartTime(), transferRateBit);
                            Log.d("SetData", downloadReport.getReportTime()-downloadReport.getStartTime() + " " + transferRateBit/1000);
                        }
                    });
                }

                @Override
                public void onUploadProgress(float percent,SpeedTestReport uploadReport) {
                    Log.i("speed-test-app","percentup"+ percent);

                }

            });

            speedTestSocket.startDownload("ping.online.net", 80, "/1Mo.dat");

            return null;
        }
    }

    public class SpeedTestTask1 extends AsyncTask<Void, Void, String> {

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
                    Log.i("speed-test-app","percentup"+ percent);
                    final float transferRateBit = uploadReport.getTransferRateBit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            uploadTransferRate.setText( (int)(transferRateBit/1000) + " ");
                            if (transferRateBit >= 1000000)
                                return;
                            speedTestGauge.setValue((int) transferRateBit);
                            setData2(uploadReport.getReportTime()-uploadReport.getStartTime(), transferRateBit);
                            Log.d("SetData", uploadReport.getReportTime()-uploadReport.getStartTime() + " " + transferRateBit);
                        }
                    });
                }

            });

            speedTestSocket.startUpload("1.testdebit.info", 80, "/", 10000000);
            return null;
        }
    }

    private void setData1(long time, float transferRateBit) {
        DataPoint newPoint = new DataPoint(time/1000.0, transferRateBit/1000);
        downloadSeries.appendData(newPoint, false, 4000);
        downloadGraph.getViewport().setMaxX(time/1000.0);
        //Log.d("GetMAX", transferRateBit/1000+" "+downloadGraph.getViewport().getMaxY(true));
        downloadGraph.getViewport().setMaxY( (int) (downloadGraph.getViewport().getMaxY(true) + 50) / 50 *50);

        //mGraph.addSeries(new LineGraphSeries<DataPoint>(new DataPoint[] {newPoint}));
    }
    private void setData2(long time, float transferRateBit) {
        DataPoint newPoint = new DataPoint(time/1000.0, transferRateBit/1000);
        uploadSeries.appendData(newPoint, false, 4000);
        uploadGraph.getViewport().setMaxX(time/1000.0);
        uploadGraph.getViewport().setMaxY( (int) (uploadGraph.getViewport().getMaxY(true) + 50) / 50 *50);

        //mGraph.addSeries(new LineGraphSeries<DataPoint>(new DataPoint[] {newPoint}));
    }

}
