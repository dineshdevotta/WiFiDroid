package rlewelle.wifidroid.data;

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
    public Location getLocation() { return location; }
}
