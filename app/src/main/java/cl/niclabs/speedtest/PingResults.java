package cl.niclabs.speedtest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class PingResults {
    private ArrayList<Double> rttList;
    private double min;
    private double avg;
    private double max;
    private double mdev; //jitter

    public PingResults(Process process) throws IOException {
        rttList = new ArrayList<>();

        StringBuffer echo = new StringBuffer();

        int exit = process.exitValue();

        if (exit == 0) {
            InputStreamReader reader = new InputStreamReader(process.getInputStream());
            BufferedReader buffer = new BufferedReader(reader);
            String line;
            int index;

            while ((line = buffer.readLine()) != null) {
                Log.d("PING", line);

                if ((index = line.indexOf("time=")) >= 0){
                    line = line.substring(index + 5);
                    String rtt = line.substring(0, line.indexOf(" "));
                    rttList.add(Double.parseDouble(rtt));
                }

                else if ((index = line.indexOf("rtt min/avg/max/mdev")) >= 0){
                    line = line.substring(index + 23);
                    line = line.substring(0, line.indexOf(" "));
                    String[] statistics = line.split("/");
                    min = Double.parseDouble(statistics[0]);
                    avg = Double.parseDouble(statistics[1]);
                    max = Double.parseDouble(statistics[2]);
                    mdev = Double.parseDouble(statistics[3]);
                }
            }

            Log.d("PING", "avg="+avg);
        } else {
            Log.d("PING", "failed, exit = 2");
        }
    }

    public double getMin() {
        return min;
    }

    public double getAvg() {
        return avg;
    }

    public double getMax() {
        return max;
    }

    public double getMdev() {
        return mdev;
    }

    public ArrayList<Double> getRttList(){
        return rttList;
    }
}
