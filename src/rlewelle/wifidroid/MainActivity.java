package rlewelle.wifidroid;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.List;

public class MainActivity extends ListActivity
{
    WifiManager wifi;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.main);

        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Log.d("MainActivity.onCreate", wifi.toString());

        List<ScanResult> scanResults = wifi.getScanResults();

        if (scanResults == null) {
            Log.d("MainActivity.onCreate", "scanResults is NULL");
            return;
        }

        Log.d("MainActivity.onCreate", scanResults.toString());
        for (ScanResult result : scanResults) {
            Log.d("MainActivity.onCreate", result.toString());
        }

        ArrayAdapter<ScanResult> scanAdapter = new ArrayAdapter<ScanResult>(
                this,
                android.R.layout.simple_list_item_1,
                scanResults
        );

        setListAdapter(scanAdapter);
    }
}
