
import javax.swing.SwingUtilities;

import api.ApiBase;
import api.sources.CryptoCompare;
import org.jfree.data.time.TimeSeriesCollection;
import ui.Elements;
import ui.Graphs.GraphModes;
import ui.Record;

public class CoinTracker {

    public static final String TITLE = "Simple Coin Tracker";
    private ApiBase api;
    private Elements e;

    public static void main(String[] args) {
        new CoinTracker();
    }
    
    /**
     * Constructor
     * 1. Build UI Elements
     * 2. Start HTTP Looping Thread
     */
    private CoinTracker() {
        api = new CryptoCompare();
        new SymbolThread().start();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                e = new Elements(api);
                e.frames.mainFrame.setTitle(TITLE + " : " + api.HOME);
                e.frames.mainFrame.setVisible(true);
                new HttpThread().start();
                new HistThread().start();
            }
        });
    }

    private class SymbolThread extends Thread {

        @Override
        public void run() {
            System.out.println("Start SymbolThread " + Thread.currentThread().getId());
            api.loadSymbols();
        }
    }

    private class HttpThread extends Thread {

        @Override
        public void run() {
            System.out.println("Start HttpThread " + Thread.currentThread().getId());
            long startTimeThread = System.currentTimeMillis();
            long cycles = 0;
            int faults = 0;

            while (!Thread.interrupted()) {
                try {
                    long startTimeLoop = System.currentTimeMillis();
                    if (!api.updatePrice(e)) faults++;
                    e.logStatus("Cycles : " + cycles++ + " Faults : " + faults +
                            " LoopTime : " + (System.currentTimeMillis() - startTimeLoop) +
                            " RunTime : "  + (System.currentTimeMillis() - startTimeThread));
                    Thread.sleep(e.refreshRate * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class HistThread extends Thread {

        @Override
        public void run() {
            System.out.println("Start HistoryThread " + Thread.currentThread().getId());
            while (!Thread.interrupted()) {
                try {
                    api.getHistory(e.tables.modelPortfolio.getList());
                    e.graphs.portfolio.setData(e.tables.modelPortfolio.getList());

                    System.out.println("History Updated");
                    // 10x Slower than Table Update
                    Thread.sleep(e.refreshRate * 1000 * 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}