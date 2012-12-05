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
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

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
            TextView channel;
            ProgressBar strength;
            //TextView strengthTxt;

            public NetworkListRowHolder(View view) {
                ssid = (TextView)view.findViewById(R.id.network_ssid);
                seen = (TextView)view.findViewById(R.id.network_lastseen);
                channel = (TextView)view.findViewById(R.id.network_channel);
                strength = (ProgressBar)view.findViewById(R.id.network_strength);
                //strengthTxt = (TextView)view.findViewById(R.id.network_strength_txt);
            }

            public void hydrate(ScanResult scan) {
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
                String channelStr = String.format("(%d Hz)", scan.frequency);
                if (scan.frequency >= 2412 && scan.frequency <= 2472)
                    channelStr = Integer.toString((scan.frequency - 2407) / 5);
                channel.setText(channelStr);

                // ScanResult: time is given in microseconds
                // Calendar:   expects time in milliseconds
                //Calendar calendar = new GregorianCalendar();
                //calendar.setTimeInMillis(scan.timestamp / 1000);
                //seen.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime()));
                //seen.setText(Long.toString(scan.timestamp));
                seen.setText("Last seen n seconds ago");

                // The signal strength is measured in milli-watt decibels (relative to 1 milli-watt)
                // Generally, these values are negative and somewhere in the [-100, 0] range, with
                // values on the lower end representing bad signals and values near zero being good
                // signals.
                // Scan.level: [-100, -30]
                //   + 100     [0,     70]
                int maxLevel = 70;
                strength.setMax(maxLevel);
                strength.setProgress(Math.min(Math.max(0, 100 + scan.level), maxLevel) );
                //strengthTxt.setText(Integer.toString(scan.level));
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
