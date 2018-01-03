package ui;

import utils.Formatting;

import javax.swing.table.DefaultTableModel;
import java.util.HashMap;
import java.util.Set;

public class CustomTableModel extends DefaultTableModel {
    private HashMap<String, Integer> symbolMap = new HashMap<>();

    public CustomTableModel(String[] columns, int defaultRowCount) {
        super(columns, defaultRowCount);
    }

    public int getSymbolIndex(String s) {
        return symbolMap.get(s);
    }

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

    public void setChangeValueAt(double value, String key, int col) {
        super.setValueAt(Formatting.signAndSize(value, 8), symbolMap.get(key), col);

    }

    /**
     * For setting the value at a given cell.
     * Using the Symbol in lieu of the row number.
     * @param key Symbol for which to get the row.
     * @param col Column index
     * @return Object contained in the given cell.
     */
    public void setValueAt(Object aValue, String key, int col) {
        super.setValueAt(aValue, symbolMap.get(key), col);
    }

    /**
     * For retrieving the value at a given cell.
     * Using the Symbol in lieu of the row number.
     * @param key Symbol for which to get the row.
     * @param col Column index
     * @return Object contained in the given cell.
     */
    public Object getValueAt(String key, int col) {
        return super.getValueAt(symbolMap.get(key), col);
    }

    /**
     * For clearing all data from this table and the underlying model.
     */
    public void clear() {
        symbolMap.clear();
        super.setRowCount(0);
    }

    /**
     * For adding a new row to the table.
     * The Zeroth index of the String array will be used as a new symbol in the map.
     * @param rowData
     */
    public void addRow(String[] rowData) {
        symbolMap.put(rowData[0], getRowCount());
        super.addRow(rowData);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false; // Table is read-only
    }
}
