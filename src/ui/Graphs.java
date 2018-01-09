package ui;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.util.Date;

public class Graphs extends JPanel {
    public Graphs(JsonArray data, String fromSymbol, String toSymbol) {
        TimeSeries ts = new TimeSeries(fromSymbol + " / " + toSymbol);
        System.out.println(data.toString());
        for (JsonValue jv : data) {
            JsonObject jo = jv.asObject();
            System.out.println(fromSymbol + " -> " + toSymbol + " : " + jo.getLong("time", 0) +
                    " / " + jo.getDouble("close", 0));
            Date d = new Date(jo.getLong("time", 1514935800));
            ts.add(new Minute(d), jo.getDouble("close", 0.0));
        }
        TimeSeriesCollection tsc = new TimeSeriesCollection();
        tsc.addSeries(ts);

        //XYDataset ds1 = createDataset("Series 1", 100, new Minute(), 200);
        //XYDataset ds2 = createDataset("Series 2", 999, new Minute(), 200);

        JFreeChart jfc = ChartFactory.createTimeSeriesChart(
                "Chart Title",
                "Time Axis Label",
                "Value Access Label",
                tsc, true, true, false);

        XYPlot xyp = (XYPlot) jfc.getPlot();

        xyp.setOrientation(PlotOrientation.VERTICAL);

        // Range Axis 2
        //NumberAxis na2 = new NumberAxis("BTC/USD Price");
        //xyp.setRangeAxis(1, na2);
        //xyp.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);

        //xyp.setDataset(1, ds2);
        //xyp.mapDatasetToDomainAxis(1, 1);
        //xyp.mapDatasetToRangeAxis(1, 1);

        xyp.setRenderer(1, new XYLineAndShapeRenderer(true, false));
        ChartUtils.applyCurrentTheme(jfc);

        ChartPanel cp = new ChartPanel(jfc);

        super.add(cp);
    }

    private static XYDataset createDataset(String name, double base, RegularTimePeriod start, int count) {
        TimeSeries ts = new TimeSeries(name);
        for (int i = 0; i < count; i++) {
            ts.add(start, base);
            start = start.next();
            base *= (Math.random() / 10 + 1);
        }
        TimeSeriesCollection tsc = new TimeSeriesCollection();
        tsc.addSeries(ts);
        return tsc;
    }
}
