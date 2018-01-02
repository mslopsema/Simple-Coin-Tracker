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

public class CoinTracker  {

    public static final String TITLE = "Simple Coin Tracker";
    private CryptoCompare cc;
    private Thread httpThread;
    private HashMap<String, Integer> trackers = new HashMap<String, Integer>();
    private HashMap<String, Integer> assets = new HashMap<String, Integer>();
    private Elements e;
    private long start = System.currentTimeMillis();

    private int refreshRate = 10; // Seconds
    private double[] ASSET_SUM = {0, 0}; // {BTC, USD}

    public static void main(String[] args) {
        new CoinTracker();
    }
    
    /**
     * Constructor
     * 1. Build UI Elements
     * 2. Start HTTP Looping Thread
     */
    private CoinTracker() {
        System.out.println("Start CT");
        cc = new CryptoCompare();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                e = new Elements();
                e.frames.mainFrame.setVisible(true);
                setGuiListeners();
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
                    if (trackers.containsKey(s) || !cc.containsSymbol(s)) continue;
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
                if (!cc.containsSymbol(asset_sym)) return;
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

    private void updateStatus(long cycles) {
        long runtime = System.currentTimeMillis() - start;
        System.out.println(TITLE + " | Source : " +  cc.HOME + " Cycles : " + cycles + " Runtime : " + runtime + "ms");
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
        long cycles = 0;

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                Set<String> keysTrackers = trackers.keySet();
                System.out.println(keysTrackers.toString());
                if (keysTrackers.size() > 0) {
                    JsonObject jo = cc.getPrice(keysTrackers);
                    if (jo == null) continue;
                    System.out.println(jo.toString());
                    for (String s : keysTrackers) {
                        JsonObject obj = (JsonObject) jo.get(s);
                        if (obj == null) continue;
                        e.tables.trackers.getModel().setValueAt(obj.get("BTC"), trackers.get(s), 1);
                        e.tables.trackers.getModel().setValueAt(obj.get("USD"), trackers.get(s), 2);
                    }
                }
                Set<String> keysAssets = assets.keySet();
                System.out.println(keysAssets.toString());
                if (keysAssets.size() > 0) {
                    JsonObject jo = cc.getPrice(keysAssets);
                    if (jo == null) continue;
                    System.out.println(jo.toString());
                    
                    double sum_usd = 0;
                    double sum_btc = 0;
                    
                    for (String s : keysAssets) {
                        JsonObject obj = (JsonObject) jo.get(s);
                        if (obj == null) continue;
                        double usd_rate = obj.getDouble("USD", 0);
                        double btc_rate = obj.getDouble("BTC", 0);
                        double count = Double.parseDouble((String) e.tables.portfolio.getModel().
                                getValueAt(assets.get(s), 1));
                        double usd_val = usd_rate * count;
                        double btc_val = btc_rate * count;
                        e.tables.portfolio.getModel().setValueAt(btc_rate, assets.get(s), 2);
                        e.tables.portfolio.getModel().setValueAt(btc_val,  assets.get(s), 3);
                        e.tables.portfolio.getModel().setValueAt(usd_rate, assets.get(s), 4);
                        e.tables.portfolio.getModel().setValueAt(usd_val,  assets.get(s), 5);
                        sum_usd += usd_val;
                        sum_btc += btc_val;
                    }
                    updateAssetTotal(sum_btc, sum_usd, true);
                }
                
                
                try {
                    updateStatus(cycles++);
                    Thread.sleep(refreshRate * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}