package com.cttest.util;

import java.util.Base64;

public final class Base64Util {

    private Base64Util() {
    }

    public static String encode(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

}
