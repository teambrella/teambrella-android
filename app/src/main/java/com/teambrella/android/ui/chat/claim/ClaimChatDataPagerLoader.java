package com.teambrella.android.ui.chat.claim;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.TeambrellaChatDataPagerLoader;

import java.util.Iterator;

/**
 * Claim chat pager loader
 */
class ClaimChatDataPagerLoader extends TeambrellaChatDataPagerLoader {

    private static final String LOG_TAG = ClaimChatDataPagerLoader.class.getSimpleName();

    ClaimChatDataPagerLoader(Context context, Uri uri) {
        super(context, uri);
    }

    @Override
    protected JsonArray getPageableData(JsonObject src) {
        return src.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_CHAT).getAsJsonArray();
    }

    @Override
    protected JsonObject postProcess(JsonObject object) {

//        JsonArray messages = getPageableData(object);
//        JsonObject metadata = new JsonObject();
//        metadata.addProperty(TeambrellaModel.ATTR_METADATA_ORIGINAL_SIZE, messages.size());
//        object.add(TeambrellaModel.ATTR_METADATA_, metadata);
//
//        object.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
//                .get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject()
//                .remove(TeambrellaModel.ATTR_DATA_CHAT);
//
//
//        JsonArray newMessages = new JsonArray();
//
//        Iterator<JsonElement> it = messages.iterator();
//        //noinspection WhileLoopReplaceableByForEach
//        while (it.hasNext()) {
//            final String imageReference = "<img src=\"%d\">";
//            JsonObject srcObject = it.next().getAsJsonObject();
//            JsonWrapper message = new JsonWrapper(srcObject);
//            String text = message.getString(TeambrellaModel.ATTR_DATA_TEXT);
//            JsonArray images = message.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGES);
//
//
//            String[] slices = text != null ? text.split("<img src=\"0\">") : null;
//
//            if (slices != null) {
//
//                for (String str : slices) {
//                    Log.d(LOG_TAG, str);
//                }
//            }
//
////
////            if (images.size() > 0) {
////                srcObject.remove(TeambrellaModel.ATTR_DATA_IMAGES);
////                String srcString = text;
////                for (int i = 0; i < images.size(); i++) {
////                    String[] slices = srcString.split(String.format(Locale.US, imageReference, i));
////                    if (slices.length == 2) {
////
////                    }
////                }
////
////            } else {
////                Log.d(LOG_TAG, text);
////            }
//
//        }


        return super.postProcess(object);
    }
}
