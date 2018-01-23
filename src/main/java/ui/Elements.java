package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;

import api.ApiBase;
import com.eclipsesource.json.JsonObject;
import ui.Graphs.PriceGraph;
import ui.TableModel.CustomTableModel;
import ui.TableModel.Portfolio;
import ui.TableModel.Tracker;
import utils.Files;

public class Elements {
    static final String[] UNITS = {"BTC", "ETH", "USD"};
    double[] ASSET_SUM = new double[UNITS.length];
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
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

        public JTextField assetValueChangRawBtc = new JTextField("0", 9);
        public JTextField assetValueChangRawUsd = new JTextField("0", 9);
        public JTextField assetValueChangePctBtc = new JTextField("0", 6);
        public JTextField assetValueChangePctUsd = new JTextField("0", 6);

        JTextField status = new JTextField();

        TextFields() {
            addTrackerSymbol.setToolTipText("Separate multiple symbols with a comma ','");
            assetValueTracker.setEditable(false);
            assetValuePortfolio.setEditable(false);
            assetValueChangRawBtc.setEditable(false);
            assetValueChangRawUsd.setEditable(false);
            assetValueChangePctBtc.setEditable(false);
            assetValueChangePctUsd.setEditable(false);
            status.setEditable(false);
        }
    }

    public class Labels {
        public final JLabel addPortfolioSymbol = new JLabel("Symbol");
        public final JLabel addPortfolioCount = new JLabel("Count");
        public final JLabel assetValueChangRawBtc = new JLabel("BTC");
        public final JLabel assetValueChangRawUsd = new JLabel("USD");
        public final JLabel assetValueChangePctBtc = new JLabel("Δ BTC");
        public final JLabel assetValueChangePctUsd = new JLabel("Δ USD");
    }

    public class ComboBoxes {
        public JComboBox assetValueTracker   = new JComboBox(UNITS);
        public JComboBox assetValuePortfolio = new JComboBox(UNITS);

        ActionListener comboAction = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                updateAssetTotal(0, 0, 0, false);
            }
        };

        ComboBoxes() {
            assetValueTracker.addActionListener(comboAction);
            assetValuePortfolio.addActionListener(comboAction);
        }
    }

    public class Tables {
        public CustomTableModel modelTrackers = new Tracker();
        public CustomTableModel modelPortfolio = new Portfolio();
        public JTable trackers = new JTable(modelTrackers);
        public JTable portfolio = new JTable(modelPortfolio);

        KeyAdapter ka = new KeyAdapter() {
            public void keyPressed(KeyEvent ke) {
                if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
                    JTable table = (JTable) ke.getSource();
                    if (table.getSelectedRow() < 0) return;
                    deleteRow(table, table.getSelectedRow());
                }

                if (ke.isControlDown()) {
                    if (ke.getKeyCode() == KeyEvent.VK_UP) {
                        JTable table = (JTable) ke.getSource();
                        if (table.getSelectedRow() < 0) return;
                        int row = table.getSelectedRow();
                        swapRows(table, row, row - 1);
                        table.setRowSelectionInterval(row - 1, row - 1);
                    } else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
                        JTable table = (JTable) ke.getSource();
                        if (table.getSelectedRow() < 0) return;
                        int row = table.getSelectedRow();
                        swapRows(table, row, row + 1);
                        table.setRowSelectionInterval(row + 1, row + 1);
                    }
                }
            }
        };

        Tables() {
            trackers.setAutoCreateRowSorter(true);
            portfolio.setAutoCreateRowSorter(true);
            trackers.getTableHeader().setReorderingAllowed(false);
            portfolio.getTableHeader().setReorderingAllowed(false);
            trackers.addKeyListener(ka);
            portfolio.addKeyListener(ka);
        }

        void deleteRow(JTable table, int row) {
            CustomTableModel ctm = (CustomTableModel) table.getModel();
            logStatus("Delete Row : " + row + " : " + ctm.get(row).symbol);
            ctm.removeRow(row);
        }

        void swapRows(JTable table, int row1, int row2) {
            CustomTableModel ctm = (CustomTableModel) table.getModel();
            logStatus("Swap Rows : " + row1 + " " + ctm.get(row1).symbol +
                                  " : " + row2 + " " + ctm.get(row2).symbol);
            ctm.swapRows(row1, row2);
        }
    }

    public class Frames {
        public JFrame mainFrame = new JFrame("Simple Coin Tracker");

        public Frames() {
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setMinimumSize(new Dimension(550, 700));
            mainFrame.setJMenuBar(menus.mainMenuBar);
            mainFrame.getContentPane().add(panels.main);
        }
    }

    public class Menus {
        public JMenuBar mainMenuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem clearItem = new JMenuItem("Clear");

        ActionListener alOpen = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                tables.modelTrackers.clear();
                tables.modelPortfolio.clear();
                loadConfig();
            }
        };

        ActionListener alSave = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JsonObject root = new JsonObject();
                root.add(Files.KEY_ASSETS, tables.modelPortfolio.toJsonArray());
                root.add(Files.KEY_TRACKERS, tables.modelTrackers.toJsonArray());
                Files.saveConfig(root);
                logStatus("Save Config");
            }
        };

        ActionListener alClear = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                tables.modelTrackers.clear();
                tables.modelPortfolio.clear();
                updateAssetTotal(0, 0, 0, true);
                logStatus("Clear Config");
            }
        };

        KeyStroke keySave = KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK);
        KeyStroke keyOpen = KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK);

        public Menus() {
            openItem.setMnemonic(KeyEvent.VK_O);
            openItem.addActionListener(alOpen);
            openItem.setAccelerator(keyOpen);

            saveItem.setMnemonic(KeyEvent.VK_S);
            saveItem.addActionListener(alSave);
            saveItem.setAccelerator(keySave);

            clearItem.addActionListener(alClear);

            fileMenu.setMnemonic(KeyEvent.VK_F);
            fileMenu.add(openItem);
            fileMenu.add(saveItem);
            fileMenu.add(clearItem);
            mainMenuBar.add(fileMenu);

            // By default, open the saved configuration when the program is loaded.
            loadConfig();
        }

        public void loadConfig() {
            JsonObject jo = Files.loadConfig().asObject();
            tables.modelPortfolio.fromJsonArray(jo.get(Files.KEY_ASSETS).asArray());
            tables.modelTrackers.fromJsonArray(jo.get(Files.KEY_TRACKERS).asArray());
            logStatus("Open Config");
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

        JPanel main = new JPanel(new GridLayout());
        JPanel status = new JPanel();

        Panels() {
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 1;
            gbc.weighty = 1;

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
            trackersTable.add(new JScrollPane(tables.trackers), BorderLayout.CENTER);

            trackers.setLayout(new GridBagLayout());
            gbc.weightx = 1;
            gbc.weighty = 1;
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
            gbc.weighty = 1000;
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
            portfolioTable.add(new JScrollPane(tables.portfolio), BorderLayout.CENTER);

            portfolio.setLayout(new GridBagLayout());
            gbc.weightx = 1;
            gbc.weighty = 1;
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
            gbc.weighty = 1000;
            portfolio.add(portfolioTable, gbc);
            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 3;
            gbc.weighty = 1000;
            portfolio.add(graphs.portfolio, gbc);

            main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
            status.setLayout(new BoxLayout(status, BoxLayout.X_AXIS));
            status.add(textFields.status);
            status.setMaximumSize(new Dimension(Integer.MAX_VALUE, 15));
        }
    }

    public class Tabs {
        public JTabbedPane mainTabs = new JTabbedPane();

        Tabs() {
            mainTabs.add("Trackers", panels.trackers);
            mainTabs.add("Portfolio", panels.portfolio);
            panels.main.add(mainTabs);
            panels.main.add(panels.status);
        }
    }

    public class Graphs {
        public PriceGraph portfolio = new PriceGraph();
    }

    public Buttons buttons = new Buttons();
    public TextFields textFields = new TextFields();
    public Labels labels = new Labels();
    public ComboBoxes comboBoxes = new ComboBoxes();
    public Tables tables = new Tables();
    public Menus menus = new Menus();
    public Graphs graphs = new Graphs();
    public Panels panels = new Panels();
    public Tabs tabs = new Tabs();
    public Frames frames = new Frames();

    public Elements(ApiBase api) {
        addComplexActions(api);
    }

    private void addComplexActions(final ApiBase api) {

        // Trackers Tab
        ActionListener alAddTracker = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String[] str = textFields.addTrackerSymbol.getText().split(",");
                for (String st : str) {
                    String s = st.toUpperCase();
                    if (tables.modelTrackers.contains(s) || !api.contains(s)) continue;
                    tables.modelTrackers.addRow(new Record(s));
                    logStatus("Add Tracker : " + s);
                }
                textFields.addTrackerSymbol.setText("");
            }
        };
        textFields.addTrackerSymbol.addActionListener(alAddTracker);
        buttons.addTrackerSymbol.addActionListener(alAddTracker);

        ActionListener alSetRefreshRate = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int rate = Integer.parseInt(textFields.setRefreshRate.getText());
                if (rate < 1) rate = 1;
                logStatus("Set Rate : " + rate + "s");
                refreshRate = rate;
            }
        };
        textFields.setRefreshRate.addActionListener(alSetRefreshRate);
        buttons.setRefreshRate.addActionListener(alSetRefreshRate);

        // Portfolio Tab
        ActionListener alAddAsset = new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String s = textFields.addPortfolioSymbol.getText().toUpperCase();
                if (!api.contains(s)) return;
                String count = textFields.addPortfolioCount.getText();
                if (tables.modelPortfolio.contains(s)) {
                    // Just update the count if the symbol is already in the table.
                    tables.modelPortfolio.get(s).count = Double.valueOf(count);
                } else {
                    // Add a new row in the table
                    tables.modelPortfolio.addRow(new Record(s, Double.valueOf(count)));
                }
                textFields.addPortfolioSymbol.setText("");
                textFields.addPortfolioCount.setText("");
                logStatus("Add Asset : " + s + " : " + count);
            }
        };
        textFields.addPortfolioSymbol.addActionListener(alAddAsset);
        textFields.addPortfolioCount.addActionListener(alAddAsset);
        buttons.addPortfolioSymbol.addActionListener(alAddAsset);
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
    public void updateAssetTotal(double BTC, double ETH, double USD, boolean isNew) {
        MathContext mc = new MathContext(10, RoundingMode.HALF_DOWN);
        BTC = Double.parseDouble(new BigDecimal(BTC, mc).toPlainString());
        ETH = Double.parseDouble(new BigDecimal(ETH, mc).toPlainString());
        USD = Double.parseDouble(new BigDecimal(USD, mc).toPlainString());

        if (isNew) ASSET_SUM = new double[] {BTC, ETH, USD};
        textFields.assetValueTracker.setText(String.valueOf(
                ASSET_SUM[comboBoxes.assetValueTracker.getSelectedIndex()]));
        textFields.assetValuePortfolio.setText(String.valueOf(
                ASSET_SUM[comboBoxes.assetValuePortfolio.getSelectedIndex()]));
    }

    /**
     * For updating the status bar.
     * The string will also be printed to the console.
     * @param s Log Status String
     */
    public void logStatus(String s) {
        String timeStamp = dateFormat.format(System.currentTimeMillis());
        String log = timeStamp + " : " + s;
        textFields.status.setText(log);
        System.out.println(log);
    }
}
