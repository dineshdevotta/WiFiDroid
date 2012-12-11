package rlewelle.wifidroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;
import org.achartengine.GraphicalView;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.data.AccessPointDataPoint;
import rlewelle.wifidroid.graphs.GraphFactory;

import java.util.Map;

public class WifiChannelsActivity extends Activity implements DataService.IDataServicable {
    private DataService.DataServiceLink serviceLink = new DataService.DataServiceLink(this);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_channels);
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
    public Context getContext() { return this; }

    @Override
    public void onScanResultsReceived() { displayLatestResults(); }

    @Override
    public void onServiceConnected() { displayLatestResults(); }

    @Override
    public void onServiceDisconnected() {}

    private void displayLatestResults() {
        // Latest result
        Map<AccessPoint, Map.Entry<Long, AccessPointDataPoint>> aggregateData = serviceLink.getService().getAggregatedResults();

        GraphicalView graph = GraphFactory.signalChannels(this, aggregateData);

        FrameLayout graphHost = (FrameLayout) findViewById(R.id.network_channels_graph_frame);
        graphHost.removeAllViews();
        graphHost.addView(graph);
    }
}