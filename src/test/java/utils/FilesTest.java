package utils;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.junit.Test;

import static org.junit.Assert.*;

public class FilesTest {
    @Test
    public void saveConfig() {
        String symbol = "test_symbol";
        String count = "test_count";
        int N = 50;

        JsonObject root = new JsonObject();
        JsonArray ja = new JsonArray();
        for (int i = 0; i < 50; i++) {
            JsonObject obj = new JsonObject();
            obj.add(Files.KEY_SYMBOL, symbol);
            obj.add(Files.KEY_COUNT, count);
            ja.add(obj);
        }
        root.add(Files.KEY_TRACKERS, ja);

        Files.saveConfig(root);
        JsonValue retVal = Files.loadConfig();
        JsonArray retArr = retVal.asObject().get(Files.KEY_TRACKERS).asArray();

        for (int i = 0; i < 50; i++) {
            assertEquals(retArr.get(i).asObject().getString(Files.KEY_SYMBOL, ""), symbol);
            assertEquals(retArr.get(i).asObject().getString(Files.KEY_COUNT, ""), count);
        }
    }
}
