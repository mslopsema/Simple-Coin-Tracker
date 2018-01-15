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
    public double priceEth;
    public double valueEth;
    public double deltaEth;
    public double priceUsd;
    public double valueUsd;
    public double deltaUsd;

    public Record(String symbol, double count) {
        this.symbol = symbol;
        this.count = count;
    }

    public Record(String symbol) {
        this.symbol = symbol;
    }
}
