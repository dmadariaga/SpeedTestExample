package cl.niclabs.speedtest;

import fr.bmartel.speedtest.SpeedTestMode;

public interface MainTest {
    void onSpeedTestFinish();

    void onSpeedTestProgress(SpeedTestMode mode, int progressPercent, float transferRateBit);

    void onWebPageLoaded(String url, long loadingTime, long sizeByte);

    void onWebPageTestFinish();

    void onVideoEnded(String quality, int timesBuffering, long totalBytes);

    void onVideoTestFinish();
}
