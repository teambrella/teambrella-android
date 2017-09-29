package com.teambrella.android.blockchain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;


public class AbiArguments {

    private static final String LOG_TAG = AbiArguments.class.getSimpleName();
    private static final int BYTES_IN_WORD = 32;
    private static final BigDecimal WEIS_IN_ETH = new BigDecimal(1_000_000_000_000_000_000L);

    private final List<Object> mArgs = new ArrayList<>(7);

    private final List<String> mArgumentsQueue = new ArrayList<String>(7);
    private final List<String> mExtraQueue = new ArrayList<String>(18);
    private int mExtraOffset;


    public static String encodeToHexString(Object... abiArgs){

        AbiArguments b = new AbiArguments();
        for (Object arg : abiArgs){
            b.add(arg);
        }

        return b.encodeToHexString();
    }

    public static String parseDecimalAmount(String decimalAmount){

        BigDecimal e = new BigDecimal(decimalAmount, MathContext.UNLIMITED);
        BigInteger wei = e.multiply(WEIS_IN_ETH).toBigInteger();
        return Hex.format(wei, BYTES_IN_WORD);
    }


    public AbiArguments add(Object arg){

        mArgs.add(arg);
        return this;
    }


    public String encodeToHexString(){

        mArgumentsQueue.clear();
        mExtraQueue.clear();
        mExtraOffset = 0;
        for (Object arg : mArgs) {

            if (arg instanceof Integer) {
                queue((int) arg);

            } else if (arg instanceof Long) {
                queue((long) arg);

            } else if (arg instanceof String) {
                queue((String) arg);

            } else if (arg instanceof String[]) {
                queue((String[]) arg);

            } else if (arg instanceof int[]) {
                queue((int[]) arg);

            } else if (arg instanceof long[]) {
                queue((long[]) arg);

            } else if (arg instanceof byte[]) {
                queue((byte[]) arg);

            } else if (arg == null) {
                queue(0);

            } else {
                throw new IllegalArgumentException("Cannot decode argument of type: " + arg.getClass().getCanonicalName() + ". Use int, long, int[], long[], string, string[], or byte[].");

            }
        }

        return dequeueAll();
    }

    private final static String format(String hex){
        return Hex.format(hex, BYTES_IN_WORD);
    }

    private final static String format(long x){
        return Hex.format(x, BYTES_IN_WORD);
    }

    private void queue(long arg) {

        mArgumentsQueue.add(format(arg));
    }

    private void queue(String arg){

        if (arg.startsWith("0x")) arg = arg.substring(2);

        int len = arg.length() / 2;
        if (len > BYTES_IN_WORD){
            queue(Hex.toBytes(arg));
        }else{
            queue(format(arg));
        }
    }

    private void queue(String[] arg){

        // 0000000040
        queue(getCurrentOffset());

        // 0000000002
        // 0000000AAA
        // 0000000BBB
        queueToExtraPart(arg.length);
        for (String s : arg){
             queueToExtraPart(s);
        }
    }

    private void queue(int[] arg){

        // 0000000040
        queue(getCurrentOffset());

        // 0000000002
        // 0000000111
        // 0000000222
        queueToExtraPart(arg.length);
        for (int x : arg){
            queueToExtraPart(x);
        }
    }

    private void queue(long[] arg){

        // 0000000040
        queue(getCurrentOffset());

        // 0000000002
        // 0000000111
        // 0000000222
        queueToExtraPart(arg.length);
        for (long x : arg){
            queueToExtraPart(x);
        }
    }

    private void queue(byte[] arg){

        // 0000000040
        queue(getCurrentOffset());

        // 0000000013
        // 1234567890
        // 1230000000
        queueToExtraPart(arg.length);
        queueToExtraPart(arg);
    }

    private int getCurrentOffset() {
        return mArgs.size() * BYTES_IN_WORD + mExtraOffset;
    }

    private void queueToExtraPart(long x) {

        mExtraQueue.add(format(x));
        mExtraOffset += BYTES_IN_WORD;
    }

    private void queueToExtraPart(String s) {

        mExtraQueue.add(format(s));
        mExtraOffset += BYTES_IN_WORD;
    }

    private void queueToExtraPart(byte[] byteArray) {

        // [1234567890123] to
        // 1234567890
        // 1230000000
        int n = byteArray.length;
        mExtraQueue.add(Hex.fromBytes(byteArray));
        mExtraOffset += byteArray.length;

        int rest = n % BYTES_IN_WORD;
        if (rest > 0)
        {
            int suffixLen = BYTES_IN_WORD - rest;
            mExtraQueue.add(Hex.format("", suffixLen));
            mExtraOffset += suffixLen;
        }
    }


    private String dequeueAll(){

        StringBuilder hexString = new StringBuilder(getCurrentOffset() * 2);
        for (String arg : mArgumentsQueue){
            hexString.append(arg);
        }

        for (String extra : mExtraQueue){
             hexString.append(extra);
        }

        return hexString.toString();
    }
}
