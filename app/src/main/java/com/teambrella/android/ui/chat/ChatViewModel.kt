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

    val loader; get() = dataPager as KChatDataPagerLoader
    
    
    var fullChatModeFromPush = false
        set(value) {
            field = value
            loader.isMarksOnlyMode = isMarksOnlyMode
        }

    var readAll = false
    var userSetMarksOnlyMode: Boolean? = false
        set(value) {
            field = value
            readAll = true
            loader.isMarksOnlyMode = isMarksOnlyMode

//            guard let topicID = chatModel?.discussion.topicID else { return }
//            service.dao.setViewMode(topicID: topicID, useMarksMode: userSetMarksOnlyMode ?? true).observe { result in
//                    switch result {
//                case let .error(error):
//                log("\(error)", type: [.error, .serverReply])
//                default:
        }
    
    val hasEnoughMarks; get() = loader.hasEnoughMarks
    
    var isPinnable = false
        set(value) {
            field = value
            loader.isMarksOnlyMode = isMarksOnlyMode
        }
    
    var isPrivateChat = false
        set(value) {
            field = value
            loader.isMarksOnlyMode = isMarksOnlyMode
        }

    val isMarksOnlyMode: Boolean
        get() {
            return userSetMarksOnlyMode
                    ?: (!isPinnable && !isPrivateChat && (loader.isServerSetMarksOnlyMode ?: true) && hasEnoughMarks && !fullChatModeFromPush)
        }
    
    
    override fun init(context: Context, config: Bundle?) {
        dataPager = KChatDataPagerLoader(config?.uri!!, TeambrellaUser.get(context).userId)
        (context as ATeambrellaDaggerActivity<*>).component.inject(dataPager as KChatDataPagerLoader)
        isInit = true
    }


    fun addPendingMessage(postId: String, message: String, vote: Float) {

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

            val lastDate = loader.getLastDate(false,true)
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
            val lastDate = loader.getLastDate(false,true)
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

    fun deleteMyImage(postId: String?) {
        loader.remove({
            it.asJsonObject.stringId  == postId
        })
        loader.updateAnotherImageButton()
    }

    fun setMyMessageVote(postId: String, vote: Int) {
        loader.updateLikes(postId, vote)
    }

    fun setMarked(postId: String, isMarked: Boolean) {
        loader.setMarked(postId, isMarked)
    }

    fun setProxy(userId: String, add: Boolean, reorder: Boolean) {
        loader.setProxy(userId, add, reorder)
    }
    
    fun saveScrollPosition(position: Int, offset: Int) {
        loader.storedScrollPosition = position
    }
}