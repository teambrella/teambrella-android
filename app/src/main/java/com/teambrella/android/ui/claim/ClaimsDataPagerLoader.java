package com.teambrella.android.ui.claim;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

/**
 * Claims Data Pager Loader
 */
public class ClaimsDataPagerLoader extends TeambrellaDataPagerLoader {


    private static final JsonObject VOTING_HEADER_ITEM = new JsonObject();
    private static final JsonObject VOTED_HEADER_ITEM = new JsonObject();
    private static final JsonObject IN_PAYMENT_HEADER_ITEM = new JsonObject();
    private static final JsonObject PROCESSED_HEADER_ITEM = new JsonObject();


    static {
        VOTING_HEADER_ITEM.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_VOTING_HEADER);
        VOTED_HEADER_ITEM.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_VOTED_HEADER);
        IN_PAYMENT_HEADER_ITEM.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_IN_PAYMENT_HEADER);
        PROCESSED_HEADER_ITEM.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_PROCESSED_HEADER);
    }


    private JsonArray mVotingClaims = new JsonArray();
    private JsonArray mVotedClaims = new JsonArray();
    private JsonArray mProcessingClaims = new JsonArray();
    private JsonArray mProcessedClaims = new JsonArray();


    ClaimsDataPagerLoader(Uri uri) {
        super(uri, null, 50);
    }


    @Override
    protected void onAddNewData(@NonNull JsonArray newData) {
        if (getArray().size() == 0) {
            if (mVotingClaims.size() == 0) {
                //mVotingClaims.add(VOTING_HEADER_ITEM);
            }

            if (mVotedClaims.size() == 0) {
                mVotedClaims.add(VOTED_HEADER_ITEM);
            }

            if (mProcessingClaims.size() == 0) {
                mProcessingClaims.add(IN_PAYMENT_HEADER_ITEM);
            }

            if (mProcessedClaims.size() == 0) {
                mProcessedClaims.add(PROCESSED_HEADER_ITEM);
            }
        }


        for (JsonElement element : newData) {
            JsonObject item = element.getAsJsonObject();
            switch (item.get(TeambrellaModel.ATTR_DATA_STATE).getAsInt()) {
                case TeambrellaModel.ClaimStates.VOTING:
                case TeambrellaModel.ClaimStates.REVOTING:
                    item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_VOTING);
                    mVotingClaims.add(element);
                    break;
                case TeambrellaModel.ClaimStates.VOTED:
                    item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_VOTED);
                    mVotedClaims.add(element);
                    break;
                case TeambrellaModel.ClaimStates.IN_PAYMENT:
                    item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_IN_PAYMENT);
                    mProcessingClaims.add(element);
                    break;
                case TeambrellaModel.ClaimStates.PROCESSEED:
                case TeambrellaModel.ClaimStates.DECLINED:
                    item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ClaimsListItemType.ITEM_PROCESSED);
                    mProcessedClaims.add(element);
                    break;
            }
        }

        if (mVotingClaims.size() > 0) {
            getArray().addAll(mVotingClaims);
            mVotingClaims = new JsonArray();

        }

        if (mVotedClaims.size() > 0) {
            String itemType = mVotedClaims.get(0).getAsJsonObject().get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).getAsString();
            switch (itemType) {
                case TeambrellaModel.ClaimsListItemType.ITEM_VOTED_HEADER:
                    if (mVotedClaims.size() > 1) {
                        getArray().addAll(mVotedClaims);
                        mVotedClaims = new JsonArray();
                    }
                    break;
                case TeambrellaModel.ClaimsListItemType.ITEM_VOTED:
                    getArray().addAll(mVotedClaims);
                    mVotedClaims = new JsonArray();
                    break;
            }
        }


        if (mProcessingClaims.size() > 0) {
            String itemType = mProcessingClaims.get(0).getAsJsonObject().get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).getAsString();
            switch (itemType) {
                case TeambrellaModel.ClaimsListItemType.ITEM_IN_PAYMENT_HEADER:
                    if (mProcessingClaims.size() > 1) {
                        getArray().addAll(mProcessingClaims);
                        mProcessingClaims = new JsonArray();
                    }
                    break;
                case TeambrellaModel.ClaimsListItemType.ITEM_IN_PAYMENT:
                    getArray().addAll(mProcessingClaims);
                    mProcessingClaims = new JsonArray();
                    break;
            }
        }


        if (mProcessedClaims.size() > 0) {
            String itemType = mProcessedClaims.get(0).getAsJsonObject().get(TeambrellaModel.ATTR_DATA_ITEM_TYPE).getAsString();
            switch (itemType) {
                case TeambrellaModel.ClaimsListItemType.ITEM_PROCESSED_HEADER:
                    if (mProcessedClaims.size() > 1) {
                        getArray().addAll(mProcessedClaims);
                        mProcessedClaims = new JsonArray();
                    }
                    break;
                case TeambrellaModel.ClaimsListItemType.ITEM_PROCESSED:
                    getArray().addAll(mProcessedClaims);
                    mProcessedClaims = new JsonArray();
                    break;
            }
        }
    }
}
