package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

import java.util.List;
import java.util.UUID;

/**
 * Tx
 */
public class Tx {

    @Column(TeambrellaRepository.Tx.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public UUID id;

    @Column(TeambrellaRepository.Tx.TEAMMATE_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATE_ID)
    public long teammateId;

    @Column(TeambrellaRepository.Tx.AMOUNT_CRYPTO)
    @SerializedName(TeambrellaModel.ATTR_DATA_CRYPTO_AMOUNT)
    public String cryptoAmount;

    @Column(TeambrellaRepository.Tx.CLAIM_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_CLAIM_ID)
    public long claimId;

    @Column(TeambrellaRepository.Tx.CLAIM_TEAMMATE_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_CLAIM_TEAMMATE_ID)
    public long claimTeammateId;

    @Column(TeambrellaRepository.Tx.WITHDRAW_REQ_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_WITHDRAW_REQ_ID)
    public long withdrawReqId;

    @Column(TeambrellaRepository.Tx.KIND)
    @SerializedName(TeambrellaModel.ATTR_DATA_KIND)
    public int kind;

    @Column(TeambrellaRepository.Tx.STATE)
    @SerializedName(TeambrellaModel.ATTR_DATA_STATE)
    public int state;

    @Column(TeambrellaRepository.Tx.INITIATED_TIME)
    @SerializedName(TeambrellaModel.ATTR_DATA_INITIATED_TIME)
    public String initiatedTime;

    @Column(TeambrellaRepository.Tx.RECEIVED_TIME)
    public String receivedTime;

    @Column(TeambrellaRepository.Tx.CRYPTO_TX)
    public String cryptoTx;

    @Column(TeambrellaRepository.Tx.RESOLUTION)
    public int resolution;

    public Teammate teammate;

    public Teammate claimTeammate;

    public List<TxInput> txInputs;

    public List<TxOutput> txOutputs;

    public List<Cosigner> cosigners;


    public Multisig getFromMultisig() {
        return this.kind == TeambrellaModel.TX_KIND_SAVE_FROM_PREV_WALLLET ? this.teammate.getPreviousAddress() :
                this.teammate.getCurrentAddress();
    }

}
