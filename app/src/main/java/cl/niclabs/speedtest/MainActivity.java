package cl.niclabs.speedtest;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.NumberFormat;
import java.util.ArrayList;

import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestError;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import pl.pawelkleczkowski.customgauge.CustomGauge;

public class MainActivity extends AppCompatActivity {

    private CustomGauge speedTestGauge;
    private TextView percentText;
    private LineChart mChart;
    private GraphView downloadGraph;
    private GraphView uploadGraph;
    private LineGraphSeries<DataPoint> downloadSeries;
    private LineGraphSeries<DataPoint> uploadSeries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        percentText = (TextView) findViewById(R.id.percent);
        speedTestGauge = (CustomGauge) findViewById(R.id.gauge3);
        mChart = (LineChart) findViewById(R.id.chart1);

        downloadGraph = (GraphView) findViewById(R.id.graph1);
        downloadGraph.getViewport().setXAxisBoundsManual(true);
        downloadGraph.getViewport().setYAxisBoundsManual(true);
        downloadGraph.getViewport().setMinX(0);
        downloadGraph.getViewport().setMaxX(1);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(0);

        downloadGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + "kbps";
                }
            }
        });

        uploadGraph = (GraphView) findViewById(R.id.graph2);
        uploadGraph.getViewport().setXAxisBoundsManual(true);
        uploadGraph.getViewport().setYAxisBoundsManual(true);
        uploadGraph.getViewport().setMinX(0);
        uploadGraph.getViewport().setMaxX(1);

        uploadGraph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(nf, nf){
            @Override
            public String formatLabel(double value, boolean isValueX) {
                if (isValueX) {
                    // show normal x values
                    return super.formatLabel(value, isValueX);
                } else {
                    // show currency for y values
                    return super.formatLabel(value, isValueX) + "kbps";
                }
            }
        });


        downloadSeries = new LineGraphSeries<DataPoint>();
        uploadSeries = new LineGraphSeries<DataPoint>();
        downloadGraph.addSeries(downloadSeries);
        uploadGraph.addSeries(uploadSeries);
        setData1(0,0);
        setData2(0,0);
        new SpeedTestTask().execute();
    }

    public void onClickStart(View view){
        new SpeedTestTask1().execute();
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
                            //mChart.animateX(0);
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
                            //percentText.setText(transferRateBit +" ");
                            if (transferRateBit >= 1000000)
                                return;
                            speedTestGauge.setValue((int) transferRateBit);
                            setData2(uploadReport.getReportTime()-uploadReport.getStartTime(), transferRateBit);
                            Log.d("SetData", uploadReport.getReportTime()-uploadReport.getStartTime() + " " + transferRateBit);
                            //mChart.animateX(0);
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

    private void setData(long time, float range) {

        Entry value = new Entry(time, range);
        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.addEntry(value);//.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            ArrayList<Entry> values = new ArrayList<Entry>();
            values.add(value);
            set1 = new LineDataSet(values, "DataSet 1");

            // set the line to be drawn like this "- - - - - -"
           // set1.enableDashedLine(10f, 5f, 0f);
            //set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLACK);
            //set1.setCircleColor(Color.BLACK);
            set1.setLineWidth(1f);
            //set1.setCircleRadius(0.5f);
            //set1.setDrawCircleHole(false);
            //set1.setValueTextSize(9f);
            set1.setDrawCircles(false);
            //set1.setFillColor(Color.BLACK);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }
    }
}
