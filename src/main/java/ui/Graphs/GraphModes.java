package ui.Graphs;

import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import ui.Record;

import java.util.Date;

public class GraphModes {
    public static final int MODE_RAW_VALUE = 0;
    public static final int MODE_TOTAL_VALUE = 1;
    public static final int MODE_DAILY_CHANGE = 2;

    private static GraphModes mGraphModes;
    private int mMode = MODE_RAW_VALUE;

    public static GraphModes getInstance() {
        if (mGraphModes == null) mGraphModes = new GraphModes();
        return mGraphModes;
    }

    private GraphModes() {
    }

    public void setMode(int mode) {
        mMode = mode;
    }

    public int getMode() {
        return mMode;
    }

    public TimeSeries build(Record record) {
        TimeSeries ts = new TimeSeries(record.symbol);
        switch (mMode) {
            case MODE_RAW_VALUE :
                for (Record.history hist : record.histories) {
                    Date date = new Date(hist.time * 1000);
                    ts.addOrUpdate(new Minute(date), hist.price);
                }
                break;
            case MODE_TOTAL_VALUE :
                for (Record.history hist : record.histories) {
                    Date date = new Date(hist.time * 1000);
                    ts.addOrUpdate(new Minute(date), hist.price * record.count);
                }
                break;
            case MODE_DAILY_CHANGE :
                double first = record.histories.get(0).price;
                for (Record.history hist : record.histories) {
                    Date date = new Date(hist.time * 1000);
                    double percentage = hist.price / first;
                    ts.addOrUpdate(new Minute(date), percentage);
                }
                break;
        }
        return ts;
    }
}
