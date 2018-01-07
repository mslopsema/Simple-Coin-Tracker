package api;

import ui.Elements;

import java.util.HashSet;

public class ApiBase {
    protected static final int TIMEOUT = 10000;

    public String HOME = "Source Home Url";
    protected HashSet<String> SYMBOLS = new HashSet<String>();

    protected ApiBase() {}

    public boolean contains(String s) {
        return SYMBOLS.contains(s);
    }

    public void loadSymbols() {}

    public boolean updatePrice(Elements e) {return false; }
}
