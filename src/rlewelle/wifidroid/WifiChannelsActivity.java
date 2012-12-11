package rlewelle.wifidroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

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

    }
}