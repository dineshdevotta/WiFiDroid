package rlewelle.wifidroid;

import android.app.Activity;
import android.os.Bundle;

public class WifiMeterActivity extends Activity {
    public static final String EXTRA_BSSID = "rlewelle.wifidroid.wifimeteractivity.BSSID";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_meter);

        
    }
}