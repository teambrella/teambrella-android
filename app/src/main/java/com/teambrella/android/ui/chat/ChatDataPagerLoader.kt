package com.teambrella.android.ui.chat

import android.net.Uri
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.data.base.KTeambrellaChatDataPagerLoader
import com.teambrella.android.util.TeambrellaDateUtils
import com.teambrella.android.util.TimeUtils
import java.util.*

class KChatDataPagerLoader(uri: Uri, val userId: String) : KTeambrellaChatDataPagerLoader(uri) {


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

        while (iterator.hasNext()) {
            val message = iterator.next()!!.asJsonObject
            var text = message.text
            val images = message.images
            val id = message.stringId
            val presentIterator = array.iterator()
            while (presentIterator.hasNext()) {
                val item = presentIterator.next()!!.asJsonObject
                if (item.stringId == id) {
                    message.localImages = item.localImages
                    presentIterator.remove()
                    response.metadata.itemsUpdated = true
                }
                if (item.chatItemType == ChatItems.CHAT_ITEM_PAID_CLAIM) {
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
                                ChatSystemMessages.FIRST_PHOTO_MISSING -> ChatItems.CHAT_ITEM_ADD_PHOTO_TO_JOIN
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