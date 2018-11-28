package com.teambrella.android.ui.chat

import android.content.Context
import android.os.Bundle
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.api.*
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.dagger.ATeambrellaDaggerActivity
import com.teambrella.android.ui.base.uri
import java.util.*

class ChatViewModel : TeambrellaPagerViewModel() {

    override fun init(context: Context, config: Bundle?) {
        dataPager = KChatDataPagerLoader(config?.uri!!, TeambrellaUser.get(context).userId)
        (context as ATeambrellaDaggerActivity<*>).component.inject(dataPager as KChatDataPagerLoader)
        isInit = true
    }


    fun addPendingMessage(postId: String, message: String, vote: Float) {

        val loader = dataPager as KChatDataPagerLoader

        val post = JsonObject().apply {
            userId = loader.userId
            text = message
            stringId = postId
            messageStatus = TeambrellaModel.PostStatus.POST_PENDING
            chatItemType = ChatItems.CHAT_ITEM_MY_MESSAGE
            val currentDate = Calendar.getInstance()
            added = currentDate.time.time

            if (vote >= 0) {
                val teammate = JsonObject()
                teammate.addProperty(TeambrellaModel.ATTR_DATA_VOTE, vote)
                add(TeambrellaModel.ATTR_DATA_ONE_TEAMMATE, teammate)
            }

            val lastDate = loader.getLastDate(true)
            isNextDay = KChatDataPagerLoader.isNextDay(lastDate, currentDate)
        }

        if (post.isNextDay == true) {
            loader.addAsNext(post.deepCopy().apply {
                chatItemType = ChatItems.CHAT_ITEM_DATE
            })
        }

        loader.addAsNext(post)
    }

    fun addPendingImage(postId: String, fileUri: String, ratio: Float, wasCameraUsed: Boolean) {
        val loader = dataPager as KChatDataPagerLoader
        val post = JsonObject().apply {
            userId = loader.userId
            stringId = postId
            messageStatus = TeambrellaModel.PostStatus.POST_PENDING
            imageIndex = 0
            chatItemType = ChatItems.CHAT_ITEM_MY_IMAGE
            cameraUsed = wasCameraUsed
            val currentDate = Calendar.getInstance()
            added = currentDate.time.time

            val images = JsonArray()
            images.add(fileUri)
            add(TeambrellaModel.ATTR_DATA_LOCAL_IMAGES, images)
            val ratios = JsonArray()
            ratios.add(ratio)
            add(TeambrellaModel.ATTR_DATA_IMAGE_RATIOS, ratios)
            val lastDate = loader.getLastDate(true)
            isNextDay = KChatDataPagerLoader.isNextDay(lastDate, currentDate)

        }

        if (post.isNextDay == true) {
            loader.addAsNext(post.deepCopy().apply {
                chatItemType = ChatItems.CHAT_ITEM_DATE
            })
        }

        loader.addAsNext(post)
        loader.updateAnotherImageButton(true)
    }

    fun deleteMyImage(postId: String) {
        val loader = dataPager as KChatDataPagerLoader
        var post = loader
        loader.remove({
            it.asJsonObject.stringId  == postId
        })
        loader.updateAnotherImageButton()
    }

}