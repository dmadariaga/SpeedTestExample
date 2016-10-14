package cl.niclabs.speedtest;

import java.io.IOException;

public class SpeedTest1 {

    public static PingResults ping(String host, int count) throws IOException, InterruptedException {
        String command = "ping -c " + count + " " + host;

        Process process = Runtime.getRuntime().exec(command);
        process.waitFor();

        return new PingResults(process);
    }
}
