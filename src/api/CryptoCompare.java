package api;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;

public class CryptoCompare {
    
    String base = "https://min-api.cryptocompare.com/data/all/coinlist";
    String price = "https://min-api.cryptocompare.com/data/pricemulti?fsyms=";
    String priceb = "&tsyms=BTC,USD";
    
    public CryptoCompare() {
        //reloadSymbolMap();
    }
    
    public List<String> getSymbols() {
        List<String> list = new ArrayList<String>();
        JsonObject jo = getHttp(base);
        System.out.println(jo.toString());
        return list;
    }
    
    public JsonObject getPrice(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String s : set) sb.append(s).append(",");
        String url = price + sb.toString() + priceb;
        return getHttp(url);
    }
    
    public JsonObject getHttp(String url) {
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
