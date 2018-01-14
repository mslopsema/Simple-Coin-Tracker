package ui.TableModel;

import org.junit.Test;
import ui.Record;

import static org.junit.Assert.*;

public class CustomTableModelTest {

    CustomTableModel ctm = new CustomTableModel() {
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
    };

    @Test
    public void getRowCount() {
        int N = (int) (Math.random() * 50);
        for (int i = 0; i < N; i++) {
            ctm.addRow(new Record(String.valueOf(i)));
        }
        assertEquals(N, ctm.getRowCount());
    }

    @Test
    public void getColumnCount() {
        String[] testStringArray = {"1", "2", "3"};
        ctm.mColumnNames = testStringArray;
        assertEquals(testStringArray.length, ctm.getColumnCount());
    }

    @Test
    public void getColumnName() {
        String[] testStringArray = {"Symbol", "Quantity", "Price/BTC", "Value/BTC", "1day Δ BTC", "Price/USD", "Value/USD", "1day Δ USD"};
        ctm.mColumnNames = testStringArray;
        for (int i = 0; i < testStringArray.length; i++) {
            assertEquals(testStringArray[i], ctm.getColumnName(i));
        }
    }

    @Test
    public void setValueAt() {
        String[] testStringArray = {"Symbol", "Quantity", "Price/BTC", "Value/BTC", "1day Δ BTC", "Price/USD", "Value/USD", "1day Δ USD"};
        ctm.mColumnNames = testStringArray;
        for (int i = 0; i < testStringArray.length; i++) {
            Record r = new Record(String.valueOf(i));
            r.count = i;
            r.priceBtc = 2 * i;
            r.valueBtc = 3 * i;
            r.deltaBtc = 4 * i;
            r.priceUsd = 5 * i;
            r.valueUsd = 6 * i;
            r.deltaUsd = 7 * i;
            ctm.addRow(r);
        }
        for (int i = 0; i < testStringArray.length; i++) {
            assertEquals(String.valueOf(i), ctm.getValueAt(i, 0));
            for (int j = 0; j < 7; j++) {
                assertEquals((double) i * (j + 1), ctm.getValueAt(i, j + 1));
            }
        }
    }

    @Test
    public void get() {
        int N = (int) (Math.random() * 50);
        for (int i = 0; i < N; i++) {
            ctm.addRow(new Record(String.valueOf(i)));
        }
        for (int i = 0; i < N; i++) {
            // Get Symbol
            assertEquals(String.valueOf(i), ctm.get(String.valueOf(i)).symbol);
            // Get Row
            assertEquals(String.valueOf(i), ctm.get(i).symbol);
        }
    }

    @Test
    public void swapRows() {
        int N = 50;
        for (int i = 0; i < N; i++) {
            ctm.addRow(new Record(String.valueOf(i)));
        }

        ctm.swapRows(10, 20);
        assertEquals("20", ctm.get(10).symbol);
        assertEquals("10", ctm.get(20).symbol);

        ctm.swapRows(10, 20);
        ctm.swapRows(-1, 10); // Should be out of bounds
        ctm.swapRows(99, 10); // Should be out of bounds

        for (int i = 0; i < N; i++) {
            assertEquals(String.valueOf(i), ctm.get(i).symbol);
        }
    }

    @Test
    public void toJsonArray() {

    }
}