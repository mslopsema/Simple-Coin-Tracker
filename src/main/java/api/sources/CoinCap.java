package api.sources;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Set;

import api.ApiBase;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import ui.Elements;

public class CoinCap extends ApiBase {

    public static final String URL_COINS   = "http://coincap.io/coins/";
    public static final String URL_FRONT   = "http://coincap.io/front/";
    public static final String URL_MAP     = "http://coincap.io/map/";
    public static final String URL_CURRENT = "http://coincap.io/page/";
    public static final String URL_HISTORY = "http://coincap.io/history/";

    public CoinCap() {
        HOME = "http://coincap.io/";
    }

    @Override
    public void loadSymbols() {
        JsonArray ja = (JsonArray) getHttp(URL_COINS);
        for (JsonValue jv : ja) SYMBOLS.add(jv.asString());
        System.out.println(SYMBOLS.toString());
    }

    @Override
    public boolean updatePrice(Elements e) {
        JsonArray ja = (JsonArray) getHttp(URL_FRONT);
        for (JsonValue jv : ja) {
            JsonObject jo = (JsonObject) jv;
            String s = jo.getString("short", "");
            if (s.length() < 1) continue;

            double usdPrice = jo.getDouble("price", 1);
            double usdChange = jo.getDouble("perc", 0);


        }
        return true;
    }

    @Override
    public boolean getHistory(Elements e) {
        return true;
    }
    
    private JsonValue getHttp(String url) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setConnectTimeout(TIMEOUT);
            c.setRequestProperty("User-Agent", "Simple-Coin-Tracker");
            c.connect();

            int status = c.getResponseCode();
            System.out.println(c.getURL() + " -> [" + status + "]");
            return Json.parse(new InputStreamReader(c.getInputStream()));
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
