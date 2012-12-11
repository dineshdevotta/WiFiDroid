package rlewelle.wifidroid.data;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;
import rlewelle.wifidroid.utils.WifiUtilities;

/**
 * Information that would uniquely identify a single access point
 */
public class AccessPoint implements Parcelable {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(BSSID);
        parcel.writeString(SSID);
        parcel.writeInt(frequency);
    }

    public static final Parcelable.Creator<AccessPoint> CREATOR = new Parcelable.Creator<AccessPoint>() {
        @Override
        public AccessPoint createFromParcel(Parcel parcel) {
            AccessPoint ap = new AccessPoint();
            ap.BSSID = parcel.readString();
            ap.SSID = parcel.readString();
            ap.frequency = parcel.readInt();

            return ap;
        }

        @Override
        public AccessPoint[] newArray(int size) {
            return new AccessPoint[size];
        }
    };
}
