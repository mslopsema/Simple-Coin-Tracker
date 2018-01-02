package api;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

/**
 * Coin Tracking API Implementation using CryptoCompare APIs
 */
public class CryptoCompare {
    public static final String HOME = "https://cryptocompare.com/";
    private static final String API_COIN_LIST = "https://min-api.cryptocompare.com/data/all/coinlist";
    private static final String API_PRICE_PREFIX = "https://min-api.cryptocompare.com/data/pricemulti";
    private static final String KEY_COINS = "Data";
    private static final int TIMEOUT = 10000;
    private Set<String> SYMBOLS = new HashSet<String>();


    public CryptoCompare() {
        loadSymbols();
    }

    private void loadSymbols() {
        List<String> list = getHttp(API_COIN_LIST).get(KEY_COINS).asObject().names();
        for (String s : list) SYMBOLS.add(s);
    }

    public boolean containsSymbol(String s) {
        return SYMBOLS.contains(s);
    }

    public JsonObject getPrice(Set<String> coinSet, Set<String> returnUnits) {
        if (coinSet.size() < 1 || returnUnits.size() < 1) return null;
        String coins = queryBuilder("?fsyms=", coinSet);
        String units = queryBuilder("&tsyms=", returnUnits);
        String url = API_PRICE_PREFIX + coins + units;
        return getHttp(url);
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
            System.out.println(c.getURL() + " -> [" + status + "][" + (System.currentTimeMillis() - start) + "ms]");
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
