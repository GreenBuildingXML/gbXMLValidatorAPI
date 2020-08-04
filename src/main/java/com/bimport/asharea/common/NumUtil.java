package com.bimport.asharea.common;

import com.google.gson.JsonElement;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumUtil {
    private static long GB = 1 << 30;
    private static long MB = 1 << 20;
    private static long KB = 1 << 10;
    private static DecimalFormat df0 = new DecimalFormat("#");
    private static DecimalFormat df1 = new DecimalFormat("#.#");
    private static DecimalFormat df2 = new DecimalFormat("#.##");
    private static DecimalFormat df3 = new DecimalFormat("#.00");
    private static DecimalFormat df4 = new DecimalFormat("#.####");

    public static double readDouble(String num, double defaultVal) {
        if (num == null || num.isEmpty()) {
            return defaultVal;
        }

        try {
            return Double.parseDouble(num);
        } catch (Exception e) {
            return defaultVal;
        }
    }

    public static double getDoubleOrDefault(Double num, double defaultVal){
        if (num == null) {
            return defaultVal;
        }
        return num;
    }

    public static int readInt(String num, int defaultVal) {
        if (num == null || num.isEmpty()) {
            return defaultVal;
        }

        try {
            return Integer.parseInt(num);
        } catch (NumberFormatException e) {
            try {
                double d = Double.parseDouble(num);
                return (int) d;
            } catch (NumberFormatException ex) {
                return defaultVal;
            }
        }
    }

    public static int readInt(JsonElement je, int defaultVal) {
        if (je == null || je.isJsonNull()) {
            return defaultVal;
        }
        return readInt(je.getAsString(), defaultVal);
    }

    public static String formatNumber1(Number n) {
        return df1.format(n.doubleValue());
    }

    public static Double formatDouble2Floor(Double n) {
        DecimalFormat tmp = new DecimalFormat("#0.##");
        tmp.setRoundingMode(RoundingMode.FLOOR);
        return Double.valueOf(tmp.format(n));
    }

    public static Double formatDouble2(Double n) {
        return Double.valueOf(df3.format(n));
    }
    public static String formatNumber2(Number n) {
        return df2.format(n.doubleValue());
    }

    public static String formatNumber4(Number n) {
        return df4.format(n.doubleValue());
    }

    public static String readableSize(long size) {
        if (size >= GB) {
            return df2.format((double) size / GB) + " GB";
        }
        if (size >= MB) {
            return df2.format((double) size / MB) + " MB";
        }
        if (size >= KB) {
            return df2.format((double) size / KB) + " KB";
        }
        return size + " Bytes";
    }

    /**
     * E.g. [0.3, 0.4, 0.5]
     */
    public static double[] readDoubleArray(String str) {
        if (str.equals("default")) {
            //skip the default key word
            return new double[0];
        }

        str = str.substring(1, str.length() - 1); // trim [ and ]
        if (str.length() > 0) {
            String[] nums = str.split(",");
            double[] res = new double[nums.length];
            for (int i = 0; i < res.length; i++) {
                res[i] = readDouble(nums[i].trim(), 0);
            }
            return res;
        }

        return new double[0];
    }

    public static String readPrice(double price) {
        String trim = formatNumber2(price);
        return "$" + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(trim));
    }
}
