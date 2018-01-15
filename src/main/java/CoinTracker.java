
import javax.swing.SwingUtilities;

import api.ApiBase;
import api.sources.CryptoCompare;
import ui.Elements;

public class CoinTracker {

    public static final String TITLE = "Simple Coin Tracker";
    private HttpThread httpThread = new HttpThread();
    private HistThread histThread = new HistThread();
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                e = new Elements(api);
                e.frames.mainFrame.setTitle(TITLE + " : " + api.HOME);
                e.frames.mainFrame.setVisible(true);
            }
        });
        api.loadSymbols();
        httpThread.start();
        histThread.start();
    }

    private class HttpThread extends Thread {

        @Override
        public void run() {
            System.out.println("Start HttpThread");
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
            System.out.println("Start HistoryThread");
            while (!Thread.interrupted()) {
                try {
                    api.getHistory(e);
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