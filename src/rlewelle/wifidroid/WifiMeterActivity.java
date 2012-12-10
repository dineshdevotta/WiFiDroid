package rlewelle.wifidroid;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import rlewelle.wifidroid.data.AccessPoint;

public class WifiMeterActivity extends Activity {
    public static final String EXTRA_BSSID = "rlewelle.wifidroid.wifimeteractivity.BSSID";

    private String BSSID;
    private WifiMeterHolder holder;
    private DataService service;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_meter);
        holder = new WifiMeterHolder();

        BSSID = getIntent().getStringExtra(EXTRA_BSSID);

        registerReceiver(scanResultsReceived, new IntentFilter(DataService.SCAN_RESULTS_AVAILABLE_ACTION));
        bindService(new Intent(this, DataService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(scanResultsReceived);
        unbindService(serviceConnection);

        super.onDestroy();
    }

    public void displayLatestResults() {
        AccessPoint ap = service.getAccessPointStatus(BSSID);
        if (ap == null) return;
        holder.hydrate(ap);
    }

    private BroadcastReceiver scanResultsReceived = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            displayLatestResults();
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            service = ((DataService.DataServiceBinder) binder).getService();
            displayLatestResults();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(WifiMeterActivity.this, "The Wifi DataService was unexpectedly shutdown!", Toast.LENGTH_LONG);
            service = null;
        }
    };

    private class WifiMeterHolder {
        public TextView BSSID;
        public TextView SSID;

        public WifiMeterHolder() {
            BSSID = (TextView) findViewById(R.id.network_bssid);
            SSID  = (TextView) findViewById(R.id.network_ssid);
        }

        public void hydrate(AccessPoint ap) {
            BSSID.setText(ap.BSSID);
            SSID.setText(ap.SSID);
        }
    }
}