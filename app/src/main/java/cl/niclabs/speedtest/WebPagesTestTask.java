package cl.niclabs.speedtest;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebPagesTestTask extends AsyncTask<String, Void, Void> {
    WebPagesTest webPagesTest;
    public WebPagesTestTask(WebPagesTest webPagesTest) {
        this.webPagesTest = webPagesTest;
    }

    @Override
    protected Void doInBackground(String... params) {
        URL url;
        HttpURLConnection urlConnection = null;
        int responseCode = -1;
        try {
            url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            responseCode = urlConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            webPagesTest.onResponseReceived(responseCode);
            Log.d("STATUS", params[0] + " " + responseCode);
            urlConnection.disconnect();
        }
        return null;
    }
}