package rlewelle.wifidroid.data;

import android.net.wifi.ScanResult;
import rlewelle.wifidroid.utils.WifiUtilities;

import java.util.Calendar;

/**
 * Information that would uniquely identify a single access point
 */
public class AccessPoint {
    private String SSID;
    private String BSSID;
    private int frequency;

    private AccessPoint() {}

    public static AccessPoint getInstance(ScanResult result) {
        AccessPoint ap = new AccessPoint();
        ap.BSSID = result.BSSID;
        ap.SSID = result.SSID;
        ap.frequency = result.frequency;

        return ap;
    }

    @Override
    public int hashCode() {
        // Perhaps not the highest-quality hash, but unless we see perf. problems, not an issue
        return BSSID.hashCode() + SSID.hashCode() + frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof AccessPoint)) return false;

        AccessPoint ap = (AccessPoint)o;

        return ap.BSSID.equals(BSSID) &&
               ap.SSID.equals(SSID) &&
               ap.frequency == frequency;
    }

    public String getBSSID() { return BSSID; }
    public String getSSID() { return SSID; }
    public int getFrequency() { return frequency; }

    public int getChannel() {
        return WifiUtilities.convertFrequencyToChannel(frequency);
    }
}
