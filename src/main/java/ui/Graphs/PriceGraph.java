package ui.Graphs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;
import ui.Record;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class PriceGraph extends JPanel {

    public PriceGraph() {
        super();
        setLayout(new BorderLayout());
        setData(new ArrayList<Record>());
    }

    public void setData(ArrayList<Record> recordArrayList) {
        TimeSeriesCollection tsc = new TimeSeriesCollection();
        for (Record r : recordArrayList) tsc.addSeries(GraphModes.getInstance().build(r));
        removeAll();

        JFreeChart mChart = ChartFactory.createTimeSeriesChart(null, null, null,
                tsc, true, true, false);

        XYPlot plot = mChart.getXYPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("MM/dd HH:mm"));
        removeAll();
        add(new ChartPanel(mChart));
        updateUI();
    }
}
