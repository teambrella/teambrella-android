package com.teambrella.android.ui.team.teammates;

import android.content.Context;
import android.net.Uri;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

/**
 * Teammates Data Pager Loader
 */
public class TeammatesDataPagerLoader extends TeambrellaDataPagerLoader {


    private static final JsonObject NEW_MEMBERS_SECTION = new JsonObject();
    private static final JsonObject TEAMMATES_SECTION = new JsonObject();


    static {
        NEW_MEMBERS_SECTION.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_NEW_MEMBERS);
        TEAMMATES_SECTION.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ATTR_DATA_ITEM_TYPE_SECTION_TEAMMATES);
    }

    private JsonArray mNewMembers;
    private JsonArray mTeammates;

    TeammatesDataPagerLoader(Context context, Uri uri) {
        super(context, uri, null);
        mNewMembers = new JsonArray();
        mTeammates = new JsonArray();
    }

    @Override
    protected JsonArray getPageableData(JsonObject src) {
        return new JsonWrapper(src).getObject(TeambrellaModel.ATTR_DATA).getJsonArray(TeambrellaModel.ATTR_DATA_TEAMMATES);
    }


    @Override
    protected void onAddNewData(JsonArray newData) {

        if (mArray.size() == 0) {
            if (mNewMembers.size() == 0) {
                mNewMembers.add(NEW_MEMBERS_SECTION);
            }

            if (mTeammates.size() == 0) {
                mTeammates.add(TEAMMATES_SECTION);
            }
        }

        for (JsonElement element : newData) {
            JsonObject item = element.getAsJsonObject();
            item.addProperty(TeambrellaModel.ATTR_DATA_ITEM_TYPE, TeambrellaModel.ATTR_DATA_ITEM_TYPE_TEAMMATE);
            if (item.get(TeambrellaModel.ATTR_DATA_IS_VOTING).getAsBoolean()) {
                mNewMembers.add(item);
            } else {
                mTeammates.add(item);
            }
        }

        if (mNewMembers.size() > 1) {
            mArray.addAll(mNewMembers);
            mNewMembers = new JsonArray();
        }

        if (mTeammates.size() > 1) {
            mArray.addAll(mTeammates);
            mTeammates = new JsonArray();
        }
    }
}
