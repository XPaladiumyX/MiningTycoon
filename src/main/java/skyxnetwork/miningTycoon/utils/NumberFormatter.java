package skyxnetwork.miningTycoon.utils;

public class NumberFormatter {

    public static String format(double number) {
        if (number < 1000) {
            return String.format("%.0f", number);
        } else if (number < 1000000) {
            return String.format("%.2fK", number / 1000);
        } else if (number < 1000000000) {
            return String.format("%.2fM", number / 1000000);
        } else if (number < 1000000000000L) {
            return String.format("%.2fB", number / 1000000000);
        } else if (number < 1000000000000000L) {
            return String.format("%.2fT", number / 1000000000000L);
        } else {
            return String.format("%.2fQ", number / 1000000000000000L);
        }
    }
}
