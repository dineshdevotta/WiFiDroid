package rlewelle.wifidroid.graphs;

import android.content.Context;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import rlewelle.wifidroid.data.AccessPoint;
import rlewelle.wifidroid.data.AccessPointDataPoint;
import rlewelle.wifidroid.utils.ColorUtilities;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class GraphFactory {
    private GraphFactory() {}

    public static GraphicalView signalStrengthOverTime(
        Context context,
        AccessPoint ap,
        Map<Long, AccessPointDataPoint> dp,
        TreeSet<Long> pollTimes
    ) {
        HashMap<AccessPoint, Map<Long, AccessPointDataPoint>> data = new HashMap<>();
        data.put(ap, dp);

        return signalStrengthsOverTime(context, data, pollTimes);
    }

    public static GraphicalView signalStrengthsOverTime(
        Context context,
        Map<AccessPoint, Map<Long, AccessPointDataPoint>> data,
        TreeSet<Long> pollTimes
    ) {
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        XYMultipleSeriesRenderer dataRenderer = new XYMultipleSeriesRenderer();

        for (Map.Entry<AccessPoint, Map<Long, AccessPointDataPoint>> ap : data.entrySet()) {
            XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
            seriesRenderer.setPointStyle(PointStyle.CIRCLE);
            dataRenderer.addSeriesRenderer(seriesRenderer);

            XYSeries seriesData = new XYSeries(ap.getKey().getSSID());
            populateSeriesWithSignalStrengthOverTime(seriesData, ap.getValue(), pollTimes);
            dataSet.addSeries(seriesData);
        }

        dataRenderer.setYAxisMin(0.0);
        dataRenderer.setYAxisMax(100.0);

        return ChartFactory.getLineChartView(
            context,
            dataSet,
            dataRenderer
        );
    }

    public static GraphicalView signalChannels(
        Context context,
        Map<AccessPoint, Map.Entry<Long, AccessPointDataPoint>> data
    ) {
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        XYMultipleSeriesRenderer dataRenderer = new XYMultipleSeriesRenderer();
        dataRenderer.setBarSpacing(0.0);

        int i=0;
        for (Map.Entry<AccessPoint, Map.Entry<Long, AccessPointDataPoint>> ap : data.entrySet()) {
            XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
            dataRenderer.addSeriesRenderer(seriesRenderer);
            seriesRenderer.setColor(ColorUtilities.getColor(i++));

            XYSeries seriesData = new XYSeries(ap.getKey().getSSID());
            populateSeriesWithChannel(seriesData, ap.getKey(), ap.getValue().getValue());
            dataSet.addSeries(seriesData);
        }

        dataRenderer.setYAxisMin(0.0);
        dataRenderer.setYAxisMax(100.0);
        dataRenderer.setXAxisMin(0.0);
        dataRenderer.setXAxisMax(15.0);

        dataRenderer.setXTitle("Channel");

        for (i=0; i<15; ++i)
            dataRenderer.addXTextLabel(i, Integer.toString(i));

        return ChartFactory.getBarChartView(
            context,
            dataSet,
            dataRenderer,
            BarChart.Type.DEFAULT
        );
    }

    /**
     * Populates the given XYSeries with signal strength per time data
     * Time will be made relative to the first time in the pollTimes set;
     * Signal strength will be normalized
     * @param seriesData
     * @param data
     * @param pollTimes
     */
    public static void populateSeriesWithSignalStrengthOverTime(
        XYSeries seriesData,
        Map<Long, AccessPointDataPoint> data,
        TreeSet<Long> pollTimes
    ) {
        long firstTime = pollTimes.first();

        for (Long time : pollTimes) {
            double x = (time - firstTime) / 1000.0;

            AccessPointDataPoint dp = data.get(time);
            seriesData.add(
                x,
                dp == null ? 0.0 : 100.0f * dp.getNormalizedLevel()
            );
        }
    }

    public static void populateSeriesWithChannel(
        XYSeries seriesData,
        AccessPoint ap,
        AccessPointDataPoint dp
    ) {
        seriesData.add(ap.getChannel(), 100.0f * dp.getNormalizedLevel());
    }
}
