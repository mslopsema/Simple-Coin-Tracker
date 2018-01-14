package utils;

public class Formatting {

    /**
     * For applying sign (+/-) and truncating the total size of a double
     * @param num the double to be processed
     * @param size the total size of the return string.
     * @return String
     */
    public static String signAndSize(double num, int size) {
        StringBuilder sb = new StringBuilder();
        if (num > 0) sb.append('+');
        sb.append(num);
        if (sb.length() > size) sb.delete(size, sb.length());
        return sb.toString();
    }
}
