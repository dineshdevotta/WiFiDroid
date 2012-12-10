package rlewelle.wifidroid;

import android.app.ListActivity;
import android.app.Notification;
import android.content.*;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.utils.WifiUtilities;

import java.util.*;

public class WifiListActivity extends ListActivity implements DataService.IDataServicable{
    DataService.DataServiceLink serviceLink = new DataService.DataServiceLink(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceLink.onCreate();
    }

    @Override
    protected void onDestroy() {
        serviceLink.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // We may not have received the latest updates (via intent) while paused,
        // so just ask the DataService for the most recent updates
        if (serviceLink.getService() == null)
            return;

        displayLatestResults();
    }

    @Override
    public void onServiceConnected() {}

    @Override
    public void onServiceDisconnected() {}

    @Override
    public void onScanResultsReceived() {
        displayLatestResults();
    }

    @Override
    public Context getContext() {
        return this;
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
                serviceLink.getService().startScan();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        AccessPoint ap = (AccessPoint) getListView().getItemAtPosition(position);

        Intent intent = new Intent(this, WifiMeterActivity.class);
        intent.putExtra(WifiMeterActivity.EXTRA_BSSID, ap.BSSID);
        startActivity(intent);
    }

    private void displayLatestResults() {
        if (serviceLink.getService() == null)
            return;

        // Retrieve and flatten result set into a list for use with the adapter
        Set<AccessPoint> latestResults      = serviceLink.getService().getLatestResults();
        Set<AccessPoint> aggregatedResults  = serviceLink.getService().getAggregatedResults();
        if (latestResults == null || aggregatedResults == null) {
            Log.d("rlewelle.WifiListActivity.displayResults", "go NULL latest/aggregated result sets!");
            return;
        }

        /*
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
        */

        setListAdapter(new NetworkListAdapter(latestResults, aggregatedResults));
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

                if (NetworkListAdapter.this.inactiveAccessPoints.contains(scan)) {
                    ssid.setTextColor(Color.RED);
                }

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
                // this number such that it is consistent across devices on that network.
                seen.setText("Last seen " +
                    DateUtils.getRelativeDateTimeString(
                        WifiListActivity.this,
                        scan.lastSeen,
                        DateUtils.SECOND_IN_MILLIS,
                        DateUtils.DAY_IN_MILLIS,
                        0
                    )
                );

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

        private Set<AccessPoint> inactiveAccessPoints;

        public NetworkListAdapter(Set<AccessPoint> latestResult, Set<AccessPoint> aggregatedResult) {
            super(WifiListActivity.this, R.layout.wifi_list_row, R.id.network_ssid, new ArrayList<AccessPoint>(aggregatedResult));
            inactiveAccessPoints = new HashSet<AccessPoint>(aggregatedResult);
            inactiveAccessPoints.removeAll(latestResult);
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
