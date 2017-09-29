package com.teambrella.android.blockchain;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import org.bouncycastle.crypto.digests.KeccakDigest;

public final class Sha3 {

    private static final String LOG_TAG = Sha3.class.getSimpleName();

    private Sha3(){}

    public static byte[] getKeccak256Hash(byte[] message){
        Log.v(LOG_TAG, "Calcualting Keccak256 hash for: " + Hex.fromBytes(message));
        byte[] h = calculateHash(message);
        Log.v(LOG_TAG, Hex.fromBytes(h));
        return h;
    }
    public static byte[] calculateHash(byte[] value)
    {
        KeccakDigest digest = new KeccakDigest(256);
        Log.v(LOG_TAG, "----------------------------------------------");
        byte[] output = new byte[digest.getDigestSize()];
        Log.v(LOG_TAG, "sz: " + digest.getDigestSize());
        digest.update(value, 0, value.length);
        Log.v(LOG_TAG, "..............................................");
        digest.doFinal(output, 0);
        Log.v(LOG_TAG, "==============================================");
        return output;
    }
}
