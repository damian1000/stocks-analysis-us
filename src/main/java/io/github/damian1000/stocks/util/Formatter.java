package io.github.damian1000.stocks.util;

import java.text.DecimalFormat;

public class Formatter {

    private static DecimalFormat df = new DecimalFormat("0.00");

    public static double format(double input) {
        return Double.valueOf(df.format(input));
    }

}
