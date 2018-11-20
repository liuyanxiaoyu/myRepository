package com.kingdom.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpUtil {

    /**
     * 获取所有匹配的字符串
     * @param regex
     * @param source
     * @return
     */
    public static List<String> getMatchers(String regex, String source){
        if(regex == null || source == null) return null;

        Matcher matcher = Pattern.compile(regex).matcher(source);
        List<String> list = new ArrayList<>();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * 获取第一个匹配的字符串
     * @param regex
     * @param source
     * @return
     */
    public static String getStartMatcher(String regex, String source){
        if(regex == null || source == null) return null;

        Matcher matcher = Pattern.compile(regex).matcher(source);
        return matcher.find() ? matcher.group(0) : null;
    }

    /**
     * 获取最后一个匹配的字符串
     * @param regex
     * @param source
     * @return
     */
    public static String getEndMatcher(String regex, String source){
        if(regex == null || source == null) return null;

        Matcher matcher = Pattern.compile(regex).matcher(source);
        return matcher.find() ? matcher.group(matcher.end()) : null;
    }

    /**
     * 字符串是否匹配
     * @param regex
     * @param source
     * @return
     */
    public static boolean isMatch(String regex, String source){
        if(regex == null || source == null) return false;

        Matcher matcher = Pattern.compile(regex).matcher(source);
        return matcher.find();
    }
}
