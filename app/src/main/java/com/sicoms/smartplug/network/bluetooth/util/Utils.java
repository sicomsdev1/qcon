package com.sicoms.smartplug.network.bluetooth.util;

public class Utils {
	
	
	
	static String hexString(byte [] value) {
        if (value == null) return "null";        
        String out = "";
        for (byte b : value) {
            out += String.format("%02x", b);
        }
        return out;
    }

}
