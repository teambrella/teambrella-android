package com.teambrella.android.blockchain;

import com.google.gson.annotations.SerializedName;

public class ScanResultTxReceipt {
    @SerializedName("blockNumber")
    public String blockNumber;

    @SerializedName("gasUsed")
    public String gasUsed;

    @SerializedName("contractAddress")
    public String contractAddress;
}
