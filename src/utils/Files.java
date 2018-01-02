package utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Set;

public class Files {
    private static final String FILE_CONFIG = ".sct.config";
    private static final String KEY_TRACKERS = "trackers";
    private static final String KEY_ASSETS = "assets";
    private static final String KEY_SYMBOL = "symbol";
    private static final String KEY_COUNT = "count";

    /**
     * For saving configuration to backup file
     * Backup file will be '.coin.config'
     * @param trackers
     * @param assets
     */
    public static void saveConfig(Set<String> trackers, HashMap<String, String> assets) {
        try {

            // Transform data to JSON
            JsonArray ja_trackers = new JsonArray();
            for (String s : trackers) ja_trackers.add(s);

            JsonArray ja_assets = new JsonArray();
            for (String s : assets.keySet()) {
                JsonObject j = new JsonObject();
                j.add(KEY_SYMBOL, s);
                j.add(KEY_COUNT, assets.get(s));
                ja_assets.add(j);
            }

            JsonObject root = new JsonObject();
            root.add(KEY_TRACKERS, ja_trackers);
            root.add(KEY_ASSETS, ja_assets);

            // Save JSON to Config File
            File f = new File(FILE_CONFIG);
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

    /**
     * For retrieving data from backup config file
     * @param trackers
     * @param assets
     */
    public static void loadConfig(Set<String> trackers, HashMap<String, String> assets) {
        try {
            JsonObject root = (JsonObject) Json.parse(new FileReader(FILE_CONFIG));
            JsonArray ja_trackers = root.get(KEY_TRACKERS).asArray();
            for (JsonValue jv : ja_trackers) trackers.add(jv.asString());
            JsonArray ja_assets = root.get(KEY_ASSETS).asArray();
            for (JsonValue jv : ja_assets) {
                JsonObject jo = jv.asObject();
                String symbol = jo.getString(KEY_SYMBOL, "");
                String count = jo.getString(KEY_COUNT, "");
                assets.put(symbol, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
