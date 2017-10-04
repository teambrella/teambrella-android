package com.teambrella.android.ui.teammates;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

import java.util.ArrayList;

/**
 * Teammates by Risk Loader
 */
class TeammatesByRiskDataPagerLoader extends TeambrellaDataPagerLoader {

    private final ArrayList<JsonArray> mRiskRanges;

    TeammatesByRiskDataPagerLoader(Context context, Uri uri, ArrayList<RiskRange> ranges) {
        super(context, uri, null, 1000);
        mRiskRanges = new ArrayList<>();
        for (RiskRange range : ranges) {
            JsonArray array = new JsonArray();
            JsonObject object = new JsonObject();
            object.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_RISK);
            object.addProperty(TeambrellaModel.ATTR_DATA_RIGHT_RANGE, range.getRightRange());
            object.addProperty(TeambrellaModel.ATTR_DATA_LEFT_RANGE, range.getLeftRange());
            array.add(object);
            mRiskRanges.add(array);
        }
    }


    @Override
    protected JsonArray getPageableData(JsonObject src) {
        return src.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_TEAMMATES).getAsJsonArray();
    }
}
