package ui.TableModel;

public class Tracker extends CustomTableModel {

    public Tracker() {
        mColumnNames = new String[]{"Tracker",
                "Price/BTC", "1day Δ% BTC",
                "Price/ETH", "1day Δ% ETH",
                "Price/USD", "1day Δ% USD"};
    }

    @Override
    public Object getValueAt(int row, int col) {
        switch (col) {
            case 0 : return mRecordList.get(row).symbol;
            case 1 : return mRecordList.get(row).priceBtc;
            case 2 : return mRecordList.get(row).deltaBtc;
            case 3 : return mRecordList.get(row).priceEth;
            case 4 : return mRecordList.get(row).deltaEth;
            case 5 : return mRecordList.get(row).priceUsd;
            case 6 : return mRecordList.get(row).deltaUsd;
            default : return null;
        }
    }
}
