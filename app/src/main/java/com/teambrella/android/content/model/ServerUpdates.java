package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;

/**
 * Single update from server
 */
public class ServerUpdates {

  @SerializedName(TeambrellaModel.ATTR_DATA_MULTISIGS)
  public Multisig[] multisigs;

  @SerializedName(TeambrellaModel.ATTR_DATA_COSIGNERS)
  public Cosigner[] cosigners;

  @SerializedName(TeambrellaModel.ATTR_DATA_PAY_TOS)
  public PayTo[] payTos;

  @SerializedName(TeambrellaModel.ATTR_DATA_TEAMS)
  public Team[] teams;

  @SerializedName(TeambrellaModel.ATTR_DATA_TEAMMATES)
  public Teammate[] teammates;

  @SerializedName(TeambrellaModel.ATTR_DATA_TXS)
  public Tx[] txs;

  @SerializedName(TeambrellaModel.ATTR_DATA_TX_INPUTS)
  public TxInput[] txInputs;

  @SerializedName(TeambrellaModel.ATTR_DATA_TX_OUTPUTS)
  public TxOutput[] txOutputs;

  @SerializedName(TeambrellaModel.ATTR_DATA_TX_SIGNATURES)
  public TXSignature[] txSignatures;

}
