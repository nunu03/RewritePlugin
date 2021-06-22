package com.coofee.rewrite.util;

public class StringUtil {
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String hex(byte[] data) {
        char[] result = new char[data.length * 2];
        int c = 0;
        for (byte b : data) {
            result[c++] = HEX_DIGITS[(b >> 4) & 0xf];
            result[c++] = HEX_DIGITS[b & 0xf];
        }
        return new String(result);
    }

    public static String replace(String origin, char place, char replace) {
        if (isEmpty(origin)) {
            return null;
        }

        return origin.replace(place, replace);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean equals(String lhs, String rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }
        
        return lhs != null && lhs.equals(rhs);
    }
}