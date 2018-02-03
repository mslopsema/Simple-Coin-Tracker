package ui;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A Record represents 1 row in a table
 */
public class Record {

    public static class history {
        public long time;
        public double price;

        public history(long time, double price) {
            this.time = time;
            this.price = price;
        }
    }

    public String symbol;
    public double count;
    public double priceBtc;
    public double valueBtc;
    public double deltaBtc;
    public double priceEth;
    public double valueEth;
    public double deltaEth;
    public double priceUsd;
    public double valueUsd;
    public double deltaUsd;

    public String[] exchangeList;
    public String exchangeSelected;

    public ArrayList<history> histories = new ArrayList<history>();


    public Record(String symbol, double count) {
        this.symbol = symbol;
        this.count = count;
    }

    public Record(String symbol) {
        this.symbol = symbol;
    }
}
