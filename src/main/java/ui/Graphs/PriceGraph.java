package ui.Graphs;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;

public class PriceGraph extends JPanel {

    public PriceGraph() {
        super();
        setLayout(new BorderLayout());
        setData(new TimeSeriesCollection());
    }

    public void setData(XYDataset data) {
        JFreeChart mChart = ChartFactory.createTimeSeriesChart(null, null, null,
                data, true, true, false);

        XYPlot plot = mChart.getXYPlot();
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yy.MM.dd HH:mm"));
        removeAll();
        add(new ChartPanel(mChart));
        updateUI();
    }
}
