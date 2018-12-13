package com.teambrella.android.ui.chat

import android.net.Uri
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.data.base.KTeambrellaChatDataPagerLoader
import com.teambrella.android.ui.chat.KChatDataPagerLoader.Companion.separate
import com.teambrella.android.util.TeambrellaDateUtils
import com.teambrella.android.util.TimeUtils
import com.teambrella.android.util.log.Log
import io.reactivex.Notification
import io.reactivex.Observable
import java.util.*

class KChatDataPagerLoader(uri: Uri, val userId: String) : KTeambrellaChatDataPagerLoader(uri) {

    private val LOG_TAG = KChatDataPagerLoader::class.java.simpleName

    companion object {

        private const val SPLIT_FORMAT_STRING = "((?<=<img src=\"%1d\">)|(?=<img src=\"%1d\">))"
        private const val IMAGE_FORMAT_STRING = "<img src=\"%d\">"


        fun isNextDay(older: Calendar, newer: Calendar): Boolean {
            val oldYer = older.get(Calendar.YEAR)
            val newYear = newer.get(Calendar.YEAR)
            val oldDayOfYear = older.get(Calendar.DAY_OF_YEAR)
            val newDayOfYear = newer.get(Calendar.DAY_OF_YEAR)
            return newYear > oldYer || newYear == oldYer && newDayOfYear > oldDayOfYear
        }


        private fun String.separate(position: Int, size: Int): List<String> {
            val list = LinkedList<String>()

            if (position < size) {
                val slices = this.trim { it <= ' ' }
                        .split(String.format(Locale.US, SPLIT_FORMAT_STRING, position, position).toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                if (slices.size == 1) {
                    if (slices[0].trim { it <= ' ' }.isNotEmpty()) {
                        list.add(slices[0].trim { it <= ' ' })
                    }
                } else {
                    for (slice in slices) {
                        list.addAll(slice.separate(position + 1, size))
                    }
                }
            } else {
                if (this.trim { it <= ' ' }.isNotEmpty()) {
                    list.add(this.trim { it <= ' ' })
                }
            }


            return list
        }

        private fun getDate(item: JsonObject): Calendar {
            val calendar = Calendar.getInstance()
            val created = item.created ?: 0L
            if (created > 0) {
                calendar.time = TimeUtils.getDateFromTicks(created)
            } else {
                val added = item.added ?: 0L
                if (added > 0) {
                    calendar.time = Date(added)
                }
            }
            return calendar
        }

    }

    private var cachedVotingPart: JsonObject? = null


    override fun getPageableData(src: JsonObject) = src.data?.discussionPart?.chat ?: JsonArray()

    override fun addPageableData(src: JsonObject, item: JsonObject) {
        val data = src.data ?: JsonObject()
        src.data = data
        val discussion = data.discussionPart ?: JsonObject()
        data.discussionPart = discussion
        val chat = discussion.chat ?: JsonArray()

        data.voting = cachedVotingPart

        chat.add(item)
    }


    public fun canUpdateAnotherImageButton(dontCareIfHasAnImageAlready: Boolean = false): Boolean {
        var awaitsNewImages = false
        var hasAnImageAlready = false
        array.forEach() {
            val message = it.asJsonObject;
            if (message.systemType == ChatSystemMessages.FIRST_PHOTO_MISSING) {
                awaitsNewImages = true
            }
            if (message.images?.size()?:0 > 0 || message.localImages?.size()?:0 > 0) {
                hasAnImageAlready = true
            }
        }
        return awaitsNewImages && (dontCareIfHasAnImageAlready || hasAnImageAlready)
    }

    public fun updateAnotherImageButton(forced: Boolean = false) {
        if (canUpdateAnotherImageButton(forced)) {
            remove({
                it.asJsonObject.chatItemType == ChatItems.CHAT_ITEM_ANOTHER_PHOTO_TO_JOIN
            })
            addAsNext(JsonObject().apply {
                chatItemType = ChatItems.CHAT_ITEM_ANOTHER_PHOTO_TO_JOIN
                val currentDate = Calendar.getInstance()
                added = currentDate.time.time
            })
        }
    }

    public fun updateLikes(postId: String, myLike: Int) {
        val posOfOldItem = array.indexOfFirst { it.asJsonObject.stringId == postId
                && it.asJsonObject.chatItemType != ChatItems.CHAT_ITEM_DATE}

        val oldItem = array[posOfOldItem]?.asJsonObject
        oldItem?.let {
            val newMessage = it.deepCopy()
            var likesDiff = myLike - (it.myLike?:0)
            newMessage.myLike = myLike
            newMessage.likes = (it.likes?:0) + likesDiff
            array.set(posOfOldItem, newMessage)
            Log.v(LOG_TAG, "(updateLikes) Updating: $postId at $posOfOldItem")

            val response = JsonObject().apply {
                metadata = JsonObject().apply {
                    itemDeleted = true
                }
                data = JsonObject().apply {
                    discussionPart = JsonObject()
                }
            }
            _observable.postValue(Notification.createOnNext(response))
        }
    }


    override fun postProcess(response: JsonObject, next: Boolean): JsonObject {
        val messages = getPageableData(response)

        val data = response.data
        val basic = data?.basic
        val paymentDateString = basic?.datePaymentFinished

        var paymentDate = if (paymentDateString != null) Calendar.getInstance().apply {
            time = TeambrellaDateUtils.getDate(paymentDateString)
        } else null


        val claimId = basic?.claimId
        val team = data?.teamPart
        val teamId = team?.teamId
        val teamCoverageType = team?.coverageType

        cachedVotingPart = data?.voting


        response.metadata = JsonObject().apply {
            originalSize = messages.size()
        }

        response.data?.discussionPart?.chat = null
        val newMessages = JsonArray()

        val iterator = messages.iterator()
        var lastTime: Calendar? = null


        fun appendPaidClaimItem() {
            newMessages.add(JsonObject().apply {
                chatItemType = ChatItems.CHAT_ITEM_PAID_CLAIM
                sharedUrl = Uri.Builder().scheme("https")
                        .authority(TeambrellaServer.AUTHORITY)
                        .appendPath("claim")
                        .appendPath(teamId.toString())
                        .appendPath(claimId.toString())
                        .build().toString()
                coverageType = teamCoverageType
                this.claimId = claimId
            })
        }

        fun appendDateItem(item: JsonObject): Boolean {
            if (item.isNextDay == true) {
                newMessages.add(item.deepCopy().apply {
                    chatItemType = ChatItems.CHAT_ITEM_DATE
                })
                item.isNextDay = null
                return true
            }
            return false
        }

        fun getMyLastImageMessage(): JsonObject {
            var myImageMessage = JsonObject()
            val iterator = messages.iterator()
            while (iterator.hasNext()) {
                val message = iterator.next()!!.asJsonObject
                val images = message.images
                if (images != null && images.size() > 0 && userId == message.userId) {
                    myImageMessage = message
                }
            }
            return myImageMessage
        }

        val myLastImageMessage = getMyLastImageMessage()
        var awaitsNewImages = canUpdateAnotherImageButton()

        while (iterator.hasNext()) {
            val message = iterator.next()!!.asJsonObject
            var text = message.text
            val images = message.images
            val id = message.stringId

            val posOfOldItem = array.indexOfFirst { it.asJsonObject.stringId == id
                    && it.asJsonObject.chatItemType != ChatItems.CHAT_ITEM_DATE}
            if (posOfOldItem >= 0)  {
                val oldItem = array[posOfOldItem].asJsonObject
                if (array[posOfOldItem].asJsonObject.lastUpdated < message.lastUpdated) {
                    val newMessage = message.deepCopy()
                    newMessage.localImages = oldItem.localImages
                    newMessage.chatItemType = oldItem.chatItemType
                    newMessage.isNextDay = oldItem.isNextDay
                    newMessage.imageIndex = oldItem.imageIndex
                    newMessage.messageStatus = TeambrellaModel.PostStatus.POST_SYNCED
                    array.set(posOfOldItem, newMessage)
                    Log.v(LOG_TAG, "(postProcess) Updating: $id at $posOfOldItem")
                    response.metadata.itemDeleted = true
                }
                continue // nothing to see here, move along
            }

            array.forEach {
                if (it.asJsonObject.chatItemType == ChatItems.CHAT_ITEM_PAID_CLAIM) {
                    paymentDate = null
                }
            }

            val time = getDate(message)
            lastTime = lastTime ?: getLastDate(next)
            paymentDate?.let {
                if ((lastTime?.time?.time ?: 0L) > 0 && it > lastTime && it < time) {
                    if (appendDateItem(JsonObject().apply {
                                created = TimeUtils.getTicksFromDate(it.time)
                                isNextDay = isNextDay(lastTime as Calendar, it)
                            })) {
                        lastTime = it
                    }
                    appendPaidClaimItem()
                    paymentDate = null
                }
            }

            message.isNextDay = ((lastTime?.time?.time
                    ?: 0L > 0) || !next && lastTime?.time?.time ?: 0L == 0L) && isNextDay(lastTime as Calendar, time)


            if (text != null && images != null && images.size() > 0) {
                text.removeParagraphs().separate(0, images.size())
                        .forEach { slice ->
                            val newMessage = message.deepCopy()

                            for (index in 0 until images.size()) {
                                if (slice == String.format(Locale.US, IMAGE_FORMAT_STRING, index)) {
                                    newMessage.imageIndex = index
                                    newMessage.chatItemType =
                                            if (userId == newMessage.userId) ChatItems.CHAT_ITEM_MY_IMAGE
                                            else ChatItems.CHAT_ITEM_IMAGE
                                    break
                                }
                            }

                            if (newMessage.imageIndex == null) {
                                newMessage.chatItemType =
                                        if (userId == newMessage.userId) ChatItems.CHAT_ITEM_MY_MESSAGE
                                        else ChatItems.CHAT_ITEM_MESSAGE
                                newMessage.text = slice
                            }

                            newMessage.messageStatus = TeambrellaModel.PostStatus.POST_SYNCED
                            appendDateItem(message)
                            newMessages.add(newMessage)
                        }

            } else if (text != null && text.isNotEmpty()) {
                text = text.removeParagraphs()
                if (text.isNotEmpty()) {
                    val newMessage = message.deepCopy()
                    newMessage.text = text
                    newMessage.messageStatus = TeambrellaModel.PostStatus.POST_SYNCED
                    newMessage.chatItemType =
                            when (newMessage.systemType) {
                                ChatSystemMessages.FIRST_MESSAGE_MISSING -> ChatItems.CHAT_ITEM_ADD_MESSAGE_TO_JOIN
                                ChatSystemMessages.FIRST_PHOTO_MISSING -> {
                                    awaitsNewImages = true
                                    ChatItems.CHAT_ITEM_ADD_PHOTO_TO_JOIN
                                }
                                ChatSystemMessages.NEEDS_FUNDING -> ChatItems.CHAT_ITEM_PAY_TO_JOIN
                                else -> {
                                    if (userId == newMessage.userId) ChatItems.CHAT_ITEM_MY_MESSAGE
                                    else ChatItems.CHAT_ITEM_MESSAGE
                                }
                            }
                    appendDateItem(message)
                    newMessages.add(newMessage)
                }
            }

            if (message.stringId == myLastImageMessage.stringId && awaitsNewImages) {
                remove({
                    it.asJsonObject.chatItemType == ChatItems.CHAT_ITEM_ANOTHER_PHOTO_TO_JOIN
                }, false)
                newMessages.add(JsonObject().apply {
                    chatItemType = ChatItems.CHAT_ITEM_ANOTHER_PHOTO_TO_JOIN
                    val currentDate = Calendar.getInstance()
                    added = currentDate.time.time
                })
            }

            lastTime = paymentDate?.let {
                if (!iterator.hasNext() && it > time) {
                    appendDateItem(JsonObject().apply {
                        created = TimeUtils.getTicksFromDate(it.time)
                        isNextDay = isNextDay(time, it)
                    })
                    appendPaidClaimItem()
                    paymentDate = null
                    it
                } else {
                    time
                }
            } ?: time
        }

        if (!next && array.size() > 0) {
            val first = array.get(0).asJsonObject
            lastTime = lastTime ?: Calendar.getInstance().apply {
                time = Date(0)
            }
            first.isNextDay = isNextDay(lastTime as Calendar, getDate(first))
            appendDateItem(first)
        }

        response.data?.discussionPart?.chat = newMessages
        return super.postProcess(response, next)
    }

    fun getLastDate(next: Boolean): Calendar {
        val calendar = Calendar.getInstance()
        val lastElement = if (array.size() > 0) array[array.size() - 1] else null
        val lastItem = lastElement?.asJsonObject
        val created = lastItem?.created ?: 0L
        if (!next) {
            calendar.time = Date(0)
        } else if (created > 0) {
            calendar.time = TimeUtils.getDateFromTicks(created)
        } else {
            val added = lastItem?.added ?: 0L
            if (added > 0) {
                calendar.time = Date(added)
            } else {
                calendar.time = Date(0)
            }
        }
        return calendar
    }

    private fun String.removeParagraphs(): String {
        return this.replace("<p>".toRegex(), "")
                .replace("</p>".toRegex(), "")
                .trim { it <= ' ' }
    }

}