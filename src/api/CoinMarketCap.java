package api;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CoinMarketCap {
    private static final String base = "https://api.coinmarketcap.com/v1/ticker/";
    
    private HashMap<String, String> symbols;
    
    public CoinMarketCap() {
        reloadSymbolMap();
    }
    
    public boolean containsSymbol(String symbol) {
        return symbols.containsKey(symbol);
    }
    
    public void reloadSymbolMap() {
        symbols = new HashMap<String, String>();
        JsonArray jarr = getHttp(base, 10000);
        
        for (JsonValue jv : jarr) {
            JsonObject jo = jv.asObject();
            symbols.put(jo.getString("symbol", "XXX"), jo.getString("id", "none"));
        }
        
        System.out.println(symbols.toString());
    }
    
    public static JsonArray getHttp(String url, int timeout) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            //c.setRequestProperty("Limit", "10");
            c.setConnectTimeout(timeout);
            c.connect();
            
            int status = c.getResponseCode();
            System.out.println(c.getURL() + " -> [" + status + "]");
            return (JsonArray) Json.parse(new InputStreamReader(c.getInputStream()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.disconnect();
            }
        }
        return null;
    }
    
    public void printPrice(String symbol) {
        String split = " : ";
        String url = base + symbols.get(symbol) + "/";
        JsonArray jarr = getHttp(url, 10000);
        JsonObject jo = jarr.get(0).asObject();
        StringBuilder sb = new StringBuilder();
        sb.append(symbol).append(split);
        sb.append(jo.getString("price_usd", "0.0")).append(split);
        sb.append(jo.getString("last_updated", "0"));
        System.out.println(sb.toString());
    }
    
}
