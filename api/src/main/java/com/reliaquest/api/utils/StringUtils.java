package com.reliaquest.api.utils;

public class StringUtils {
    public static boolean containsString(String sourceString, String searchString) {
        return sourceString.toLowerCase().contains(searchString.toLowerCase());
    }
}
