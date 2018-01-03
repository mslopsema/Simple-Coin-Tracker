import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.eclipsesource.json.JsonObject;

import api.CryptoCompare;
import ui.Elements;
import utils.Formatting;

public class CoinTracker {

    public static final String TITLE = "Simple Coin Tracker";
    private CryptoCompare api;
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
                e.loadConfig();
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
            Set<String> units = new HashSet<String>();
            for (String s : Elements.UNITS) units.add(s);

            while (!Thread.interrupted()) {
                long startTimeLoop = System.currentTimeMillis();
                Set<String> allKeys = new HashSet<String>();
                allKeys.addAll(e.tables.modelTrackers.keySet());
                allKeys.addAll(e.tables.modelPortfolio.keySet());

                JsonObject jo = api.getPrice(allKeys, units);
                if (jo == null) {
                    faults++;
                } else {
                    double sumUsd = 0;
                    double sumBtc = 0;
                    double sumUsdOld = 0;
                    double sumBtcOld = 0;
                    JsonObject rawObj = (JsonObject) jo.get("RAW");
                    for (String s : rawObj.names()) {
                        JsonObject obj = (JsonObject) rawObj.get(s);
                        if (obj == null) {
                            faults++;
                            continue;
                        }

                        double btcPrice  = ((JsonObject) obj.get("BTC")).getDouble("PRICE", 0);
                        double btcChange = ((JsonObject) obj.get("BTC")).getDouble("CHANGEPCT24HOUR", 0);
                        double usdPrice  = ((JsonObject) obj.get("USD")).getDouble("PRICE", 0);
                        double usdChange = ((JsonObject) obj.get("USD")).getDouble("CHANGEPCT24HOUR", 0);

                        if (e.tables.modelTrackers.contains(s)) {
                            e.tables.modelTrackers.setValueAt(btcPrice,  s, 1);
                            e.tables.modelTrackers.setValueAt(Formatting.signAndSize(btcChange, 8), s, 2);
                            e.tables.modelTrackers.setValueAt(usdPrice,  s, 3);
                            e.tables.modelTrackers.setValueAt(Formatting.signAndSize(btcChange, 8), s, 4);
                        }
                        if (e.tables.modelPortfolio.contains(s)) {
                            double count = Double.parseDouble((String) e.tables.modelPortfolio.getValueAt(s, 1));

                            btcChange /= 100;
                            usdChange /= 100;
                            double btcSum = btcPrice * count;
                            double usdSum = usdPrice * count;
                            sumBtc += btcSum;
                            sumUsd += usdSum;
                            double btcSumOld = btcSum / (btcChange + 1);
                            double usdSumOld = usdSum / (usdChange + 1);
                            double btcDiff = btcSum - btcSumOld;
                            double usdDiff = usdSum - usdSumOld;
                            sumBtcOld += btcSumOld;
                            sumUsdOld += usdSumOld;

                            e.tables.modelPortfolio.setValueAt(btcPrice, s, 2);
                            e.tables.modelPortfolio.setValueAt(btcSum,   s, 3);
                            e.tables.modelPortfolio.setValueAt(Formatting.signAndSize(btcDiff, 8),  s, 4);
                            e.tables.modelPortfolio.setValueAt(usdPrice, s, 5);
                            e.tables.modelPortfolio.setValueAt(usdSum,   s, 6);
                            e.tables.modelPortfolio.setValueAt(Formatting.signAndSize(usdDiff, 8),  s, 7);
                        }
                    }
                    DecimalFormat dfPct = new DecimalFormat("+0.00%;-0.00%");
                    e.textFields.assetValueChangePctBtc.setText(String.valueOf(dfPct.format(sumBtc / sumBtcOld - 1)));
                    e.textFields.assetValueChangePctUsd.setText(String.valueOf(dfPct.format(sumUsd / sumUsdOld - 1)));
                    e.textFields.assetValueChangRawBtc.setText(Formatting.signAndSize(sumBtc - sumBtcOld, 10));
                    e.textFields.assetValueChangRawUsd.setText(Formatting.signAndSize(sumUsd - sumUsdOld, 10));

                    e.updateAssetTotal(sumBtc, sumUsd, true);
                }
                System.out.println("Cycles : " + cycles++ + " Faults : " + faults +
                        " LoopTime : " + (System.currentTimeMillis() - startTimeLoop) +
                        " RunTime : "  + (System.currentTimeMillis() - startTimeThread));

                try {
                    Thread.sleep(e.refreshRate * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}