import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.eclipsesource.json.JsonObject;

import api.CryptoCompare;
import ui.Elements;
import utils.Formatting;

public class CoinTracker  {

    public static final String TITLE = "Simple Coin Tracker";
    private CryptoCompare api;
    private Thread httpThread;
    private HashMap<String, Integer> trackers = new HashMap<String, Integer>();
    private HashMap<String, Integer> assets = new HashMap<String, Integer>();
    private Elements e;

    private int refreshRate = 10; // Seconds
    private double[] ASSET_SUM = new double[Elements.UNITS.length]; // {BTC, USD}

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
                e = new Elements();
                e.frames.mainFrame.setTitle(TITLE + " : " + api.HOME);
                e.frames.mainFrame.setVisible(true);
                setGuiListeners();
                loadConfig();
            }
        });
        restartHttpThread();
    }
    
    /**
     * Drawing UI Elements
     */
    private void setGuiListeners() {
        // Menus
        e.menus.openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loadConfig();
            }
        });
        e.menus.saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                HashMap<String, String> assetMap = new HashMap<String, String>();
                for (String s : assets.keySet()) {
                    String count = (String) e.tables.portfolio.getValueAt(assets.get(s), 1);
                    assetMap.put(s, count);
                }
                utils.Files.saveConfig(trackers.keySet(), assetMap);
            }
        });
        e.menus.clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                e.tables.modelTrackers.setRowCount(0);
                e.tables.modelPortfolio.setRowCount(0);
                trackers.clear();
                assets.clear();
                updateAssetTotal(0, 0, true);
            }
        });

        // Trackers Tab
        ActionListener addAL = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String[] str = e.textFields.addTrackerSymbol.getText().split(",");
                for (String st : str) {
                    String s = st.toUpperCase();
                    if (trackers.containsKey(s) || !api.containsSymbol(s)) continue;
                    trackers.put(s, e.tables.trackers.getRowCount());
                    e.tables.modelTrackers.addRow(new String[]{s, "", ""});
                }
                e.textFields.addTrackerSymbol.setText("");
            }
        };
        e.textFields.addTrackerSymbol.addActionListener(addAL);
        e.buttons.addTrackerSymbol.addActionListener(addAL);

        ActionListener comboAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateAssetTotal(0, 0, false);
            }
        };
        e.comboBoxes.assetValueTracker.addActionListener(comboAction);
        e.comboBoxes.assetValuePortfolio.addActionListener(comboAction);

        ActionListener refreshAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int rate = Integer.parseInt(e.textFields.setRefreshRate.getText());
                if (rate < 1) rate = 1;
                System.out.println("Set Rate : " + rate + "s");
                refreshRate = rate;
            }
        };
        e.textFields.setRefreshRate.addActionListener(refreshAction);
        e.buttons.setRefreshRate.addActionListener(refreshAction);

        // Portfolio Tab
        ActionListener assetAL = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String asset_sym = e.textFields.addPortfolioSymbol.getText().toUpperCase();
                if (!api.containsSymbol(asset_sym)) return;
                String asset_count = e.textFields.addPortfolioCount.getText();
                assets.put(asset_sym, e.tables.portfolio.getRowCount());
                e.tables.modelPortfolio.addRow(new String[]{asset_sym, asset_count});
                e.textFields.addPortfolioSymbol.setText("");
                e.textFields.addPortfolioCount.setText("");
            }
        };
        e.textFields.addPortfolioSymbol.addActionListener(assetAL);
        e.textFields.addPortfolioCount.addActionListener(assetAL);
        e.buttons.addPortfolioSymbol.addActionListener(assetAL);
    }

    private void loadConfig() {
        Set<String> t = new HashSet<String>();
        HashMap<String, String> a = new HashMap<String, String>();
        utils.Files.loadConfig(t, a);

        for (String s : t) {
            if (trackers.containsKey(s)) continue;
            trackers.put(s, e.tables.trackers.getRowCount());
            e.tables.modelTrackers.addRow(new String[]{s, "", ""});
        }

        for (String s : a.keySet()) {
            if (assets.containsKey(s)) continue;
            assets.put(s, e.tables.portfolio.getRowCount());
            e.tables.modelPortfolio.addRow(new String[]{s, a.get(s)});
        }
    }

    /**
     * For updating the Estimated Value of the portfolio.
     * There is 2 states {BTC, USD} - Based on Currency Units.
     * There is 2 fields which will be populated based on the Combo Box state.
     * Will restrict to 10 decimal places.
     * @param BTC Bitcoin Value
     * @param USD US Dollar Value
     * @param isNew To denote if the values are being changed, or if simply the units configuration is changing
     */
    private void updateAssetTotal(double BTC, double USD, boolean isNew) {
        MathContext mc = new MathContext(10, RoundingMode.HALF_DOWN);
        BTC = Double.parseDouble(new BigDecimal(BTC, mc).toPlainString());
        USD = Double.parseDouble(new BigDecimal(USD, mc).toPlainString());

        if (isNew) ASSET_SUM = new double[] {BTC, USD};
        e.textFields.assetValueTracker.setText(String.valueOf(
                ASSET_SUM[e.comboBoxes.assetValueTracker.getSelectedIndex()]));
        e.textFields.assetValuePortfolio.setText(String.valueOf(
                ASSET_SUM[e.comboBoxes.assetValuePortfolio.getSelectedIndex()]));
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
                allKeys.addAll(trackers.keySet());
                allKeys.addAll(assets.keySet());

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

                        double btcPrice = ((JsonObject) obj.get("BTC")).getDouble("PRICE", 0);
                        double btcChange = ((JsonObject) obj.get("BTC")).getDouble("CHANGEPCT24HOUR", 0);
                        double usdPrice = ((JsonObject) obj.get("USD")).getDouble("PRICE", 0);
                        double usdChange = ((JsonObject) obj.get("USD")).getDouble("CHANGEPCT24HOUR", 0);


                        if (trackers.containsKey(s)) {
                            e.tables.trackers.getModel().setValueAt(btcPrice, trackers.get(s), 1);
                            e.tables.trackers.getModel().setValueAt(btcChange, trackers.get(s), 2);
                            e.tables.trackers.getModel().setValueAt(usdPrice, trackers.get(s), 3);
                            e.tables.trackers.getModel().setValueAt(usdChange, trackers.get(s), 4);
                        }
                        if (assets.containsKey(s)) {
                            double count = Double.parseDouble((String) e.tables.portfolio.getModel().
                                    getValueAt(assets.get(s), 1));

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

                            e.tables.portfolio.getModel().setValueAt(btcPrice, assets.get(s), 2);
                            e.tables.portfolio.getModel().setValueAt(btcSum,  assets.get(s), 3);
                            e.tables.portfolio.getModel().setValueAt(btcDiff, assets.get(s), 4);
                            e.tables.portfolio.getModel().setValueAt(usdPrice, assets.get(s), 5);
                            e.tables.portfolio.getModel().setValueAt(usdSum,  assets.get(s), 6);
                            e.tables.portfolio.getModel().setValueAt(usdDiff, assets.get(s), 7);
                        }
                    }
                    DecimalFormat dfPct = new DecimalFormat("+0.00%;-0.00%");
                    e.textFields.assetValueChangePctBtc.setText(String.valueOf(dfPct.format(sumBtc / sumBtcOld - 1)));
                    e.textFields.assetValueChangePctUsd.setText(String.valueOf(dfPct.format(sumUsd / sumUsdOld - 1)));
                    e.textFields.assetValueChangRawBtc.setText(Formatting.signAndSize(sumBtc - sumBtcOld, 8));
                    e.textFields.assetValueChangRawUsd.setText(Formatting.signAndSize(sumUsd - sumUsdOld, 8));

                    updateAssetTotal(sumBtc, sumUsd, true);
                }
                System.out.println("Cycles : " + cycles++ + " Faults : " + faults +
                        " LoopTime : " + (System.currentTimeMillis() - startTimeLoop) +
                        " RunTime : "  + (System.currentTimeMillis() - startTimeThread));

                try {
                    Thread.sleep(refreshRate * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}