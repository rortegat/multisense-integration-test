package com.example.multisensepruebas.util;

public class MacAddressUtils {
    public static Long toLong(String macAddress) {
        macAddress = macAddress.replace(".", "");
        return Long.parseLong(macAddress, 16);
    }
}
