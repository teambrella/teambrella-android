package com.teambrella.android.content.model;

import com.google.gson.annotations.SerializedName;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.content.TeambrellaRepository;

import org.chalup.microorm.annotations.Column;

import java.util.List;


public class Teammate {

    @Column(TeambrellaRepository.Teammate.ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_ID)
    public long id;

    @Column(TeambrellaRepository.Teammate.TEAM_ID)
    @SerializedName(TeambrellaModel.ATTR_DATA_TEAM_ID)
    public long teamId;

    @Column(TeambrellaRepository.Teammate.NAME)
    @SerializedName(TeambrellaModel.ATTR_DATA_NAME)
    public String name;

    @Column(TeambrellaRepository.Teammate.FB_NAME)
    @SerializedName(TeambrellaModel.ATTR_DATA_FB_NAME)
    public String facebookName;

    @Column(TeambrellaRepository.Teammate.PUBLIC_KEY)
    @SerializedName(TeambrellaModel.ATTR_DATA_PUBLIC_KEY)
    public String publicKey;

    @Column(TeambrellaRepository.Teammate.PUBLIC_KEY_ADDRESS)
    @SerializedName(TeambrellaModel.ATTR_DATA_PUBLIC_KEY_ADDRESS)
    public String publicKeyAddress;

    public List<Multisig> multisigs;


    @Column(TeambrellaRepository.Team.PAY_TO_ADDRESS_OK_AGE)
    public int payToAddressOkAge;

    @Column(TeambrellaRepository.Team.AUTO_APPROVAL_MY_GODD_ADDRESS)
    public int autoApprovalMyGoodAddress;

    @Column(TeambrellaRepository.Team.AUTO_APPROVAL_MY_NEW_ADDRESS)
    public int autoApprovalMyNewAddress;

    @Column(TeambrellaRepository.Team.AUTO_APPROVAL_COSIGN_NEW_ADDRESS)
    public int autoApprovalCosignNewAddress;

    @Column(TeambrellaRepository.Team.AUTO_APPROVAL_COSIGN_GOOD_ADDRESS)
    public int getAutoApprovalCosignGoodAddress;


    public Multisig getCurrentAddress() {
        if (this.multisigs != null) {
            for (Multisig address : this.multisigs) {
                if (address.status == TeambrellaModel.USER_MULTISIG_STATUS_CURRENT) {
                    return address;
                }
            }
        }
        return null;
    }

    public Multisig getNextAddress() {
        if (this.multisigs != null) {
            for (Multisig address : this.multisigs) {
                if (address.status == TeambrellaModel.USER_MULTISIG_STATUS_NEXT) {
                    return address;
                }
            }
        }
        return null;
    }


    public Multisig getPreviousAddress() {
        if (this.multisigs != null) {
            for (Multisig address : this.multisigs) {
                if (address.status == TeambrellaModel.USER_MULTISIG_STATUS_PREVIOUS) {
                    return address;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "{id:" + id + ", teamId:" + teamId + ", fb:" + facebookName + ", publicKey:" + publicKey + ", publicKeyAddress:" + publicKeyAddress + "}";
    }

}
