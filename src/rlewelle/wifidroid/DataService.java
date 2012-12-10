package rlewelle.wifidroid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.utils.WifiUtilities;

import java.util.List;

public class DataService extends Service {
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = "rlewelle.wifidroid.DataService.SCAN_RESULTS";

    private final IBinder binder = new DataServiceBinder();

    private WifiManager wifi;
    private List<AccessPoint> latestResults;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("rlewelle.DataService.onCreate", "derp");

        // Grab instance of the wifi manager
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Register receiver for when scan results are available
        registerReceiver(scanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        //
        wifi.startScan();
    }

    @Override
    public void onDestroy() {
        Log.d("rlewelle.DataService.onDestroy", "derp");
        unregisterReceiver(scanResultReceiver);
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        Log.d("rlewelle.DataService.onBind", intent.toString());
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("DataService.unBind", intent.toString());
        return super.onUnbind(intent);
    }

    /**
     * Receiver for when new ScanResults are made available by the OS
     */
    private BroadcastReceiver scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("DataService.scanner", "DataService receives new data");
            latestResults = WifiUtilities.accessPointsFromScanResults(wifi.getScanResults());
            sendBroadcast(new Intent(SCAN_RESULTS_AVAILABLE_ACTION));
        }
    };

    /**
     * Get the latest scan results made available by the OS
     * @return the results if recorded, null otherwise
     */
    public List<AccessPoint> getLatestResults() {
        return latestResults;
    }

    public boolean isWifiEnabled() {
        return wifi.isWifiEnabled();
    }

    public boolean startScan() {
        if (!isWifiEnabled())
            return false;

        return wifi.startScan();
    }

    public class DataServiceBinder extends Binder {
        public DataService getService() { return DataService.this; }
    }
}
