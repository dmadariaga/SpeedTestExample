package cl.niclabs.speedtest;

import fr.bmartel.speedtest.SpeedTestMode;

public interface MainTest {
    void onSpeedTestFinish();

    void onSpeedTestProgress(SpeedTestMode mode, int progressPercent, float transferRateBit);

    void onWebPageLoaded(String url, long loadingTime);

    void onWebPageTestFinish();

    void onVideoEnded(String quality, int timesBuffering);

    void onVideoTestFinish();
}
