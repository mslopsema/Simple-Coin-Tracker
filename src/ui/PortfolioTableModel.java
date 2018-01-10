package ui;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PortfolioTableModel extends AbstractTableModel {
    private String[] columnNames = {"Symbol", "Quantity", "Price/BTC", "Value/BTC", "1day Δ BTC", "Price/USD", "Value/USD", "1day Δ USD"};
    private HashMap<String, Integer> symbolMap = new HashMap<>();
    private ArrayList<Record> list = new ArrayList<Record>();

    @Override
    public int getRowCount() {
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class : Double.class;
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0 : return list.get(row).symbol;
            case 1 : return list.get(row).count;
            case 2 : return list.get(row).priceBtc;
            case 3 : return list.get(row).valueBtc;
            case 4 : return list.get(row).deltaBtc;
            case 5 : return list.get(row).priceUsd;
            case 6 : return list.get(row).valueUsd;
            case 7 : return list.get(row).deltaUsd;
            default : return null;
        }
    }

    @Override
    public void setValueAt(Object obj, int row, int col) {
        if (col == 1) list.get(row).count = (Double) obj;
    }

    /**
     * Accessor for the underlying HashMap.
     * Will return the ROW associated with the SYMBOL.
     * @param s The Symbol
     * @return The corresponding row in the table.
     */
    public Record get(int row) {
        return list.get(row);
    }

    public Record get(String symbol) {
        return get(symbolMap.get(symbol));
    }

    /**
     * Accessor for the underlying HashMap.
     * Will return a HashSet<String> of the SYMBOLS stored in this table.
     * @return A set of the SYMBOLS.:
     */
    public Set<String> keySet() {
        return symbolMap.keySet();
    }

    /**
     * Accessor for the underlying Map<>().containsKey() method.
     * @param s String to check
     * @return If the Symbol exists
     */
    public boolean contains(String s) {
        return symbolMap.containsKey(s);
    }

    /**
     * For clearing all data from this table and the underlying model.
     */
    public void clear() {
        symbolMap.clear();
        list.clear();
    }

    /**
     * For adding a new row to the table.
     * The Zeroth index of the String array will be used as a new symbol in the map.
     * @param r
     */
    public void addRow(Record r) {
        if (r.symbol == null || r.symbol.length() < 1) return;
        symbolMap.put(r.symbol, getRowCount());
        list.add(r);
    }

    /**
     * For removing a row
     * @param row
     */
    public void removeRow(int row) {
        symbolMap.remove(list.get(row).symbol);
        list.remove(row);
        fireTableRowsDeleted(row, row);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == 1); // Only Count Column is editable
    }
}
