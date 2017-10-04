package com.teambrella.android.ui.teammates;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

import java.util.ArrayList;

/**
 * Teammates by Risk Loader
 */
class TeammatesByRiskDataPagerLoader extends TeambrellaDataPagerLoader {

    private final ArrayList<JsonArray> mRiskRanges;
    private final ArrayList<RiskRange> mRanges;

    TeammatesByRiskDataPagerLoader(Context context, Uri uri, ArrayList<RiskRange> ranges) {
        super(context, uri, null, 1000);
        mRiskRanges = new ArrayList<>();
        mRanges = ranges;
    }


    @Override
    protected JsonArray getPageableData(JsonObject src) {
        return src.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_TEAMMATES).getAsJsonArray();
    }


    @Override
    protected void onAddNewData(JsonArray newData) {

        mRiskRanges.clear();

        for (RiskRange range : mRanges) {
            JsonArray array = new JsonArray();
            JsonObject object = new JsonObject();
            object.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_RISK);
            object.addProperty(TeambrellaModel.ATTR_DATA_RIGHT_RANGE, range.getRightRange());
            object.addProperty(TeambrellaModel.ATTR_DATA_LEFT_RANGE, range.getLeftRange());
            array.add(object);
            mRiskRanges.add(array);
        }

        for (JsonElement element : newData) {
            JsonObject object = element.getAsJsonObject();
            JsonWrapper item = new JsonWrapper(object);
            object.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ATTR_DATA_ITEM_TYPE_TEAMMATE);
            float risk = item.getFloat(TeambrellaModel.ATTR_DATA_RISK);
            for (int i = 0; i < mRanges.size(); i++) {
                if (risk >= mRanges.get(i).getLeftRange()
                        && risk <= mRanges.get(i).getRightRange()) {
                    mRiskRanges.get(i).add(object);
                    break;
                }
            }
        }
        for (JsonArray section : mRiskRanges) {
            mArray.addAll(section);
        }
    }
}
