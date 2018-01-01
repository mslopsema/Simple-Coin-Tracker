package api;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CoinCap {
    
    public static final String URL_COINS   = "http://coincap.io/coins/";
    public static final String URL_MAP     = "http://coincap.io/map/";
    public static final String URL_CURRENT = "http://coincap.io/page/";
    public static final String URL_HISTORY = "http://coincap.io/history/";
    
    Set<String> coins = new HashSet<String>();
    
    public CoinCap() {
        reloadCoins();
    }
    
    public void reloadCoins() {
        JsonArray ja = (JsonArray) getHttp(URL_COINS);
        //JsonArray ja = (JsonArray) getHttp(URL_CURRENT + "BTC");
    }
    
    public JsonValue getHttp(String url) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            //c.setRequestProperty("Limit", "10");
            c.setConnectTimeout(10000);
            c.connect();
            
            
            int status = c.getResponseCode();
            System.out.println(c.getURL() + " -> [" + status + "]");
            JsonValue jv = Json.parse(new InputStreamReader(c.getInputStream()));
            System.out.println(jv.toString());
            return jv;
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
