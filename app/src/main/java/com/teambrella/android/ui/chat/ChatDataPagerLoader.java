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
import com.teambrella.android.util.TimeUtils;

import java.util.Calendar;
import java.util.Date;
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

    ChatDataPagerLoader(Context context, Uri uri) {
        super(context, uri);
    }

    @Override
    protected JsonArray getPageableData(JsonObject src) {
        return src.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_CHAT).getAsJsonArray();
    }

    @Override
    protected void addPageableData(JsonObject src, JsonObject item) {
        super.addPageableData(src, item);
        JsonElement data = src.get(TeambrellaModel.ATTR_DATA);
        if (data == null) {
            data = new JsonObject();
            src.add(TeambrellaModel.ATTR_DATA, data);
        }
        JsonElement discussion = data.getAsJsonObject().get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION);
        if (discussion == null) {
            discussion = new JsonObject();
            data.getAsJsonObject().add(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION, discussion);
        }
        JsonElement chat = discussion.getAsJsonObject().get(TeambrellaModel.ATTR_DATA_CHAT);
        if (chat == null) {
            chat = new JsonArray();
            discussion.getAsJsonObject().add(TeambrellaModel.ATTR_DATA_CHAT, chat);
        }
        chat.getAsJsonArray().add(item);
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
        Calendar lastTime = null;

        while (it.hasNext()) {
            JsonObject srcObject = it.next().getAsJsonObject();
            JsonWrapper message = new JsonWrapper(srcObject);
            String text = message.getString(TeambrellaModel.ATTR_DATA_TEXT);
            JsonArray images = message.getJsonArray(TeambrellaModel.ATTR_DATA_IMAGES);


            String id = message.getString(TeambrellaModel.ATTR_DATA_ID);
            Iterator<JsonElement> presentIterator = mArray.iterator();
            while (presentIterator.hasNext()) {
                JsonWrapper item = new JsonWrapper(presentIterator.next().getAsJsonObject());
                if (item.getString(TeambrellaModel.ATTR_DATA_ID) != null
                        && item.getString(TeambrellaModel.ATTR_DATA_ID).equals(id)) {
                    if (item.hasValue(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES)) {
                        srcObject.add(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES, item.getJsonArray(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES));
                    }
                    presentIterator.remove();
                    metadata.addProperty(TeambrellaModel.ATTR_METADATA_ITEMS_UPDATED, true);
                }
            }

            Calendar time = getDate(message);
            if (lastTime == null) {
                lastTime = getLastDate();
            }
            srcObject.addProperty(TeambrellaModel.ATTR_DATA_IS_NEXT_DAY, isNextDay(lastTime, time));
            lastTime = time;

            Gson gson = new Gson();
            if (text != null && images != null && images.size() > 0) {
                text = text.replaceAll("<p>", "");
                text = text.replaceAll("</p>", "");
                List<String> slices = separate(text.trim(), 0, images.size());
                for (String slice : slices) {
                    JsonObject newObject = gson.fromJson(srcObject, JsonObject.class);
                    newObject.addProperty(TeambrellaModel.ATTR_DATA_TEXT, slice);
                    newObject.addProperty(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS, TeambrellaModel.PostStatus.POST_SYNCED);
                    newMessages.add(newObject);
                    srcObject.remove(TeambrellaModel.ATTR_DATA_IS_NEXT_DAY);
                }
            } else if (!TextUtils.isEmpty(text)) {
                text = text.replaceAll("<p>", "");
                text = text.replaceAll("</p>", "");
                if (!TextUtils.isEmpty(text)) {
                    JsonObject newObject = gson.fromJson(srcObject, JsonObject.class);
                    newObject.addProperty(TeambrellaModel.ATTR_DATA_TEXT, text);
                    newObject.addProperty(TeambrellaModel.ATTR_DATA_MESSAGE_STATUS, TeambrellaModel.PostStatus.POST_SYNCED);
                    newMessages.add(newObject);
                }
            }
        }

        object.get(TeambrellaModel.ATTR_DATA).getAsJsonObject()
                .get(TeambrellaModel.ATTR_DATA_ONE_DISCUSSION).getAsJsonObject()
                .add(TeambrellaModel.ATTR_DATA_CHAT, newMessages);

        return super.postProcess(object);
    }

    public Calendar getLastDate() {
        Calendar calendar = Calendar.getInstance();
        JsonElement lastElement = mArray.size() > 0 ? mArray.get(mArray.size() - 1) : null;
        JsonWrapper lastItem = lastElement != null ? new JsonWrapper(lastElement.getAsJsonObject()) : null;
        long created = lastItem != null ? lastItem.getLong(TeambrellaModel.ATTR_DATA_CREATED, 0) : 0;
        if (created > 0) {
            calendar.setTime(TimeUtils.getDateFromTicks(created));
        } else {
            long added = lastItem != null ? lastItem.getLong(TeambrellaModel.ATTR_DATA_ADDED, 0) : 0;
            if (added > 0) {
                calendar.setTime(new Date(added));
            } else {
                calendar.setTime(new Date(0));
            }
        }
        return calendar;
    }

    private Calendar getDate(JsonWrapper item) {
        Calendar calendar = Calendar.getInstance();
        long created = item.getLong(TeambrellaModel.ATTR_DATA_CREATED, 0);
        if (created > 0) {
            calendar.setTime(TimeUtils.getDateFromTicks(created));
        } else {
            long added = item.getLong(TeambrellaModel.ATTR_DATA_ADDED, 0);
            if (added > 0) {
                calendar.setTime(new Date(added));
            }
        }
        return calendar;
    }


    public static boolean isNextDay(Calendar older, Calendar newer) {
        int oldYer = older.get(Calendar.YEAR);
        int newYear = newer.get(Calendar.YEAR);
        int oldDayOfYear = older.get(Calendar.DAY_OF_YEAR);
        int newDayOfYear = newer.get(Calendar.DAY_OF_YEAR);
        return newYear > oldYer || newYear == oldYer && newDayOfYear > oldDayOfYear;
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
