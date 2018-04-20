package com.teambrella.android.blockchain;

import com.teambrella.android.util.log.Log;

import org.bouncycastle.crypto.digests.KeccakDigest;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Hex {

    private static final String LOG_TAG = Hex.class.getSimpleName();
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private Hex(){}


    public static String fromBytes(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int b = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[b >>> 4];
            hexChars[j * 2 + 1] = hexArray[b & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] toBytes(String hexString) {
        int len = hexString.length();
        byte[] res = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            res[i / 2] = (byte) (
                    (Character.digit(hexString.charAt(i), 16) << 4) +
                            Character.digit(hexString.charAt(i+1), 16)
            );
        }
        return res;
    }

    public static byte[] toBytes(Object... hexOrBytesArgs) {

        ByteArrayOutputStream res = new ByteArrayOutputStream();
        for (Object arg : hexOrBytesArgs) {
            byte[] bbb;

            if (arg instanceof String) {
                bbb = toBytes((String) arg);
                res.write(bbb, 0, bbb.length);

            } else if (arg instanceof String[]) {
                for (String s : (String[]) arg) {
                    bbb = toBytes(s);
                    res.write(bbb, 0, bbb.length);
                }

            } else if (arg instanceof byte[]) {
                bbb = (byte[]) arg;
                res.write(bbb, 0, bbb.length);

            } else {
                throw new IllegalArgumentException("" + arg);
            }

        }
        return res.toByteArray();
    }

    public static String format(long x, int fixedSizeInBytes){

        // 777 to "00000000000000000777"
        return String.format("%0" + (fixedSizeInBytes * 2) + "x", x);
    }

    public static String format(BigInteger x, int fixedSizeInBytes){

        // 777 to "00000000000000000777"
        return String.format("%0" + (fixedSizeInBytes * 2) + "x", x);
    }

    public static String format(String hex, int fixedSizeInBytes){

        // "0xABCDEF" to "00000000000000000ABCDEF"
        hex = remove0xPrefix(hex);

        int len = hex.length() / 2;
        if (len > fixedSizeInBytes){
            throw new IllegalArgumentException("Hex string exceeds size of: " + fixedSizeInBytes + ". The string was: " + hex);
        }

        byte[] prefix = new byte[fixedSizeInBytes - len];
        return fromBytes(prefix) + hex;
    }

    public static final String remove0xPrefix(String hex){
        return hex.startsWith("0x") ? hex.substring(2) : hex;
    }
}
