
import javax.swing.SwingUtilities;

import api.ApiBase;
import api.sources.CryptoCompare;
import ui.Elements;

public class CoinTracker {

    public static final String TITLE = "Simple Coin Tracker";
    private ApiBase api;
    private Thread httpThread;
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
        restartHttpThread();
    }

    private void restartHttpThread() {
        if (httpThread != null) {
            httpThread.interrupt();
            httpThread = null;
        }
        httpThread = new Thread(new HttpThread());
        httpThread.start();
    }

    private class HttpThread implements Runnable {

        @Override
        public void run() {
            api.loadSymbols();
            long startTimeThread = System.currentTimeMillis();
            long cycles = 0;
            int faults = 0;

            while (!Thread.interrupted()) {
                try {
                    long startTimeLoop = System.currentTimeMillis();
                    if (!api.updatePrice(e)) faults++;
                    System.out.println("Cycles : " + cycles++ + " Faults : " + faults +
                            " LoopTime : " + (System.currentTimeMillis() - startTimeLoop) +
                            " RunTime : "  + (System.currentTimeMillis() - startTimeThread));
                    Thread.sleep(e.refreshRate * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}