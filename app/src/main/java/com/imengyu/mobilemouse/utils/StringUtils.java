package com.imengyu.mobilemouse.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtils {

    /**
     * 检查文字是否为null或空字符串
     * @param s 字符串
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * 获取可读时间字符串
     * @param millisecond 毫秒
     * @return 字符串
     */
    public static String getTimeString(int millisecond) {
        int hour = millisecond / (1000 * 3600);
        int min = millisecond / (1000 * 60) - hour * 60;
        int sec = millisecond / 1000 - (hour * 60 + min) * 60;

        StringBuilder sb = new StringBuilder();
        if(hour > 0) {
            sb.append(hour);
            sb.append(':');
            if(min < 10)
                sb.append('0');
        }

        sb.append(min);
        sb.append(':');
        if(sec < 10)
            sb.append('0');
        sb.append(sec);

        return sb.toString();
    }

    /***
     * 是否包含指定字符串,不区分大小写
     * @param input 原字符串
     * @param regex 正则
     */
    public static boolean containsIgnoreCase(String input, String regex) {
        if(isNullOrEmpty(input))
            return false;
        Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(input);
        return m.find();
    }

    /**
     * 替换正则表达式特殊字符
     * @param input 源字符串
     */
    public static String replaceRegexSpecialChar(String input) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            if(ch == '?' || ch == '!' || ch == '\\' || ch == '\'' || ch == '\"' || ch == '^' || ch == '$'|| ch == '[' || ch == ']')
                sb.append('\\');
            else if(ch == '(' || ch == '|' || ch == '*' || ch == '+' || ch == ')' || ch == '.'  || ch == '{' || ch == '}')
                sb.append('\\');
            sb.append(ch);
        }
        return sb.toString();
    }

    private static final Pattern patternIPV6;
    private static final Pattern patternIPV4;

    static {
        // ipv6
        patternIPV6 = Pattern.compile("^((([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){1,7}:)|(([0-9A-Fa-f]{1,4}:){6}:[0-9A-Fa-f]{1,4})|(([0-9A-Fa-f]{1,4}:){5}(:[0-9A-Fa-f]{1,4}){1,2})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){1,3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){1,4})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){1,5})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){1,6})|(:(:[0-9A-Fa-f]{1,4}){1,7})|(([0-9A-Fa-f]{1,4}:){6}(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){4}(:[0-9A-Fa-f]{1,4}){0,1}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){3}(:[0-9A-Fa-f]{1,4}){0,2}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(([0-9A-Fa-f]{1,4}:){2}(:[0-9A-Fa-f]{1,4}){0,3}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|([0-9A-Fa-f]{1,4}:(:[0-9A-Fa-f]{1,4}){0,4}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3})|(:(:[0-9A-Fa-f]{1,4}){0,5}:(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}))$");
        // ipv4
        patternIPV4 = Pattern.compile("^(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}$");
    }

    /**
     * 检测字符串是不是一个有效的IPV4地址
     * @param text 地址字符串
     */
    public static boolean isValidIPV4Address(String text) {
        if (text != null && !text.isEmpty())
            return patternIPV4.matcher(text).matches();
        return false;
    }

    /**
     * 检测字符串是不是一个有效的IPV6地址
     * @param text 地址字符串
     * @return 是不是一个有效的IPV6地址
     */
    public static boolean isValidIPV6Address(String text) {
        if (text != null && !text.isEmpty())
            return patternIPV6.matcher(text).matches();
        return false;
    }
}
