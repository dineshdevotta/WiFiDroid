package rlewelle.wifidroid.data;

import android.net.wifi.WifiManager;

/**
 * Time-dependent data for an access point
 */
public class AccessPointDataPoint {
    private int level;
    private Location location;

    public AccessPointDataPoint(int level, Location location) {
        this.level = level;
        this.location = location;
    }

    public int getLevel() { return level; }
    public float getNormalizedLevel() { return (float) WifiManager.calculateSignalLevel(level, 32) / 32.0f; }

    public Location getLocation() { return location; }
}
