package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.KeyEvent;

public class Elements {
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

        public TextFields() {
            assetValueTracker.setEditable(false);
            assetValueTracker.setHorizontalAlignment(SwingConstants.LEFT);
            assetValuePortfolio.setEditable(false);
            assetValuePortfolio.setHorizontalAlignment(SwingConstants.LEFT);
            addTrackerSymbol.setToolTipText("Separate multiple symbols with a comma ','");
        }
    }

    public class Labels {
        public JLabel addPortfolioSymbol = new JLabel("Symbol");
        public JLabel addPortfolioCount = new JLabel("Count");
    }

    public class ComboBoxs {
        public String[] VALUES = {"BTC", "USD"};
        public JComboBox assetValueTracker   = new JComboBox(VALUES);
        public JComboBox assetValuePortfolio = new JComboBox(VALUES);
    }

    public class Tables {
        public final String[] COLUMNS_TRACKER = {"Tracker", "Price/BTC", "Price/USD"};
        public final String[] COLUMNS_PORTFOLIO = {"Symbol", "Quantity", "Price/BTC", "Value/BTC", "Price/USD", "Value/USD"};
        public DefaultTableModel modelTrackers = new DefaultTableModel(COLUMNS_TRACKER, 0);
        public DefaultTableModel modelPortfolio = new DefaultTableModel(COLUMNS_PORTFOLIO, 0);
        public JTable trackers = new JTable(modelTrackers);
        public JTable portfolio = new JTable(modelPortfolio);
    }

    public class Frames {
        public JFrame mainFrame = new JFrame("Simple Coin Tracker : ");

        public Frames() {
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setSize(550, 650);
            mainFrame.setResizable(false);
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
    }

    public class Panels {
        JPanel addTracker = new JPanel();
        JPanel setRefresh = new JPanel();
        JPanel assetValueTracker = new JPanel();
        JPanel trackersHeader = new JPanel();
        JPanel trackersTable = new JPanel();
        JPanel trackers = new JPanel();

        JPanel addPortfolio = new JPanel();
        JPanel assetValuePortfolio = new JPanel();
        JPanel portfolioHeader = new JPanel();
        JPanel portfolioTable = new JPanel();
        JPanel portfolio = new JPanel();

        Panels() {
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
            trackersHeader.add(addTracker);
            trackersHeader.add(setRefresh);
            trackersHeader.add(assetValueTracker);
            trackersTable.setBorder(BorderFactory.createTitledBorder("Trackers"));
            trackersTable.add(scrollPanes.trackers);
            trackers.add(trackersHeader);
            trackers.add(trackersTable);

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
            portfolioHeader.add(addPortfolio);
            portfolioHeader.add(assetValuePortfolio);
            portfolioTable.setBorder(BorderFactory.createTitledBorder("Assets"));
            portfolioTable.add(scrollPanes.portfolio);
            portfolio.add(portfolioHeader);
            portfolio.add(portfolioTable);
        }
    }

    public class Tabs {
        JTabbedPane mainTabs = new JTabbedPane();

        Tabs() {
            mainTabs.add("Trackers", panels.trackers);
            mainTabs.add("Portfolio", panels.portfolio);
        }
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
}
