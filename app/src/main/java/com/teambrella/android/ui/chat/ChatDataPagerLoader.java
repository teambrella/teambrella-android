package com.teambrella.android.ui.chat;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.TeambrellaChatDataPagerLoader;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Claim chat pager loader
 */
public class ChatDataPagerLoader extends TeambrellaChatDataPagerLoader {

    private static final String SPLIT_FORMAT_STRING = "((?<=<img src=\"%1d\">)|(?=<img src=\"%1d\">))";

    private static final String LOG_TAG = ChatDataPagerLoader.class.getSimpleName();

    public ChatDataPagerLoader(Context context, Uri uri) {
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
        JsonArray messages = getPageableData(object);
        JsonObject metadata = new JsonObject();
        metadata.addProperty(TeambrellaModel.ATTR_METADATA_ORIGINAL_SIZE, messages.size());
        object.add(TeambrellaModel.ATTR_METADATA_, metadata);

        object.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject()
                .remove(TeambrellaModel.ATTR_DATA_CHAT);


        JsonArray newMessages = new JsonArray();

        Iterator<JsonElement> it = messages.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) {
            JsonObject srcObject = it.next().getAsJsonObject();
            JsonWrapper message = new JsonWrapper(srcObject);
            String text = message.getString(TeambrellaModel.ATTR_DATA_TEXT);
            JsonArray images = message.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGES);
            Gson gson = new Gson();
            if (text != null && images != null && images.size() > 0) {
                text = text.replaceAll("<p>", "");
                text = text.replaceAll("</p>", "");
                List<String> slices = separate(text.trim(), 0, images.size());
                for (String slice : slices) {
                    JsonObject newObject = gson.fromJson(srcObject, JsonObject.class);
                    newObject.addProperty(TeambrellaModel.ATTR_DATA_TEXT, slice);
                    newMessages.add(newObject);
                }
            } else if (!TextUtils.isEmpty(text)) {
                text = text.replaceAll("<p>", "");
                text = text.replaceAll("</p>", "");
                if (!TextUtils.isEmpty(text)) {
                    JsonObject newObject = gson.fromJson(srcObject, JsonObject.class);
                    newObject.addProperty(TeambrellaModel.ATTR_DATA_TEXT, text);
                    newMessages.add(newObject);
                }
            }
        }

        object.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject()
                .add(TeambrellaModel.ATTR_DATA_CHAT, newMessages);


        return super.postProcess(object);
    }


    private static List<String> separate(String input, int position, int size) {
        List<String> list = new LinkedList<>();

        if (position < size) {
            String[] slices = input.trim().split(String.format(Locale.US, SPLIT_FORMAT_STRING, position, position));
            if (slices.length == 1) {
                if (slices[0].trim().length() > 0) {
                    list.add(slices[0].trim());
                }
            } else {
                for (String slice : slices) {
                    list.addAll(separate(slice, position + 1, size));
                }
            }
        } else {
            if (input.trim().length() > 0) {
                list.add(input.trim());
            }
        }


        return list;
    }
}
