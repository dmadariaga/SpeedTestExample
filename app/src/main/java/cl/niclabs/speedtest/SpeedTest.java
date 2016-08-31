package cl.niclabs.speedtest;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SpeedTest {

    public static PingResults ping(String host, int count) throws IOException, InterruptedException {
        String command = "ping -c " + count + " " + host;

        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        return new PingResults(process);
    }
}
