package api.sources;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import api.ApiBase;
import org.jfree.data.time.*;
import ui.Elements;
import ui.Record;
import utils.Formatting;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Coin Tracking API Implementation using CryptoCompare APIs
 */
public class CryptoCompare extends ApiBase {

    private static final String API_COIN_LIST = "https://min-api.cryptocompare.com/data/all/coinlist";
    private static final String API_PRICE_PREFIX = "https://min-api.cryptocompare.com/data/pricemultifull";
    private static final String API_HISTORY_MIN_PREFIX = "https://min-api.cryptocompare.com/data/histominute?fsym=";
    private static final String API_HISTORY_MIN_SUFFIX = "&tsym=USD&limit=2000&e=CCCAGG";
    private static final String API_HISTORY_HOUR_PREFIX = "https://min-api.cryptocompare.com/data/histohour?fsym=";
    private static final String API_HISTORY_DAY_PREFIX = "https://min-api.cryptocompare.com/data/histoday?fsym=";
    private static final String KEY_DATA = "Data";
    private static final int HISTORY = 100;

    public CryptoCompare() {
        HOME = "https://cryptocompare.com/";
    }

    @Override
    public void loadSymbols() {
        SYMBOLS.addAll(getHttp(API_COIN_LIST).get(KEY_DATA).asObject().names());
        System.out.println("SYMBOLS : " + SYMBOLS.toString());
    }


    @Override
    public boolean updatePrice(Elements e) {
        HashSet<String> keys = new HashSet<String>();
        keys.addAll(e.tables.modelTrackers.keySet());
        keys.addAll(e.tables.modelPortfolio.keySet());
        if (keys.size() <= 0) return true;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_PRICE_PREFIX);
        urlBuilder.append(queryBuilder("?fsyms=", keys));
        urlBuilder.append("&tsyms=BTC,ETH,USD");
        JsonObject jo = getHttp(urlBuilder.toString());
        if (jo == null) return false;

        double sumUsd = 0;
        double sumBtc = 0;
        double sumEth = 0;
        double sumUsdOld = 0;
        double sumBtcOld = 0;
        double sumEthOld = 0;

        JsonObject rawObj = (JsonObject) jo.get("RAW");
        for (String s : rawObj.names()) {
            JsonObject obj = (JsonObject) rawObj.get(s);
            if (obj == null) continue;

            double btcPrice  = getDouble(((JsonObject) obj.get("BTC")).get("PRICE"));
            double btcChange = getDouble(((JsonObject) obj.get("BTC")).get("CHANGEPCT24HOUR"));
            double ethPrice  = getDouble(((JsonObject) obj.get("ETH")).get("PRICE"));
            double ethChange = getDouble(((JsonObject) obj.get("ETH")).get("CHANGEPCT24HOUR"));
            double usdPrice  = getDouble(((JsonObject) obj.get("USD")).get("PRICE"));
            double usdChange = getDouble(((JsonObject) obj.get("USD")).get("CHANGEPCT24HOUR"));
            /*System.out.println(s + " : " +
                    btcPrice + " " + btcChange + " " +
                    ethPrice + " " + ethChange + " " +
                    usdPrice + " " + usdChange);*/

            // Crypto Compare has a bug where they send value of '0' for BTC/BTC price
            // It will cause NaN when trying to calculate pricing changes.
            if (s.equals("BTC")) {
                btcPrice = 1.0;
                btcChange = 0.0;
            } else if (s.equals("ETH")) {
                ethPrice = 1.0;
                ethChange = 0.0;
            }

            if (e.tables.modelTrackers.contains(s)) {
                Record r = e.tables.modelTrackers.get(s);
                r.priceBtc = btcPrice;
                r.deltaBtc = btcChange;
                r.priceEth = ethPrice;
                r.deltaEth = ethChange;
                r.priceUsd = usdPrice;
                r.deltaUsd = usdChange;
            }
            if (e.tables.modelPortfolio.contains(s)) {
                Record r = e.tables.modelPortfolio.get(s);

                btcChange /= 100;
                ethChange /= 100;
                usdChange /= 100;
                double btcSum = btcPrice * r.count;
                double ethSum = ethPrice * r.count;
                double usdSum = usdPrice * r.count;
                sumBtc += btcSum;
                sumEth += ethSum;
                sumUsd += usdSum;
                double btcSumOld = btcSum / (btcChange + 1);
                double ethSumOld = ethSum / (ethChange + 1);
                double usdSumOld = usdSum / (usdChange + 1);
                double btcDiff = btcSum - btcSumOld;
                double ethDiff = ethSum - ethSumOld;
                double usdDiff = usdSum - usdSumOld;
                sumBtcOld += btcSumOld;
                sumEthOld += ethSumOld;
                sumUsdOld += usdSumOld;

                r.priceBtc = btcPrice;
                r.valueBtc = btcSum;
                r.deltaBtc = btcDiff;
                r.priceEth = ethPrice;
                r.valueEth = ethSum;
                r.deltaEth = ethDiff;
                r.priceUsd = usdPrice;
                r.valueUsd = usdSum;
                r.deltaUsd = usdDiff;
            }
        }
        e.tables.modelTrackers.fireTableDataChanged();
        e.tables.modelPortfolio.fireTableDataChanged();
        DecimalFormat dfPct = new DecimalFormat("+0.00%;-0.00%");
        e.textFields.assetValueChangePctBtc.setText(String.valueOf(dfPct.format(sumBtc / sumBtcOld - 1)));
        e.textFields.assetValueChangePctUsd.setText(String.valueOf(dfPct.format(sumUsd / sumUsdOld - 1)));
        e.textFields.assetValueChangRawBtc.setText(Formatting.signAndSize(sumBtc - sumBtcOld, 10));
        e.textFields.assetValueChangRawUsd.setText(Formatting.signAndSize(sumUsd - sumUsdOld, 10));
        e.updateAssetTotal(sumBtc, sumEth, sumUsd, true);

        return true;
    }

    private String queryBuilder(String prefix, Set<String> vals) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (String s : vals) sb.append(s).append(",");
        return sb.toString();
    }

    /**
     * For retrieving the historical pricing of each coin in the portfolio.
     * Will call each coin request in seperate thread, and load the results into the graph.
     * @param recordList
     * @return
     */
    public boolean getHistory(ArrayList<Record> recordList) {

        class Task implements Callable<Boolean> {
            private Record record;

            Task(Record record) {
                this.record = record;
            }

            public Boolean call() {
                JsonObject jo = getHttp(API_HISTORY_MIN_PREFIX + record.symbol + API_HISTORY_MIN_SUFFIX);
                record.histories.clear();
                JsonArray data = jo.get("Data").asArray();

                for (JsonValue jv : data) {
                    JsonObject obj = jv.asObject();
                    long time = obj.getLong("time", 1453116960);
                    double close = obj.getDouble("close", 1);
                    record.histories.add(new Record.history(time, close));
                }
                return true;
            }
        }


        if (recordList.size() < 1) return false;

        // Build the threadpool with each coin
        ExecutorService executor = Executors.newFixedThreadPool(recordList.size());
        CompletionService<Boolean> completionService = new ExecutorCompletionService<Boolean>(executor);
        for (Record r : recordList) completionService.submit(new Task(r));
        executor.shutdown();
        return true;
    }

    private JsonObject getHttp(String url) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(TIMEOUT);
            c.connect();

            int status = c.getResponseCode();
            JsonObject jo = (JsonObject) Json.parse(new InputStreamReader(c.getInputStream()));
            System.out.println(c.getURL() + " -> [" + status + "]");
            //System.out.println(jo.toString());
            return jo;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
        return null;
    }

    /**
     * For resolving crypto compare bug where they send inconsistent double formatting.
     * Sometimes it comes as a double : #.###
     * Sometimes it comes as a string : "#.###"
     * Solution -> Just try parsing both
     * @param jv
     * @return
     */
    private double getDouble(JsonValue jv) {
        double ret;
        try {
            ret = jv.asDouble();
        } catch (Exception e) {
            ret = Double.parseDouble(jv.asString());
        }
        return ret;
    }
}
