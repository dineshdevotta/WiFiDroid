package rlewelle.wifidroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.*;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.utils.WifiUtilities;

import java.util.*;

public class DataService extends Service {
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = "rlewelle.wifidroid.DataService.SCAN_RESULTS";

    private final IBinder binder = new DataServiceBinder();

    private WifiManager wifiManager;
    private NotificationManager notificationManager;

    private Set<AccessPoint> latestResults;
    private Set<AccessPoint> aggregatedResults;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("rlewelle.DataService.onCreate", "derp");

        // Grab instance of the wifiManager manager
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Register receiver for when scan results are available
        registerReceiver(scanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        //
        wifiManager.startScan();

        // Throw an icon in the notification bar so the user knows we're running)
        Notification notification = new Notification.Builder(this)
            .setContentTitle("Derp")
            .setContentText("Herp")
            .setSmallIcon(R.drawable.recording)
            .build();

        startForeground(1, notification);
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
        latestResults = new HashSet<AccessPoint>(WifiUtilities.accessPointsFromScanResults(wifiManager.getScanResults()));

        if (aggregatedResults == null)
            aggregatedResults = new HashSet<AccessPoint>();

        // We defined the equality of access points based on their BSSIDs
        // but not the last time that we saw them. We want to update the
        // last seen time, so remove them then re-add them.
        // A map may be a better choice here.
        aggregatedResults.removeAll(latestResults);
        aggregatedResults.addAll(latestResults);

        sendBroadcast(new Intent(SCAN_RESULTS_AVAILABLE_ACTION));
        }
    };

    public AccessPoint getAccessPointStatus(String BSSID) {
        if (aggregatedResults == null) return null;

        for (AccessPoint ap : aggregatedResults) {
            if (ap.BSSID.equals(BSSID)) {
                return ap;
            }
        }

        return null;
    }

    /**
     * Get the latest scan results made available by the OS
     * @return the results if recorded, null otherwise
     */
    public Set<AccessPoint> getLatestResults() {
        return latestResults;
    }

    public Set<AccessPoint> getAggregatedResults() {
        return aggregatedResults;
    }

    public void clearAggregatedResults() {
        aggregatedResults.clear();
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    public boolean startScan() {
        if (!isWifiEnabled())
            return false;

        return wifiManager.startScan();
    }

    public class DataServiceBinder extends Binder {
        public DataService getService() { return DataService.this; }
    }

    public interface IDataServicable {
        public void onServiceConnected();
        public void onServiceDisconnected();
        public void onScanResultsReceived();
        public Context getContext();
    }

    public static class DataServiceLink {
        private DataService service;
        private IDataServicable client;

        public DataServiceLink(IDataServicable client) {
            this.client = client;
        }

        public void onCreate() {
            client.getContext().registerReceiver(resultsReceiver, new IntentFilter(DataService.SCAN_RESULTS_AVAILABLE_ACTION));
            client.getContext().bindService(new Intent(client.getContext(), DataService.class), serviceConnection, Context.BIND_AUTO_CREATE);
        }

        public void onDestroy() {
            client.getContext().unregisterReceiver(resultsReceiver);
            client.getContext().unbindService(serviceConnection);
        }

        private ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder binder) {
                service = ((DataService.DataServiceBinder) binder).getService();
                client.onServiceConnected();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                Toast.makeText(client.getContext(), "The Wifi DataService was unexpectedly shutdown!", Toast.LENGTH_LONG);

                service = null;
                client.onServiceDisconnected();
            }
        };

        private BroadcastReceiver resultsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                client.onScanResultsReceived();
            }
        };

        public DataService getService() { return service; }
    }
}
