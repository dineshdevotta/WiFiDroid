package rlewelle.wifidroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.achartengine.GraphicalView;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.data.AccessPointDataPoint;
import rlewelle.wifidroid.graphs.GraphFactory;

import java.util.Map;

public class WifiMeterActivity extends Activity implements DataService.IDataServicable{
    public static final String EXTRA_AP = "rlewelle.wifidroid.wifimeteractivity.EXTRA_AP";

    private DataService.DataServiceLink serviceLink = new DataService.DataServiceLink(this);

    private AccessPoint ap;

    private WifiMeterHolder holder;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_meter);
        holder = new WifiMeterHolder();
        serviceLink.onCreate();

        ap = getIntent().getParcelableExtra(EXTRA_AP);
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
    public void onServiceConnected() {
        displayLatestResults();
    }

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

    public void displayLatestResults() {
        if (ap == null)
            return;

        // Latest result
        Map.Entry<Long, AccessPointDataPoint> latestDp = serviceLink.getService().getLatestResult(ap);
        holder.hydrate(ap, latestDp.getKey(), latestDp.getValue());

        // Time-series data
        Map<Long, AccessPointDataPoint> data = serviceLink.getService().getHistory(ap);

        GraphicalView graph = GraphFactory.signalStrengthOverTime(this, ap, data, serviceLink.getService().getUpdateTimesInMillis());

        FrameLayout graphHost = (FrameLayout) findViewById(R.id.network_signal_graph_frame);
        graphHost.removeAllViews();
        graphHost.addView(graph);
    }

    private class WifiMeterHolder {
        public TextView BSSID;
        public TextView SSID;

        public WifiMeterHolder() {
            BSSID = (TextView) findViewById(R.id.network_bssid);
            SSID  = (TextView) findViewById(R.id.network_ssid);
        }

        public void hydrate(AccessPoint ap, Long lastSeen, AccessPointDataPoint dp) {
            BSSID.setText(ap.getBSSID());
            SSID.setText(ap.getSSID());
        }
    }
}