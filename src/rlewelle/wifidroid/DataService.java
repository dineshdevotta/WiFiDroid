package rlewelle.wifidroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.*;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.data.AccessPointDataPoint;
import rlewelle.wifidroid.utils.WifiUtilities;

import java.util.*;

public class DataService extends Service {
    public static final String SCAN_RESULTS_AVAILABLE_ACTION = "rlewelle.wifidroid.DataService.SCAN_RESULTS";

    private final IBinder binder = new DataServiceBinder();

    private WifiManager wifiManager;

    private ArrayList<Long> updateTimes = new ArrayList<>();

    // How long (in milliseconds) to wait before automatically requesting another update
    // once we have received an update. A value of zero indicates to ask for another
    // update the moment we get the update. A value < 0 indicates that we should not
    // automatically refresh.
    private long updateDelay = -1;
    private Timer updateTimer = new Timer(true);

    // The complete data set that we are currently operating with
    // Each access point is associated with a history of data points associated with a specific time
    // I'm going to assume that entries are added to the list in order of increasing time
    private Map<AccessPoint, List<Pair<Long, AccessPointDataPoint>>> data = new HashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // Grab instance of the wifiManager manager
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        // Register receiver for when scan results are available
        registerReceiver(scanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // We don't know when the last scan was, so to avoid getting a
        // data set with a questionable timestamp, just request a new
        // scan
        wifiManager.startScan();

        // Throw an icon in the notification bar so the user knows we're running)
        Notification notification = new Notification.Builder(this)
            .setContentTitle("WiFiDroid")
            .setContentText("Scanner currently active")
            .setSmallIcon(R.drawable.recording)
            .build();

        startForeground(1, notification);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(scanResultReceiver);
        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * Receiver for when new ScanResults are made available by the OS.
     * Converts the data from the OS ScanResult to our domain model AccessPoint
     */
    private BroadcastReceiver scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long updateTime = System.currentTimeMillis();
            updateTimes.add(updateTime);

            List<ScanResult> results = wifiManager.getScanResults();
            for (ScanResult result : results) {
                AccessPoint ap = AccessPoint.getInstance(result);
                AccessPointDataPoint dp = new AccessPointDataPoint(result.level, null);

                if (!data.containsKey(ap))
                    data.put(ap, new ArrayList<Pair<Long, AccessPointDataPoint>>());

                data.get(ap).add(new Pair<>(updateTime, dp));
            }

            if (updateDelay > 0) {
                updateTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        requestUpdate();
                    }
                }, updateDelay);
            } else if (updateDelay == 0) {
                requestUpdate();
            }

            sendBroadcast(new Intent(SCAN_RESULTS_AVAILABLE_ACTION));
        }
    };

    /**
     * Returns the time at which the DataService received its first update
     * @return
     */
    public long getFirstUpdateTimeInMillis() { return updateTimes.get(0); }

    /**
     * Returns the time at which the DataService received its last update
     * @return
     */
    public long getLastUpdateTimeInMillis() { return updateTimes.get(updateTimes.size()-1); }

    public List<Long> getUpdateTimesInMillis() { return Collections.unmodifiableList(updateTimes); }

    public Map<AccessPoint, List<Pair<Long, AccessPointDataPoint>>> getData() { return Collections.unmodifiableMap(data); }

    /**
     * Clears the data buffer and requests an update
     */
    public void clearData() {
        data.clear();
        updateTimes.clear();

        requestUpdate();
    }

    /**
     * Get the most recent data point for the given access point, if it exists.
     * @param ap
     * @return The data point if it exists, null if it doesn't
     */
    public Pair<Long, AccessPointDataPoint> getLatestResult(AccessPoint ap) {
        List<Pair<Long, AccessPointDataPoint>> dpList = data.get(ap);

        if (dpList == null)
            return null;

        return dpList.get(dpList.size() - 1);
    }

    /**
     * Returns all recorded data for the given access point
     * @param ap
     * @return the history if it exists, null if we have not observed the given ap
     */
    public List<Pair<Long, AccessPointDataPoint>> getHistory(AccessPoint ap) {
        return data.get(ap);
    }

    /**
     * Returns only the entries in the dataset that were seen on the last update
     * @return Always a map, possibly empty
     */
    public Map<AccessPoint, AccessPointDataPoint> getLatestResults() {
        Map<AccessPoint, AccessPointDataPoint> map = new HashMap<>();

        long lastUpdate = getLastUpdateTimeInMillis();

        // Really missing C# var about now. Damn you Java! Get with the goddamned program!
        // Hell, I miss LINQ. Functional makes this hella easy!
        for (Map.Entry<AccessPoint, List<Pair<Long, AccessPointDataPoint>>> entry : data.entrySet()) {
            Pair<Long, AccessPointDataPoint> dp = entry.getValue().get(entry.getValue().size() - 1);

            if (dp.first == lastUpdate)
                map.put(entry.getKey(), dp.second);
        }

        return map;
    }

    /**
     * Returns the latest data value for all access points in the dataset, regardless
     * of when we last saw them
     * @return Always a map, possibly empty
     */
    public Map<AccessPoint, Pair<Long, AccessPointDataPoint>> getAggregatedResults() {
        Map<AccessPoint, Pair<Long, AccessPointDataPoint>> map = new HashMap<>();

        for (Map.Entry<AccessPoint, List<Pair<Long, AccessPointDataPoint>>> entry : data.entrySet()) {
            Pair<Long, AccessPointDataPoint> dp = entry.getValue().get(entry.getValue().size() - 1);
            map.put(entry.getKey(), dp);
        }

        return map;
    }

    public boolean isWifiEnabled() {
        return wifiManager.isWifiEnabled();
    }

    /**
     * Instructs the DataService to attempt to acquire another datapoint
     * @return true if the user should expect an update
     */
    public boolean requestUpdate() {
        return isWifiEnabled() && wifiManager.startScan();
    }

    /**
     * Instructs the DataService to automatically update on the given interval.
     * The behaviour changes based on the value of delay; a value less than zero indicates
     * not to auto-update, a value of zero will request a new update as soon as one is received,
     * and a value greater than zero will have a scheduled request for an update.
     * @param delay How many milliseconds to wait before requesting the next update
     */
    public void setUpdateDelay(long delay) {
        // New value of less than 0 indicates we should stop updates
        updateTimer.purge();
        updateDelay = delay;
        requestUpdate();
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
