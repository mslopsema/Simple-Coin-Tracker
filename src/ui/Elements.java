package ui;

import api.CryptoCompare;
import utils.Files;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Elements {
    public static final String[] UNITS = {"BTC", "USD"};
    public double[] ASSET_SUM = new double[UNITS.length]; // {BTC, USD}
    public int refreshRate = 10; // s

    public class Buttons {
        public JButton addTrackerSymbol   = new JButton("ADD");
        public JButton setRefreshRate     = new JButton("SET");
        public JButton addPortfolioSymbol = new JButton("ADD");
    }

    public class TextFields {
        public JTextField addTrackerSymbol    = new JTextField("BTC", 5);
        public JTextField setRefreshRate      = new JTextField("10", 5);

        public JTextField addPortfolioSymbol  = new JTextField("BTC", 5);
        public JTextField addPortfolioCount   = new JTextField("1", 5);

        public JTextField assetValueTracker   = new JTextField("0", 8);
        public JTextField assetValuePortfolio = new JTextField("0", 8);

        public JTextField assetValueChangRawBtc = new JTextField("0"); // 8
        public JTextField assetValueChangRawUsd = new JTextField("0"); // 8
        public JTextField assetValueChangePctBtc = new JTextField("0");// 5
        public JTextField assetValueChangePctUsd = new JTextField("0");// 5

        public TextFields() {
            assetValueTracker.setEditable(false);
            //assetValueTracker.setHorizontalAlignment(SwingConstants.LEFT);
            assetValuePortfolio.setEditable(false);
            //assetValuePortfolio.setHorizontalAlignment(SwingConstants.LEFT);
            addTrackerSymbol.setToolTipText("Separate multiple symbols with a comma ','");
            assetValueChangRawBtc.setEditable(false);
            assetValueChangRawUsd.setEditable(false);
            assetValueChangePctBtc.setEditable(false);
            assetValueChangePctUsd.setEditable(false);
        }
    }

    public class Labels {
        public JLabel addPortfolioSymbol = new JLabel("Symbol");
        public JLabel addPortfolioCount = new JLabel("Count");
        public JLabel assetValueChangRawBtc = new JLabel("BTC");
        public JLabel assetValueChangRawUsd = new JLabel("USD");
        public JLabel assetValueChangePctBtc = new JLabel("Δ BTC");
        public JLabel assetValueChangePctUsd = new JLabel("Δ USD");
    }

    public class ComboBoxs {
        public JComboBox assetValueTracker   = new JComboBox(UNITS);
        public JComboBox assetValuePortfolio = new JComboBox(UNITS);
    }

    public class Tables {
        public final String[] COLUMNS_TRACKER = {"Tracker", "Price/BTC", "1day Δ BTC", "Price/USD", "1day Δ USD"};
        public final String[] COLUMNS_PORTFOLIO = {"Symbol", "Quantity", "Price/BTC", "Value/BTC", "1day Δ BTC", "Price/USD", "Value/USD", "1day Δ USD"};
        public CustomTableModel modelTrackers = new CustomTableModel(COLUMNS_TRACKER, 0);
        public CustomTableModel modelPortfolio = new CustomTableModel(COLUMNS_PORTFOLIO, 0);
        public JTable trackers = new JTable(modelTrackers);
        public JTable portfolio = new JTable(modelPortfolio);

        public Tables() {
            modelTrackers.isCellEditable(1, 1);
        }
    }

    public class Frames {
        public JFrame mainFrame = new JFrame("Simple Coin Tracker");

        public Frames() {
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(550, 700);
            mainFrame.setJMenuBar(menus.mainMenuBar);
            mainFrame.getContentPane().add(tabs.mainTabs);
        }
    }

    public class Menus {
        public JMenuBar mainMenuBar = new JMenuBar();
        public JMenu fileMenu = new JMenu("File");
        public JMenuItem openItem = new JMenuItem("Open");
        public JMenuItem saveItem = new JMenuItem("Save");
        public JMenuItem clearItem = new JMenuItem("Clear");

        public Menus() {
            fileMenu.setMnemonic(KeyEvent.VK_F);
            openItem.setMnemonic(KeyEvent.VK_O);
            saveItem.setMnemonic(KeyEvent.VK_S);
            clearItem.setMnemonic(KeyEvent.VK_C);
            fileMenu.add(openItem);
            fileMenu.add(saveItem);
            fileMenu.add(clearItem);
            mainMenuBar.add(fileMenu);
        }
    }

    public class ScrollPanes {
        JScrollPane trackers = new JScrollPane(tables.trackers);
        JScrollPane portfolio = new JScrollPane(tables.portfolio);

        public ScrollPanes() {
        }
    }

    public class Panels {
        JPanel addTracker = new JPanel();
        JPanel setRefresh = new JPanel();
        JPanel assetValueTracker = new JPanel();
        JPanel trackersTable = new JPanel(new GridLayout());
        JPanel trackers = new JPanel();

        JPanel addPortfolio = new JPanel();
        JPanel assetValuePortfolio = new JPanel();
        JPanel assetValueChange = new JPanel();
        JPanel portfolioTable = new JPanel(new GridLayout());
        JPanel portfolio = new JPanel();

        Panels() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            gbc.weightx = 1.0;

            // Trackers Tab
            addTracker.setBorder(BorderFactory.createTitledBorder("Add Tracker"));
            addTracker.add(textFields.addTrackerSymbol);
            addTracker.add(buttons.addTrackerSymbol);
            setRefresh.setBorder(BorderFactory.createTitledBorder("Refresh Rate (s)"));
            setRefresh.add(textFields.setRefreshRate);
            setRefresh.add(buttons.setRefreshRate);
            assetValueTracker.setBorder(BorderFactory.createTitledBorder("Estimated Value"));
            assetValueTracker.add(textFields.assetValueTracker);
            assetValueTracker.add(comboBoxes.assetValueTracker);
            trackersTable.setBorder(BorderFactory.createTitledBorder("Trackers"));
            trackersTable.add(scrollPanes.trackers);

            trackers.setLayout(new GridBagLayout());
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            trackers.add(addTracker, gbc);
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            trackers.add(setRefresh, gbc);
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            trackers.add(assetValueTracker, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 3;
            trackers.add(trackersTable, gbc);

            // Portfolio Tab
            addPortfolio.setBorder(BorderFactory.createTitledBorder("Add Asset"));
            addPortfolio.add(labels.addPortfolioSymbol);
            addPortfolio.add(textFields.addPortfolioSymbol);
            addPortfolio.add(labels.addPortfolioCount);
            addPortfolio.add(textFields.addPortfolioCount);
            addPortfolio.add(buttons.addPortfolioSymbol);

            assetValuePortfolio.setBorder(BorderFactory.createTitledBorder("Estimated Value"));
            assetValuePortfolio.add(textFields.assetValuePortfolio);
            assetValuePortfolio.add(comboBoxes.assetValuePortfolio);

            assetValueChange.setBorder(BorderFactory.createTitledBorder("24hr Portfolio Changes"));
            assetValueChange.add(textFields.assetValueChangRawBtc);
            assetValueChange.add(labels.assetValueChangRawBtc);
            assetValueChange.add(textFields.assetValueChangRawUsd);
            assetValueChange.add(labels.assetValueChangRawUsd);
            assetValueChange.add(textFields.assetValueChangePctBtc);
            assetValueChange.add(labels.assetValueChangePctBtc);
            assetValueChange.add(textFields.assetValueChangePctUsd);
            assetValueChange.add(labels.assetValueChangePctUsd);

            portfolioTable.setBorder(BorderFactory.createTitledBorder("Assets"));
            portfolioTable.add(scrollPanes.portfolio);

            portfolio.setLayout(new GridBagLayout());
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            portfolio.add(addPortfolio, gbc);
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            portfolio.add(assetValuePortfolio, gbc);
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 3;
            portfolio.add(assetValueChange, gbc);
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 3;
            portfolio.add(portfolioTable, gbc);
        }
    }

    public class Tabs {
        public JTabbedPane mainTabs = new JTabbedPane();

        Tabs() {
            mainTabs.add("Trackers", panels.trackers);
            mainTabs.add("Portfolio", panels.portfolio);
        }
    }

    public class Graphs {

    }

    public Buttons buttons = new Buttons();
    public TextFields textFields = new TextFields();
    public Labels labels = new Labels();
    public ComboBoxs comboBoxes = new ComboBoxs();
    public Tables tables = new Tables();
    public Menus menus = new Menus();
    public ScrollPanes scrollPanes = new ScrollPanes();
    public Panels panels = new Panels();
    public Tabs tabs = new Tabs();
    public Frames frames = new Frames();

    public Elements(CryptoCompare api) {
        addActions(api);
    }

    private void addActions(CryptoCompare api) {
        // Menus
        menus.openItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                loadConfig();
            }
        });
        menus.saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                HashMap<String, String> assetMap = new HashMap<String, String>();
                for (String s : tables.modelPortfolio.keySet()) {
                    String count = (String) tables.modelPortfolio.getValueAt(s, 1);
                    assetMap.put(s, count);
                }
                Files.saveConfig(tables.modelTrackers.keySet(), assetMap);
            }
        });
        menus.clearItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                tables.modelTrackers.clear();
                tables.modelPortfolio.clear();
                updateAssetTotal(0, 0, true);
            }
        });

        // Trackers Tab
        ActionListener addAL = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String[] str = textFields.addTrackerSymbol.getText().split(",");
                for (String st : str) {
                    String s = st.toUpperCase();
                    if (tables.modelTrackers.contains(s) || !api.containsSymbol(s)) continue;
                    tables.modelTrackers.addRow(new String[]{s});
                }
                textFields.addTrackerSymbol.setText("");
            }
        };
        textFields.addTrackerSymbol.addActionListener(addAL);
        buttons.addTrackerSymbol.addActionListener(addAL);

        ActionListener comboAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateAssetTotal(0, 0, false);
            }
        };
        comboBoxes.assetValueTracker.addActionListener(comboAction);
        comboBoxes.assetValuePortfolio.addActionListener(comboAction);

        ActionListener refreshAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int rate = Integer.parseInt(textFields.setRefreshRate.getText());
                if (rate < 1) rate = 1;
                System.out.println("Set Rate : " + rate + "s");
                refreshRate = rate;
            }
        };
        textFields.setRefreshRate.addActionListener(refreshAction);
        buttons.setRefreshRate.addActionListener(refreshAction);

        // Portfolio Tab
        ActionListener assetAL = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String s = textFields.addPortfolioSymbol.getText().toUpperCase();
                if (tables.modelPortfolio.contains(s) || !api.containsSymbol(s)) return;
                String asset_count = textFields.addPortfolioCount.getText();
                tables.modelPortfolio.addRow(new String[]{s, asset_count});
                textFields.addPortfolioSymbol.setText("");
                textFields.addPortfolioCount.setText("");
            }
        };
        textFields.addPortfolioSymbol.addActionListener(assetAL);
        textFields.addPortfolioCount.addActionListener(assetAL);
        buttons.addPortfolioSymbol.addActionListener(assetAL);
    }

    public void loadConfig() {
        Set<String> t = new HashSet<String>();
        HashMap<String, String> a = new HashMap<String, String>();
        Files.loadConfig(t, a);

        for (String s : t) {
            if (tables.modelTrackers.contains(s)) continue;
            tables.modelTrackers.addRow(new String[]{s, "", ""});
        }

        for (String s : a.keySet()) {
            if (tables.modelPortfolio.contains(s)) continue;
            tables.modelPortfolio.addRow(new String[]{s, a.get(s)});
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
    public void updateAssetTotal(double BTC, double USD, boolean isNew) {
        MathContext mc = new MathContext(10, RoundingMode.HALF_DOWN);
        BTC = Double.parseDouble(new BigDecimal(BTC, mc).toPlainString());
        USD = Double.parseDouble(new BigDecimal(USD, mc).toPlainString());

        if (isNew) ASSET_SUM = new double[] {BTC, USD};
        textFields.assetValueTracker.setText(String.valueOf(
                ASSET_SUM[comboBoxes.assetValueTracker.getSelectedIndex()]));
        textFields.assetValuePortfolio.setText(String.valueOf(
                ASSET_SUM[comboBoxes.assetValuePortfolio.getSelectedIndex()]));
    }
}
