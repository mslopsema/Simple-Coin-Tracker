package api;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

/**
 * Coin Tracking API Implementation using CryptoCompare APIs
 */
public class CryptoCompare {
    /*
    public static final String[] MARKETS = {"ANXBTC", "Abucoins", "BTC38", "BTCChina", "BTCE", "BTCMarkets",
            "BTCXIndia", "BTER", "BXinth", "Binance", "Bit2C", "BitBay", "BitMarket", "BitSquare", "BitTrex",
            "Bitfinex", "Bithumb", "Bitso", "Bitstamp", "Bleutrade", "CCCAGG", "CCEDK", "CCEX", "CHBTC", "Cexio",
            "Coinbase", "Coincheck", "Coinfloor", "Coinone", "Coinroom", "Coinse", "Coinsetter", "CryptoX", "Cryptopia",
            "Cryptsy", "EtherDelta", "EthexIndia", "Exmo", "Gatecoin", "Gateio", "Gemini", "HitBTC", "Huobi",
            "HuobiPro", "Jubi", "Korbit", "Kraken", "LakeBTC", "Liqui", "LiveCoin", "LocalBitcoins", "Luno", "Lykke",
            "MercadoBitcoin", "MonetaGo", "MtGox", "Novaexchange", "OKCoin", "OKEX", "Paymium", "Poloniex",
            "QuadrigaCX", "Quoine", "Remitano", "TheRockTrading", "Tidex", "TuxExchange", "Unocoin", "Vaultoro",
            "ViaBTC", "WavesDEX", "Yacuna", "Yobit", "Yunbi", "Zaif", "bitFlyer", "bitFlyerFX", "btcXchange", "itBit"};*/
    //public static final String[] MARKETS = {"Binance", "CCCAGG", "Coinbase", "Gemini"};
    public static final String HOME = "https://cryptocompare.com/";
    private static final String API_COIN_LIST = "https://min-api.cryptocompare.com/data/all/coinlist";
    private static final String API_PRICE_PREFIX = "https://min-api.cryptocompare.com/data/pricemultifull";
    private static final String API_HISTORY_PREFIX = "https://min-api.cryptocompare.com/data/histominute";
    private static final String KEY_DATA = "Data";
    private static final int TIMEOUT = 10000;
    private static final int HISTORY = 100;
    private static int marketId = 1; // Default CCCAGG (Aggregate)
    private Set<String> SYMBOLS = new HashSet<String>();


    public CryptoCompare() {
        System.out.println("Using API Source : " + HOME);
    }

    public void loadSymbols() {
        SYMBOLS.addAll(getHttp(API_COIN_LIST).get(KEY_DATA).asObject().names());
        System.out.println("SYMBOLS : " + SYMBOLS.toString());
    }

    public boolean containsSymbol(String s) {
        return SYMBOLS.contains(s);
    }

    public JsonArray getHistory(String from, String to) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_HISTORY_PREFIX);
        urlBuilder.append("?fsym=" + from);
        urlBuilder.append("&tsym=" + to);
        urlBuilder.append("&limit=" + HISTORY);
        return (JsonArray) getHttp(urlBuilder.toString()).get(KEY_DATA);
    }

    public JsonObject getPrice(Set<String> coinSet, Set<String> returnUnits) {
        if (coinSet.size() < 1 || returnUnits.size() < 1) return null;
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(API_PRICE_PREFIX);
        urlBuilder.append(queryBuilder("?fsyms=", coinSet));
        urlBuilder.append(queryBuilder("&tsyms=", returnUnits));
        return getHttp(urlBuilder.toString());
    }

    private String queryBuilder(String prefix, Set<String> vals) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        for (String s : vals) sb.append(s).append(",");
        //sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private JsonObject getHttp(String url) {
        HttpURLConnection c = null;
        try {
            long start = System.currentTimeMillis();
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            //c.setRequestProperty("Limit", "10");
            c.setConnectTimeout(TIMEOUT);
            c.connect();

            int status = c.getResponseCode();
            //System.out.println(c.getURL() + " -> [" + status + "][" + (System.currentTimeMillis() - start) + "ms]");
            return (JsonObject) Json.parse(new InputStreamReader(c.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
        return null;
    }
}
