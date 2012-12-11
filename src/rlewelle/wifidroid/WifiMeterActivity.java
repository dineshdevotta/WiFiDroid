package rlewelle.wifidroid;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Pair;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.data.AccessPointDataPoint;

import java.util.List;

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
        Pair<Long, AccessPointDataPoint> latestDp = serviceLink.getService().getLatestResult(ap);
        holder.hydrate(ap, latestDp.first, latestDp.second);

        // Time-series data
        List<Pair<Long, AccessPointDataPoint>> data = serviceLink.getService().getHistory(ap);

        XYSeries a = new XYSeries("Signal Strength (dB)");
        XYSeriesRenderer r = new XYSeriesRenderer();

        long firstTime = data.get(0).first;
        for (Pair<Long, AccessPointDataPoint> dp : data) {
            a.add((dp.first - firstTime)/1000.0, 100.0f * dp.second.getNormalizedLevel());
        }

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        dataset.addSeries(a);
        renderer.addSeriesRenderer(r);
        renderer.setYAxisMin(0.0);
        renderer.setYAxisMax(100.0);

        GraphicalView graph = ChartFactory.getLineChartView(
            this,
            dataset,
            renderer
        );



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