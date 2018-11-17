package com.teambrella.android.content.model;

import com.teambrella.android.util.log.Log;

import com.teambrella.android.content.TeambrellaRepository;
import com.teambrella.android.util.TeambrellaUtilService;

import org.chalup.microorm.annotations.Column;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Unconfirmed {
  private static final String LOG_TAG = TeambrellaUtilService.class.getSimpleName();

  private Date mDate;

  @Column(TeambrellaRepository.Unconfirmed.ID)
  public long id;

  @Column(TeambrellaRepository.Unconfirmed.MULTISIG_ID)
  public long multisigId;

  @Column(TeambrellaRepository.Unconfirmed.TX_ID)
  public String txId;

  @Column(TeambrellaRepository.Unconfirmed.CRYPTO_TX)
  public String cryptoTx;

  @Column(TeambrellaRepository.Unconfirmed.CRYPTO_FEE)
  public long cryptoFee;

  @Column(TeambrellaRepository.Unconfirmed.CRYPTO_NONCE)
  public long cryptoNonce;

  @Column(TeambrellaRepository.Unconfirmed.DATE_CREATED)
  public String dateCreated;


  public void initDates(SimpleDateFormat sdf){
    try{
      mDate = sdf.parse(dateCreated);
    }catch (ParseException e){
      Log.e(LOG_TAG, "Cannot recognise date format: " + dateCreated, e);
    }
  }

  public void formatDates(SimpleDateFormat sdf) {
    dateCreated = sdf.format(mDate);
  }

  public Date getDateCreated(){
    return mDate;
  }
  public void setDateCreated(Date d){
    mDate = d;
  }
}
