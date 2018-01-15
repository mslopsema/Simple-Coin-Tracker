package api.sources;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import api.ApiBase;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import org.jfree.data.xy.XYDataset;
import ui.Elements;
import ui.Record;
import utils.Formatting;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Coin Tracking API Implementation using CryptoCompare APIs
 */
public class CryptoCompare extends ApiBase {
    /*
    public static final String[] MARKETS = {"ANXBTC", "Abucoins", "BTC38", "BTCChina", "BTCE", "BTCMarkets",
            "BTCXIndia", "BTER", "BXinth", "Binance", "Bit2C", "BitBay", "BitMarket", "BitSquare", "BitTrex",
            "Bitfinex", "Bithumb", "Bitso", "Bitstamp", "Bleutrade", "CCCAGG", "CCEDK", "CCEX", "CHBTC", "Cexio",
            "Coinbase", "Coincheck", "Coinfloor", "Coinone", "Coinroom", "Coinse", "Coinsetter", "CryptoX", "Cryptopia",
            "Cryptsy", "EtherDelta", "EthexIndia", "Exmo", "Gatecoin", "Gateio", "Gemini", "HitBTC", "Huobi",
            "HuobiPro", "Jubi", "Korbit", "Kraken", "LakeBTC", "Liqui", "LiveCoin", "LocalBitcoins", "Luno", "Lykke",
            "MercadoBitcoin", "MonetaGo", "MtGox", "Novaexchange", "OKCoin", "OKEX", "Paymium", "Poloniex",
            "QuadrigaCX", "Quoine", "Remitano", "TheRockTrading", "Tidex", "TuxExchange", "Unocoin", "Vaultoro",
            "ViaBTC", "WavesDEX", "Yacuna", "Yobit", "Yunbi", "Zaif", "bitFlyer", "bitFlyerFX", "btcXchange", "itBit"};
    public static final String[] MARKETS = {"Binance", "CCCAGG", "Coinbase", "Gemini"};
    */

    private static final String API_COIN_LIST = "https://min-api.cryptocompare.com/data/all/coinlist";
    private static final String API_PRICE_PREFIX = "https://min-api.cryptocompare.com/data/pricemultifull";
    private static final String API_HISTORY_MIN_PREFIX = "https://min-api.cryptocompare.com/data/histominute?fsym=";
    private static final String API_HISTORY_MIN_SUFFIX = "&tsym=USD&limit=1440&e=CCCAGG";
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
        urlBuilder.append("&tsyms=BTC,USD");
        JsonObject jo = getHttp(urlBuilder.toString());
        if (jo == null) return false;

        double sumUsd = 0;
        double sumBtc = 0;
        double sumUsdOld = 0;
        double sumBtcOld = 0;

        JsonObject rawObj = (JsonObject) jo.get("RAW");
        for (String s : rawObj.names()) {
            JsonObject obj = (JsonObject) rawObj.get(s);
            if (obj == null) continue;

            double btcPrice  = getDouble(((JsonObject) obj.get("BTC")).get("PRICE"));
            double btcChange = getDouble(((JsonObject) obj.get("BTC")).get("CHANGEPCT24HOUR"));
            double usdPrice  = getDouble(((JsonObject) obj.get("USD")).get("PRICE"));
            double usdChange = getDouble(((JsonObject) obj.get("USD")).get("CHANGEPCT24HOUR"));
            //System.out.println(s + " : " + btcPrice + " " + btcChange + " " + usdPrice + " " + usdChange);

            // Crypto Compare has a bug where they send value of '0' for BTC/BTC price
            // It will cause NaN when trying to calculate pricing changes.
            if (s.equals("BTC")) {
                btcPrice = 1.0;
                btcChange = 0.0;
            }

            if (e.tables.modelTrackers.contains(s)) {
                Record r = e.tables.modelTrackers.get(s);
                r.priceBtc = btcPrice;
                r.deltaBtc = btcChange;
                r.priceUsd = usdPrice;
                r.deltaUsd = usdChange;
            }
            if (e.tables.modelPortfolio.contains(s)) {
                Record r = e.tables.modelPortfolio.get(s);

                btcChange /= 100;
                usdChange /= 100;
                double btcSum = btcPrice * r.count;
                double usdSum = usdPrice * r.count;
                sumBtc += btcSum;
                sumUsd += usdSum;
                double btcSumOld = btcSum / (btcChange + 1);
                double usdSumOld = usdSum / (usdChange + 1);
                double btcDiff = btcSum - btcSumOld;
                double usdDiff = usdSum - usdSumOld;
                sumBtcOld += btcSumOld;
                sumUsdOld += usdSumOld;

                r.priceBtc = btcPrice;
                r.valueBtc = btcSum;
                r.deltaBtc = btcDiff;
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
        e.updateAssetTotal(sumBtc, sumUsd, true);

        return true;
    }

    private String queryBuilder(String prefix, Set<String> vals) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (String s : vals) sb.append(s).append(",");
        return sb.toString();
    }

    public boolean getHistory(Elements e) {

        TimeSeriesCollection tsc = new TimeSeriesCollection();

        for (String key : e.tables.modelPortfolio.keySet()) {
            String url = API_HISTORY_MIN_PREFIX + key + API_HISTORY_MIN_SUFFIX;
            JsonObject jo = getHttp(url);

            TimeSeries ts = new TimeSeries(key);
            JsonArray data = jo.get("Data").asArray();
            double first = -1;

            for (JsonValue jv : data) {
                JsonObject obj = jv.asObject();
                long time = obj.getLong("time", 1453116960);
                double close = obj.getDouble("close", 1);
                if (first < 0) first = close;
                double percentage = close / first;

                Date date = new Date(time * 1000);
                ts.addOrUpdate(new Minute(date), percentage);
            }
            tsc.addSeries(ts);
        }
        e.graphs.portfolio.setData(tsc);
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
