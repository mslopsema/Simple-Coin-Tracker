package utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.*;
import java.util.*;

public class Files {
    private static final String CONFIG = "config";

    public static void saveConfig(Set<String> trackers, HashMap<String, String> assets) {
        try {

            JsonArray ja_trackers = new JsonArray();
            for (String s : trackers) ja_trackers.add(s);

            JsonArray ja_assets = new JsonArray();
            for (String s : assets.keySet()) {
                JsonObject j = new JsonObject();
                j.add("symbol", s);
                j.add("count", assets.get(s));
                ja_assets.add(j);
            }

            JsonObject root = new JsonObject();
            root.add("trackers", ja_trackers);
            root.add("assets", ja_assets);

            File f = new File(CONFIG);
            if (f.exists()) f.delete();
            f.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(root.toString());
            bw.flush();
            bw.close();
            System.out.println("Save Success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadConfig(Set<String> trackers, HashMap<String, String> assets) {
        try {
            JsonObject root = (JsonObject) Json.parse(new FileReader(CONFIG));
            JsonArray ja_trackers = root.get("trackers").asArray();
            for (JsonValue jv : ja_trackers) trackers.add(jv.asString());
            JsonArray ja_assets = root.get("assets").asArray();
            for (JsonValue jv : ja_assets) {
                JsonObject jo = jv.asObject();
                String symbol = jo.getString("symbol", "");
                String count = jo.getString("count", "");
                assets.put(symbol, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
