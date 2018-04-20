package com.teambrella.android.blockchain;

import com.google.gson.annotations.SerializedName;

/**
 * Explorer Utxo
 */
public class ExplorerUtxo {
    @SerializedName("address")
    String address;
    @SerializedName("txid")
    String txId;
    @SerializedName("vout")
    int vout;
    @SerializedName("ts")
    long ts;
    @SerializedName("scriptPubKey")
    String scriptPubKey;
    @SerializedName("amount")
    float amount;
    @SerializedName("confirmation")
    int confirmation;
}
