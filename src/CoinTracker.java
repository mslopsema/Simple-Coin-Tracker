import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import com.eclipsesource.json.JsonObject;

import api.CryptoCompare;

public class CoinTracker  {
    
    private CryptoCompare cc;
    private Thread httpThread;
    private HashMap<String, Integer> trackers = new HashMap<String, Integer>();
    private HashMap<String, Integer> assets = new HashMap<String, Integer>();
    private int refreshRate = 10; // Seconds
    
    private double SUM_USD = 0;
    private double SUM_BTC = 0;
    
    private JButton btn_addSymbol = new JButton("ADD");
    private JButton btn_setRefresh = new JButton("SET");
    private JButton btn_addPortfolio = new JButton("ADD");
    private JTextField tf_addSymbol = new JTextField("BTC", 5);
    private JTextField tf_setRefresh = new JTextField("10", 5);
    private JTextField tf_addSymbolPortfolio = new JTextField("BTC", 5);
    private JTextField tf_addQuantityPortfolio = new JTextField("1", 5);
    private JTextField tf_portfolioValue = new JTextField("0", 8);
    private JTextField tf_portfolioValue2 = new JTextField("0", 8);
    private JComboBox cmbo_PortfolioValue = new JComboBox(new String[]{"BTC", "USD"});
    private JComboBox cmbo_PortfolioValue2 = new JComboBox(new String[]{"BTC", "USD"});
    private String[] columns = {"Tracker", "Price/BTC", "Price/USD"};
    private DefaultTableModel dtm = new DefaultTableModel(columns, 0);
    private JTable tbl_trackers = new JTable(dtm);
    private String[] columns_portfolio = {"Symbol", "Quantity", "Price/BTC", "Value/BTC", "Price/USD", "Value/USD"};
    private DefaultTableModel dtm_portfolio = new DefaultTableModel(columns_portfolio, 0);
    private JTable tbl_portfolio = new JTable(dtm_portfolio);
    
    public static void main(String[] args) throws IOException, InterruptedException {
        new CoinTracker();
    }
    
    /**
     * Constructor
     * 1. Build UI Elements
     * 2. Start HTTP Looping Thread
     */
    CoinTracker() {
        System.out.println("Start CT");
        cc = new CryptoCompare();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() { startGUI(); }
        });
        restartHttpThread();
    }
    
    /**
     * Drawing UI Elements
     */
    private void startGUI() {
        JFrame mainFrame = new JFrame("Coin Tracker");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(500, 600);
        //mainFrame.setMinimumSize(new Dimension(500, 700));
        //mainFrame.setMaximumSize(new Dimension(500, 700));
        mainFrame.setResizable(false);
        
        ActionListener addAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String[] str = tf_addSymbol.getText().split(",");
                tf_addSymbol.setText("");
                for (String st : str) {
                    String s = st.toUpperCase();
                    if (trackers.containsKey(s)) continue;
                    trackers.put(s, tbl_trackers.getRowCount());
                    dtm.addRow(new String[]{s, "", ""});
                }
            }
        };
        
        ActionListener comboAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tf_portfolioValue.setText(String.valueOf(((String) cmbo_PortfolioValue
                        .getSelectedItem()).equals("BTC") ? SUM_BTC : SUM_USD));
                tf_portfolioValue2.setText(String.valueOf(((String) cmbo_PortfolioValue2
                        .getSelectedItem()).equals("BTC") ? SUM_BTC : SUM_USD));
            }
        };
        
        tf_addSymbol.setToolTipText("Seperate multiple symbols with comma ','");
        tf_addSymbol.addActionListener(addAL);
        btn_addSymbol.addActionListener(addAL);
        JPanel addSymbolPanel = new JPanel();
        addSymbolPanel.setBorder(BorderFactory.createTitledBorder("Add Tracker"));
        addSymbolPanel.add(tf_addSymbol);
        addSymbolPanel.add(btn_addSymbol);
        
        
        btn_setRefresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int rate = Integer.parseInt(tf_setRefresh.getText());
                if (rate < 1) rate = 1;
                System.out.println("Rate : " + rate + "s");
                refreshRate = rate;
                //restartHttpThread();
            }
        });
        JPanel setRefreshPanel = new JPanel();
        setRefreshPanel.setBorder(BorderFactory.createTitledBorder("Refresh Rate (s)"));
        setRefreshPanel.add(tf_setRefresh);
        setRefreshPanel.add(btn_setRefresh);
        
        JPanel valuePanel = new JPanel();
        valuePanel.setBorder(BorderFactory.createTitledBorder("Estimated Value"));
        tf_portfolioValue.setEditable(false);
        valuePanel.add(tf_portfolioValue);
        cmbo_PortfolioValue.addActionListener(comboAction);
        valuePanel.add(cmbo_PortfolioValue);
        
        JPanel top = new JPanel();
        top.add(addSymbolPanel);
        top.add(setRefreshPanel);
        top.add(valuePanel);
        
        JPanel trackerPanel = new JPanel();
        trackerPanel.setBorder(BorderFactory.createTitledBorder("Trackers"));
        trackerPanel.add(new JScrollPane(tbl_trackers));

        JPanel panel1 = new JPanel();
        panel1.add(top, BorderLayout.NORTH);
        panel1.add(trackerPanel);
        
        JTabbedPane jtp = new JTabbedPane();
        jtp.add("Trackers", panel1);

        /////////////////
        
        ActionListener assetAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String asset_sym = tf_addSymbolPortfolio.getText().toUpperCase();
                String asset_count = tf_addQuantityPortfolio.getText();
                assets.put(asset_sym, tbl_portfolio.getRowCount());
                dtm_portfolio.addRow(new String[]{asset_sym, asset_count, "", ""});
                tf_addSymbolPortfolio.setText("");
                tf_addQuantityPortfolio.setText("");
            }
        };
        
        JPanel addPortfolioPanel = new JPanel();
        addPortfolioPanel.setBorder(BorderFactory.createTitledBorder("Add Asset"));
        tf_addSymbolPortfolio.addActionListener(assetAL);
        tf_addQuantityPortfolio.addActionListener(assetAL);
        btn_addPortfolio.addActionListener(assetAL);
        addPortfolioPanel.add(new JLabel("Symbol"));
        addPortfolioPanel.add(tf_addSymbolPortfolio);
        addPortfolioPanel.add(new JLabel("Quantity"));
        addPortfolioPanel.add(tf_addQuantityPortfolio);
        addPortfolioPanel.add(btn_addPortfolio);
        
        JPanel valuePanel2 = new JPanel();
        valuePanel2.setBorder(BorderFactory.createTitledBorder("Estimated Value"));
        tf_portfolioValue2.setEditable(false);
        valuePanel2.add(tf_portfolioValue2);
        cmbo_PortfolioValue2.addActionListener(comboAction);
        valuePanel2.add(cmbo_PortfolioValue2);
        
        JPanel top2 = new JPanel();
        top2.add(addPortfolioPanel);
        top2.add(valuePanel2);
        
        JPanel portfolioPanel = new JPanel();
        portfolioPanel.setBorder(BorderFactory.createTitledBorder("Assets"));
        portfolioPanel.add(new JScrollPane(tbl_portfolio));
        
        JPanel panel2 = new JPanel();
        panel2.add(top2);
        panel2.add(portfolioPanel);
        jtp.add("Portfolio", panel2);

        //Display the window.
        //frame.pack();
        mainFrame.getContentPane().add(jtp);
        mainFrame.setVisible(true);
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
                        tbl_trackers.getModel().setValueAt(obj.get("BTC"), trackers.get(s), 1);
                        tbl_trackers.getModel().setValueAt(obj.get("USD"), trackers.get(s), 2);
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
                        double count = Double.parseDouble((String) tbl_portfolio.getModel().
                                getValueAt(assets.get(s), 1));
                        double usd_val = usd_rate * count;
                        double btc_val = btc_rate * count;
                        tbl_portfolio.getModel().setValueAt(btc_rate, assets.get(s), 2);
                        tbl_portfolio.getModel().setValueAt(btc_val,  assets.get(s), 3);
                        tbl_portfolio.getModel().setValueAt(usd_rate, assets.get(s), 4);
                        tbl_portfolio.getModel().setValueAt(usd_val,  assets.get(s), 5);
                        sum_usd += usd_val;
                        sum_btc += btc_val;
                    }
                    SUM_BTC = sum_btc;
                    SUM_USD = sum_usd;
                    tf_portfolioValue.setText(String.valueOf(((String) cmbo_PortfolioValue
                            .getSelectedItem()).equals("BTC") ? SUM_BTC : SUM_USD));
                    tf_portfolioValue2.setText(String.valueOf(((String) cmbo_PortfolioValue2
                            .getSelectedItem()).equals("BTC") ? SUM_BTC : SUM_USD));
                }
                
                
                try {
                    Thread.sleep(refreshRate * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}