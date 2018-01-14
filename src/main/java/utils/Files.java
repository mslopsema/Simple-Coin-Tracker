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
    public static final String FILE_CONFIG = ".sct.config";
    public static final String KEY_TRACKERS = "trackers";
    public static final String KEY_ASSETS = "assets";
    public static final String KEY_SYMBOL = "symbol";
    public static final String KEY_COUNT = "count";

    /**
     * For saving configuration to backup file
     * Backup file will be '.coin.config'
     * @param trackers
     * @param assets
     */
    public static void saveConfig(JsonArray trackers, JsonArray assets) {
        try {
            JsonObject root = new JsonObject();
            root.add(KEY_TRACKERS, trackers);
            root.add(KEY_ASSETS, assets);

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
