package ui.TableModel;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import ui.Record;
import utils.Files;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public abstract class CustomTableModel extends AbstractTableModel {
    protected String[] mColumnNames;
    protected ArrayList<Record> mRecordList = new ArrayList<Record>();

    public int getRowCount() {
        return mRecordList.size();
    }

    public int getColumnCount() {
        return mColumnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return mColumnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? String.class : Double.class;
    }

    public abstract Object getValueAt(int row, int col);

    @Override
    public void setValueAt(Object obj, int row, int col) {
        if (col == 1) mRecordList.get(row).count = (Double) obj;
    }

    /**
     * Accessor for the underlying List<Record>.
     * Will return the Record associated with the ROW.
     * @param row The ROW
     * @return The Record from the list
     */
    public Record get(int row) {
        return mRecordList.get(row);
    }

    /**
     * Accessor for the underlying List<Record>.
     * Will return the Symbol associated with the ROW.
     * @param symbol The Symbol
     * @return The Record from the list
     */
    public Record get(String symbol) {
        for (Record r : mRecordList) if (symbol.equalsIgnoreCase(r.symbol)) return r;
        return null;
    }

    /**
     * Accessor for the underlying List.
     * Will return a HashSet<String> of the SYMBOLS stored in this table.
     * @return A set of the SYMBOLS.
     */
    public Set<String> keySet() {
        Set<String> keys = new HashSet<String>();
        for (Record r : mRecordList) keys.add(r.symbol);
        return keys;
    }

    public Iterator<Record> iterator() {
        return mRecordList.iterator();
    }

    /**
     * Accessor for the underlying Map<>().containsKey() method.
     * @param s String to check
     * @return If the Symbol exists
     */
    public boolean contains(String s) {
        return (get(s) != null);
    }

    /**
     * For clearing all data from this table and the underlying model.
     */
    public void clear() {
        mRecordList.clear();
    }

    /**
     * For adding a new row to the table.
     * @param r
     */
    public void addRow(Record r) {
        if (r.symbol == null || r.symbol.length() < 1 || contains(r.symbol)) return;
        mRecordList.add(r);
        fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
    }

    /**
     * For removing a row
     * @param row
     */
    public void removeRow(int row) {
        mRecordList.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * For swapping the position of 2 rows.
     * @param row1
     * @param row2
     */
    public void swapRows(int row1, int row2) {
        if (row1 < 0 || row1 >= mRecordList.size()) return;
        if (row2 < 0 || row2 >= mRecordList.size()) return;

        Record temp = mRecordList.get(row1);
        mRecordList.set(row1, mRecordList.get(row2));
        mRecordList.set(row2, temp);
        fireTableRowsUpdated(row1, row1);
        fireTableRowsUpdated(row2, row2);
    }

    /**
     * For storing the Record data into JSON formatting.
     * Only SYMBOL and COUNT is saved.
     * @return
     */
    public JsonArray toJsonArray() {
        JsonArray ja = new JsonArray();
        for (Record r : mRecordList) {
            JsonObject jo = new JsonObject();
            jo.add(Files.KEY_SYMBOL, r.symbol);
            jo.add(Files.KEY_COUNT, r.count);
            ja.add(jo);
        }
        return ja;
    }


}
