package cn.javacoder.stockanalyze.utils;

import org.springframework.util.StringUtils;
import org.springframework.util.TypeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class MiscUtils {

    public static  String[] exchanges = {"SH","SZ"};

    private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatYesterday() {
        Date d = new Date(System.currentTimeMillis() - 24*60*60*1000)  ;
        return format(d);
    }

    public static String format(Date d) {
        return df.format(d);
    }

    public static  Date parseDate(String strDate) {
        try {
            return df.parse(strDate);
        } catch (ParseException e) {
            throw new RuntimeException("parse date error " + strDate, e);
        }
    }

    private static  final Pattern NUMBER_FORMAT = Pattern.compile("^[-+]?\\d+(\\.\\d+)?([eE]?\\d+)?$");

    public static int convert2intWith2P(String val) {
        if(StringUtils.isEmpty(val)){
            return 0;
        }
        if(!NUMBER_FORMAT.matcher(val).matches()) {
            return 0;
        }
        double d = Double.valueOf(val);
        return (int)(d*100);
    }

    /**
     * 小数转换成百分比
     */
    public static int d2p(String val) {
        if(StringUtils.isEmpty(val)){
            return 0;
        }
        if(!NUMBER_FORMAT.matcher(val).matches()) {
            return 0;
        }
        double d = Double.valueOf(val);
        return (int)(d*100);
    }

    public static int convert2Integer(String val) {
        if(!NUMBER_FORMAT.matcher(val).matches()) {
            return 0;
        }
        return Integer.valueOf(val);
    }

    public static long d2i(String val) {
        if(!NUMBER_FORMAT.matcher(val).matches()) {
            return 0;
        }
        return Double.valueOf(val).longValue();
    }


    public static long convert2Long(String val) {
        if(!NUMBER_FORMAT.matcher(val).matches()) {
            return 0;
        }
        return Long.valueOf(val);
    }

    public static int convert2Million(String val) {
        if(StringUtils.isEmpty(val)){
            return 0;
        }
        if(!NUMBER_FORMAT.matcher(val).matches()) {
            return 0;
        }
        double l = Double.valueOf(val);
        return (int)(l/1000000);
    }
}
