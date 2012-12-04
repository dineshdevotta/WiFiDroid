package rlewelle.wifidroid;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class MainActivity extends ListActivity
{
    WifiManager wifi;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        List<ScanResult> scanResults = wifi.getScanResults();
        if (scanResults == null) {
            Log.d("MainActivity.onCreate", "scanResults is NULL");
            return;
        }

        NetworkListAdapter scanAdapter = new NetworkListAdapter(scanResults);
        setListAdapter(scanAdapter);
    }

    public class NetworkListAdapter extends ArrayAdapter<ScanResult> {
        public class NetworkListRowHolder {
            TextView ssid;
            TextView seen;
            TextView strength;
            TextView channel;

            public NetworkListRowHolder(View view) {
                ssid = (TextView)view.findViewById(R.id.network_ssid);
                seen = (TextView)view.findViewById(R.id.network_lastseen);
                strength = (TextView)view.findViewById(R.id.network_strength);
                channel = (TextView)view.findViewById(R.id.network_channel);
            }

            public void hydrate(ScanResult scan) {
                // Assuming we're talking about a 2.4GHz WiFi source, channels are as follows:
                // Channel 1 - 2412
                // Channel 2 - 2417
                // Channel 3 - 2422
                // ...
                // Channel 13 - 2472
                // and then channel 14 messes everything up at
                // Channel 14 - 2484

                // There's also 5GHz channels, but I'm not worrying about those right now
                String channelStr = String.format("(%d Hz)", scan.frequency);
                if (scan.frequency >= 2412 && scan.frequency <= 2472)
                    channelStr = Integer.toString((scan.frequency - 2407) / 5);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(scan.timestamp);

                ssid.setText(scan.SSID);
                seen.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
                strength.setText(Integer.toString(scan.level));
                channel.setText(channelStr);
            }
        }

        public NetworkListAdapter(List<ScanResult> scanResults) {
            super(MainActivity.this, R.layout.wifi_list_row, R.id.network_ssid, scanResults);
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
