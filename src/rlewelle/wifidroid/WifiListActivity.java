package rlewelle.wifidroid;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import rlewelle.wifidroid.data.AccessPoint;

import java.util.*;

public class WifiListActivity extends ListActivity
{
    WifiManager wifi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        registerReceiver(
            new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Toast.makeText(WifiListActivity.this, "Got new data", Toast.LENGTH_SHORT).show();
                    displayData();
                }
            },

            new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        );

        displayData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.wifi_list_refresh:
                if (!checkWifiEnabled()) return false;
                if (!wifi.startScan()) {
                    // Not really sure what would cause this, or what to do about it
                    Log.d("WifiListActivity.wifi_list_refresh", "startScan() returned false!");
                }

                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    /**
     * Make sure that wifi is enabled, and if not, tell the user.
     * @return True if wifi is enabled.
     */
    public boolean checkWifiEnabled() {
        if (!wifi.isWifiEnabled()) {
            Toast.makeText(this, "Wifi is disabled!", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    // Grab and display the latest data from the WifiManager
    public void displayData() {
        if (!checkWifiEnabled()) return;

        List<AccessPoint> scanResults = WifiUtilities.accessPointsFromScanResults(wifi.getScanResults());
        if (scanResults == null) {
            Log.d("WifiListActivity.onCreate", "scanResults is NULL");
            return;
        }



        Collections.sort(scanResults, new Comparator<AccessPoint>() {
            @Override
            public int compare(AccessPoint a, AccessPoint b) {
                // Sort by signal strength (high->low)
                //return WifiManager.compareSignalLevel(b.level, a.level);

                // Sort by channel (low->high)
                return ((Integer)a.frequency).compareTo(b.frequency);

                // Sort by name (low->high)
                //return a.SSID.compareTo(b.SSID);
            }
        });

        setListAdapter(new NetworkListAdapter(scanResults));
    }

    public class NetworkListAdapter extends ArrayAdapter<AccessPoint> {
        public class NetworkListRowHolder {
            TextView ssid;
            TextView seen;
            TextView channel;
            ProgressBar strength;

            public NetworkListRowHolder(View view) {
                ssid = (TextView)view.findViewById(R.id.network_ssid);
                seen = (TextView)view.findViewById(R.id.network_lastseen);
                channel = (TextView)view.findViewById(R.id.network_channel);
                strength = (ProgressBar)view.findViewById(R.id.network_strength);
            }

            public void hydrate(AccessPoint scan) {
                ssid.setText(scan.SSID);

                // Assuming we're talking about a 2.4GHz WiFi source, channels are as follows:
                // Channel 1 - 2412
                // Channel 2 - 2417
                // Channel 3 - 2422
                // ...
                // Channel 13 - 2472
                // and then channel 14 messes everything up at
                // Channel 14 - 2484

                // There's also 5GHz channels, but I'm not worrying about those right now
                int channelNumber = WifiUtilities.convertFrequencyToChannel(scan.frequency);
                String channelStr = channelNumber == -1
                                  ? String.format("(%d Hz)", scan.frequency)
                                  : Integer.toString(channelNumber);

                channel.setText(channelStr);

                // ScanResult.timestamp is not the last time we saw this AP - rather, it is
                // the synchronized time function of that access point; each network maintains
                // this number such that it is consistant across devices on that network.
                seen.setText("Last seen n seconds ago");

                // The signal strength is measured in milli-watt decibels (relative to 1 milli-watt)
                // Generally, these values are negative and somewhere in the [-100, 0] range, with
                // values on the lower end representing bad signals and values near zero being good
                // signals.
                // Scan.level: [-100, -30]
                //   + 100     [0,     70]
                int maxLevel = 32;
                strength.setMax(maxLevel);
                strength.setProgress(WifiManager.calculateSignalLevel(scan.level, maxLevel));
            }
        }

        public NetworkListAdapter(List<AccessPoint> scanResults) {
            super(WifiListActivity.this, R.layout.wifi_list_row, R.id.network_ssid, scanResults);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NetworkListRowHolder holder;

            if (convertView == null || convertView.getTag() == null) {
                convertView = getLayoutInflater().inflate(R.layout.wifi_list_row, null);
                holder = new NetworkListRowHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (NetworkListRowHolder)convertView.getTag();
            }

            holder.hydrate(getItem(position));
            return convertView;
        }
    }
}
