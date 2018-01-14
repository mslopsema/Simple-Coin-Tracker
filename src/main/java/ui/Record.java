package ui;

/**
 * A Record represents 1 row in a table
 */
public class Record {
    public String symbol;
    public double count;
    public double priceBtc;
    public double valueBtc;
    public double deltaBtc;
    public double priceUsd;
    public double valueUsd;
    public double deltaUsd;

    Record(String symbol, double count) {
        this.symbol = symbol;
        this.count = count;
    }

    Record(String symbol) {
        this.symbol = symbol;
    }
}
