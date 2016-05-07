package com.lvack.MasterStats.Util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

/**
 * NumberFormatterClass for MasterStats
 *
 * @author Leon Vack - TWENTY |20
 */

public class NumberFormatter {
    public static final DecimalFormat FORMAT = getFormat();

    private static DecimalFormat getFormat() {
        DecimalFormat decimalFormat = (DecimalFormat) NumberFormat.getInstance();
        DecimalFormatSymbols decimalFormatSymbols = decimalFormat.getDecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(' ');
        decimalFormatSymbols.setDecimalSeparator('.');
        return new DecimalFormat("###,###,###,##0", decimalFormatSymbols);
    }

    public static String formatInt(int i) {
        return FORMAT.format(i);
    }

    public static String formatLong(long i) {
        return FORMAT.format(i);
    }
}
