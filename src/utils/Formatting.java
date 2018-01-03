package utils;

public class Formatting {
    public static String signAndSize(double num, int size) {
        StringBuilder sb = new StringBuilder();
        sb.append(num < 0 ? '-' : '+');
        sb.append(num);
        sb.delete(size + 2, sb.length());
        return sb.toString();
    }
}
