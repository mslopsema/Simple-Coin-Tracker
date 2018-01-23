package utils;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Files {
    public static final String FILE_CONFIG = ".sct.config";
    public static final String KEY_TRACKERS = "trackers";
    public static final String KEY_ASSETS = "assets";
    public static final String KEY_SYMBOL = "symbol";
    public static final String KEY_COUNT = "count";

    /**
     * For saving configuration to backup file
     * Backup file will be '.coin.config'
     * @param root
     */
    public static void saveConfig(JsonValue root) {
        try {
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
     */
    public static JsonValue loadConfig() {
        JsonValue jv = new JsonObject();
        try {
            jv = Json.parse(new FileReader(FILE_CONFIG));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jv;
    }
}
