package ui.TableModel;

public class Portfolio extends CustomTableModel {

    public Portfolio() {
        mColumnNames = new String[]{"Symbol", "Quantity", "Price/BTC", "Value/BTC", "1day Δ BTC", "Price/USD", "Value/USD", "1day Δ USD"};
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0 : return mRecordList.get(row).symbol;
            case 1 : return mRecordList.get(row).count;
            case 2 : return mRecordList.get(row).priceBtc;
            case 3 : return mRecordList.get(row).valueBtc;
            case 4 : return mRecordList.get(row).deltaBtc;
            case 5 : return mRecordList.get(row).priceUsd;
            case 6 : return mRecordList.get(row).valueUsd;
            case 7 : return mRecordList.get(row).deltaUsd;
            default : return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == 1); // Only Count Column is editable
    }
}
