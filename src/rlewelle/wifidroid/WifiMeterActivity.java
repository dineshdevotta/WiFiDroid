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
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.data.AccessPointDataPoint;

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

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        XYSeries a = new XYSeries("A");
        a.add(1.0, 10.0);
        a.add(2.0, 20.0);
        a.add(3.0, 5.0);

        dataset.addSeries(a);

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.addSeriesRenderer(new SimpleSeriesRenderer());

        GraphicalView graph = ChartFactory.getBarChartView(
            this,
            dataset,
            renderer,
            BarChart.Type.DEFAULT
        );

        FrameLayout graphHost = (FrameLayout) findViewById(R.id.network_signal_graph_frame);
        graphHost.addView(graph);

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

        Pair<Long, AccessPointDataPoint> dp = serviceLink.getService().getLatestResult(ap);
        holder.hydrate(ap, dp.first, dp.second);
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