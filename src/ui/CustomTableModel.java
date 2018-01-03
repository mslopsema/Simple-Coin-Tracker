package ui;

import javax.swing.table.DefaultTableModel;

public class CustomTableModel extends DefaultTableModel {

    public CustomTableModel(String[] columns, int defaultRowCount) {
        super(columns, defaultRowCount);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false; // Table is read-only
    }
}
