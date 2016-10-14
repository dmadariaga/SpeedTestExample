package cl.niclabs.speedtest;


import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import fr.bmartel.speedtest.SpeedTestMode;

public class SpeedTest {
    private MainTest mainTest;
    private int fileOctetSize;
    private String host;

    public SpeedTest(MainTest mainTest, int fileOctetSize) {
        this.fileOctetSize = fileOctetSize;
        this.mainTest = mainTest;
    }

    public void start(){
        final ArrayList urls = new ArrayList<>();

        final String url = "http://blasco.duckdns.org:5000/activeServers/";
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONArray activeServers = response.getJSONArray("data");
                    JSONObject server = activeServers.getJSONObject(0);
                    host = server.getString("url");
                    host = host.substring(7,host.indexOf(":5000"));

                    startSpeedTest(SpeedTestMode.DOWNLOAD, host, fileOctetSize);

                    Log.d("JSON", host);

                } catch (JSONException e) {
                    Log.d("JSON", "API JSONException");
                    e.printStackTrace();
                }
            };
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });

    }

    public void startSpeedTest(SpeedTestMode mode, String host, int fileOctetSize) {
        new SpeedTestTask(this, host, fileOctetSize).execute(mode);
    }

    public void onSpeedTestFinish(SpeedTestMode mode) {
        switch (mode){
            case DOWNLOAD:
                startSpeedTest(SpeedTestMode.UPLOAD, host, fileOctetSize);
                break;
            case UPLOAD:
                mainTest.onSpeedTestFinish();
                break;
        }
    }

    public void onProgress(SpeedTestMode mode, int progressPercent, float transferRateBit) {
        mainTest.onSpeedTestProgress(mode, progressPercent, transferRateBit);
    }
}
