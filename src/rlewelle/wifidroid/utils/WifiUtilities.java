package rlewelle.wifidroid.utils;

import android.net.wifi.ScanResult;
import rlewelle.wifidroid.data.AccessPoint;

import java.util.ArrayList;
import java.util.List;

public class WifiUtilities {
    // All in MHz
    private static final int MIN_2GHZ_FREQ = 2412;
    private static final int MAX_2GHZ_FREQ = 2472;
    private static final int CHANNEL_BANDWIDTH = 5;

    /**
     * Given a frequency in the 2.4GHz range, returns the corresponding channel number
     * @param frequency
     * @return -1 if the frequency is outside of the 2.4GHz range, the channel number otherwise
     */
    public static int convertFrequencyToChannel(int frequency) {
        if (frequency >= MIN_2GHZ_FREQ && frequency <= MAX_2GHZ_FREQ)
            return (frequency - MIN_2GHZ_FREQ) / CHANNEL_BANDWIDTH + 1;

        return -1;
    }

    // mumble mumble Java mumble mumble functional mumble mumble map
    public static List<AccessPoint> accessPointsFromScanResults(List<ScanResult> scanResults) {
        if (scanResults == null)
            return null;

        List<AccessPoint> data = new ArrayList<AccessPoint>(scanResults.size());

        for (ScanResult result : scanResults)
            data.add(AccessPoint.getInstance(result));

        return data;
    }
}
