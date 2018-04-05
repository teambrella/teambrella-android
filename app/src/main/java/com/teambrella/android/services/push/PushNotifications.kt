@file:JvmName("KPushNotifications")

package com.teambrella.android.services.push

import android.os.Bundle
import com.google.gson.JsonObject
import com.teambrella.android.api.*

const val CREATED_POST = 1
const val DELETED_POST = 2
const val TYPING = 13
const val NEW_CLAIM = 4
const val PRIVATE_MSG = 5
const val WALLET_FUNDED = 6
const val POSTS_SINCE_INTERACTED = 7
const val NEW_TEAMMATE = 8
const val NEW_DISCUSSION = 9
const val TOPIC_MESSAGE_NOTIFICATION = 21
const val DEBUG_DB = 101
const val DEBUG_UPDATE = 102
const val DEBUG_SYNC = 103

private const val CMD = "Cmd"
private const val DEBUG = "Debug"
private const val USER_ID = "UserId"
private const val TEAM_ID = "TeamId"
private const val TOPIC_ID = "TopicId"
private const val POST_ID = "PostId"
private const val USER_NAME = "UserName"
private const val USER_IMAGE = "Avatar"
private const val CONTENT = "Content"
private const val AMOUNT = "Amount"
private const val TEAM_LOGO = "TeamLogo"
private const val TEAM_NAME = "TeamName"
private const val COUNT = "Count"
private const val BALANCE_CRYPTO = "BalanceCrypto"
private const val BALANCE_FIAT = "BalanceFiat"
private const val MESSAGE = "Message"
private const val TEAMMATE_USER_ID = "TeammateUserId"
private const val TEAMMATE_USER_NAME = "TeammateUserName"
private const val TEAMMATE_AVATAR = "TeammateAvatar"
private const val CLAIM_ID = "ClaimClaimId"
private const val CLAIM_USER_NAME = "ClaimUserName"
private const val OBJECT_NAME = "ClaimObjectName"
private const val CLAIM_PHOTO = "ClaimSmallPhoto"
private const val TOPIC_NAME = "TopicName"
private const val DISCUSSION_TOPIC_NAME = "DiscussionTopicName"
private const val MY_TOPIC = "MyTopic"

interface INotificationMessage {

    var cmd: Int
    var teamId: Int
    var senderUserId: String?
    var senderAvatar: String?
    var senderUserName: String?
    var topicId: String?
    var postId: String?
    var content: String?
    var count: Int
    var teammateUserId: String?
    var teammateUserName: String?
    var teammateAvatar: String?
    var claimId: Int
    var claimerName: String?
    var claimPhoto: String?
    var claimObjectName: String?
    var isMyTopic: Boolean
    var topicName: String?
    var discussionTopicName: String?
    var message: String?
    var balanceFiat: String?
    var balanceCrypto: String?
    var teamLogo: String?
    var teamName: String?
    var amount: String?
    var debug: Boolean
}

class FireBaseNotificationMessage(val data: MutableMap<String, String>) : INotificationMessage {

    override var cmd: Int = data[CMD]?.toInt() ?: 0
    override var teamId: Int = data[TEAM_ID]?.toInt() ?: 0
    override var senderUserId: String? = data[USER_ID]
    override var senderAvatar: String? = data[USER_IMAGE]
    override var senderUserName: String? = data[USER_NAME]
    override var topicId: String? = data[TOPIC_ID]
    override var postId: String? = data[POST_ID]
    override var content: String? = data[CONTENT]
    override var count: Int = data[COUNT]?.toInt() ?: 0
    override var teammateUserId: String? = data[TEAMMATE_USER_ID]
    override var teammateUserName: String? = data[TEAMMATE_USER_NAME]
    override var teammateAvatar: String? = data[TEAMMATE_AVATAR]
    override var claimId: Int = data[CLAIM_ID]?.toInt() ?: 0
    override var claimerName: String? = data[CLAIM_USER_NAME]
    override var claimPhoto: String? = data[CLAIM_PHOTO]
    override var claimObjectName: String? = data[OBJECT_NAME]
    override var isMyTopic: Boolean = data[MY_TOPIC]?.toBoolean() ?: false
    override var topicName: String? = data[TOPIC_NAME]
    override var discussionTopicName: String? = data[DISCUSSION_TOPIC_NAME]
    override var message: String? = data[MESSAGE]
    override var balanceFiat: String? = data[BALANCE_FIAT]
    override var balanceCrypto: String? = data[BALANCE_CRYPTO]
    override var teamLogo: String? = data[TEAM_LOGO]
    override var teamName: String? = data[TEAM_NAME]
    override var amount: String? = data[AMOUNT]
    override var debug: Boolean = data[DEBUG]?.toBoolean() ?: false
}


class BundleNotificationMessage(val data: Bundle) : INotificationMessage {

    constructor(message: INotificationMessage) : this(Bundle()) {
        cmd = message.cmd
        teamId = message.teamId
        senderUserId = message.senderUserId
        senderAvatar = message.senderAvatar
        senderUserName = message.senderUserName
        topicId = message.topicId
        postId = message.postId
        content = message.content
        count = message.count
        teammateUserId = message.teammateUserId
        teammateAvatar = message.teammateAvatar
        teammateUserName = message.teammateUserName
        claimId = message.claimId
        claimerName = message.claimerName
        claimObjectName = message.claimObjectName
        claimPhoto = message.claimPhoto
        isMyTopic = message.isMyTopic
        topicName = message.topicName
        discussionTopicName = message.discussionTopicName
        this.message = message.message
        balanceCrypto = message.balanceCrypto
        balanceFiat = message.balanceFiat
        teamLogo = message.teamLogo
        teamName = message.teamName
        amount = message.amount
        debug = message.debug
    }

    override var cmd: Int
        get() = data.getInt(CMD, 0)
        set(value) {
            data.putInt(CMD, value)
        }
    override var teamId: Int
        get() = data.getInt(TEAM_ID, 0)
        set(value) {
            data.putInt(TEAM_ID, value)
        }

    override var senderUserId: String?
        get() = data.getString(USER_ID)
        set(value) {
            data.putString(USER_ID, value)
        }

    override var senderAvatar: String?
        get() = data.getString(USER_IMAGE)
        set(value) {
            data.putString(USER_IMAGE, value)
        }
    override var senderUserName: String?
        get() = data.getString(USER_NAME)
        set(value) {
            data.putString(USER_NAME, value)
        }
    override var topicId: String?
        get() = data.getString(TOPIC_ID)
        set(value) {
            data.putString(TOPIC_ID, value)
        }
    override var postId: String?
        get() = data.getString(POST_ID)
        set(value) {
            data.putString(POST_ID, value)
        }
    override var content: String?
        get() = data.getString(CONTENT)
        set(value) {
            data.putString(CONTENT, value)
        }
    override var count: Int
        get() = data.getInt(COUNT, 0)
        set(value) {
            data.putInt(COUNT, value)
        }
    override var teammateUserId: String?
        get() = data.getString(TEAMMATE_USER_ID)
        set(value) {
            data.putString(TEAMMATE_USER_ID, value)
        }
    override var teammateUserName: String?
        get() = data.getString(TEAMMATE_USER_NAME)
        set(value) {
            data.putString(TEAMMATE_USER_NAME, value)
        }
    override var teammateAvatar: String?
        get() = data.getString(TEAMMATE_AVATAR)
        set(value) {
            data.putString(TEAMMATE_AVATAR, value)
        }
    override var claimId: Int
        get() = data.getInt(CLAIM_ID, 0)
        set(value) {
            data.putInt(CLAIM_ID, value)
        }
    override var claimerName: String?
        get() = data.getString(CLAIM_USER_NAME)
        set(value) {
            data.putString(CLAIM_USER_NAME, value)
        }
    override var claimPhoto: String?
        get() = data.getString(CLAIM_PHOTO)
        set(value) {
            data.putString(CLAIM_PHOTO, value)
        }
    override var claimObjectName: String?
        get() = data.getString(OBJECT_NAME)
        set(value) {
            data.putString(OBJECT_NAME, value)
        }
    override var isMyTopic: Boolean
        get() = data.getBoolean(IS_MY_TOPIC, false)
        set(value) {
            data.putBoolean(IS_MY_TOPIC, value)
        }
    override var topicName: String?
        get() = data.getString(TOPIC_NAME)
        set(value) {
            data.putString(TOPIC_NAME, value)
        }
    override var discussionTopicName: String?
        get() = data.getString(DISCUSSION_TOPIC_NAME)
        set(value) {
            data.putString(DISCUSSION_TOPIC_NAME, value)
        }
    override var message: String?
        get() = data.getString(MESSAGE)
        set(value) {
            data.putString(MESSAGE, value)
        }
    override var balanceFiat: String?
        get() = data.getString(BALANCE_FIAT)
        set(value) {
            data.putString(BALANCE_FIAT, value)
        }
    override var balanceCrypto: String?
        get() = data.getString(BALANCE_CRYPTO)
        set(value) {
            data.putString(BALANCE_CRYPTO, value)
        }
    override var teamLogo: String?
        get() = data.getString(TEAM_LOGO)
        set(value) {
            data.putString(TEAM_LOGO, value)
        }
    override var teamName: String?
        get() = data.getString(TEAM_NAME)
        set(value) {
            data.putString(TEAM_NAME, value)
        }
    override var amount: String?
        get() = data.getString(AMOUNT)
        set(value) {
            data.putString(AMOUNT, value)
        }
    override var debug: Boolean
        get() = data.getBoolean(DEBUG, false)
        set(value) {
            data.putBoolean(DEBUG, value)
        }
}


class SocketNotificationMessage(private val data: JsonObject) : INotificationMessage {
    override var cmd: Int = data.cmd ?: 0
    override var teamId: Int = data.teamId ?: 0
    override var senderUserId: String? = data.userId
    override var senderAvatar: String? = data.userImage
    override var senderUserName: String? = data.userName
    override var topicId: String? = data.topicId
    override var postId: String? = data.postId
    override var content: String? = data.content
    override var count: Int = data.count ?: 0
    override var teammateUserId: String? = data.teammate?.userId
    override var teammateUserName: String? = data.teammate?.userName
    override var teammateAvatar: String? = data.teammate?.avatar
    override var claimId: Int = data.claimId ?: data.claim?.claimId ?: 0
    override var claimerName: String? = data.claim?.userName
    override var claimPhoto: String? = data.claim?.objectPhoto
    override var claimObjectName: String? = data.claim?.objectName
    override var isMyTopic: Boolean = data.isMyTopic ?: false
    override var topicName: String? = data.topicName
    override var discussionTopicName: String? = data.discussion?.topicName
    override var message: String? = data.message
    override var balanceFiat: String? = data.balanceFiat
    override var balanceCrypto: String? = data.balanceCrypto
    override var teamLogo: String? = data.teamLogo
    override var teamName: String? = data.teamName
    override var amount: String? = data.amountStr
    override var debug: Boolean = false
}


