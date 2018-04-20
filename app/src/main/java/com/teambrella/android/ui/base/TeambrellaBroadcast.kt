package com.teambrella.android.ui.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager

class TeambrellaBroadcastManager(context: Context) {

    companion object {
        const val ON_PRIVATE_MESSAGE_READ_ACTION = "on_private_message_read_action"
        const val ON_PUBLIC_MESSAGE_READ_ACTION = "on_message_read_action"
        const val ON_PROXY_LIST_CHANGED = "on_proxy_added_action"
        const val ON_DISCUSSION_STARTED_ACTION = "on_discussion_started_action"
        const val ON_CLAIM_SUBMITTED_ACTION = "on_claim_submitted_action"
        const val ON_CLAIM_VOTE = "on_claim_vote"
        const val EXTRA_TOPIC_ID = "topicId"
        const val EXTRA_CLAIM_ID = "claimId"
        const val EXTRA_USER_ID = "userId"
    }

    private val broadCastManager = LocalBroadcastManager.getInstance(context)

    fun registerReceiver(receiver: TeambrellaBroadcastReceiver) {
        broadCastManager.registerReceiver(receiver, IntentFilter(ON_PRIVATE_MESSAGE_READ_ACTION))
        broadCastManager.registerReceiver(receiver, IntentFilter(ON_PUBLIC_MESSAGE_READ_ACTION))
        broadCastManager.registerReceiver(receiver, IntentFilter(ON_PROXY_LIST_CHANGED))
        broadCastManager.registerReceiver(receiver, IntentFilter(ON_CLAIM_VOTE))
        broadCastManager.registerReceiver(receiver, IntentFilter(ON_CLAIM_SUBMITTED_ACTION))
        broadCastManager.registerReceiver(receiver, IntentFilter(ON_DISCUSSION_STARTED_ACTION))
    }

    fun unregisterReceiver(receiver: TeambrellaBroadcastReceiver) {
        broadCastManager.unregisterReceiver(receiver)
    }

    fun notifyTopicRead(topicId: String) {
        broadCastManager.sendBroadcast(Intent(ON_PUBLIC_MESSAGE_READ_ACTION)
                .putExtra(EXTRA_TOPIC_ID, topicId))
    }

    fun notifyProxyListChanged() {
        broadCastManager.sendBroadcast(Intent(ON_PROXY_LIST_CHANGED))
    }

    fun notifyClaimVote(claimId: Int) {
        broadCastManager.sendBroadcast(Intent(ON_CLAIM_VOTE)
                .putExtra(EXTRA_CLAIM_ID, claimId))
    }

    fun notifyPrivateMessageRead(userId: String) {
        broadCastManager.sendBroadcast(Intent(ON_PRIVATE_MESSAGE_READ_ACTION)
                .putExtra(EXTRA_USER_ID, userId))
    }

    fun notifyNewChatStarted() {
        broadCastManager.sendBroadcast(Intent(ON_DISCUSSION_STARTED_ACTION))
    }

    fun notifyClaimSubmitted() {
        broadCastManager.sendBroadcast(Intent(ON_CLAIM_SUBMITTED_ACTION))
    }

}

open class TeambrellaBroadcastReceiver() : BroadcastReceiver() {
    final override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { _intent ->
            _intent.action?.let { _action ->
                when (_action) {
                    TeambrellaBroadcastManager.ON_PUBLIC_MESSAGE_READ_ACTION -> {
                        val topicId = _intent.getStringExtra(TeambrellaBroadcastManager.EXTRA_TOPIC_ID)
                        topicId?.let { _topicId ->
                            onTopicRead(_topicId)
                        }
                    }
                    TeambrellaBroadcastManager.ON_PROXY_LIST_CHANGED -> onProxyListChanged()
                    TeambrellaBroadcastManager.ON_CLAIM_VOTE -> {
                        val claimId = _intent.getIntExtra(TeambrellaBroadcastManager.EXTRA_CLAIM_ID, -1)
                        if (claimId > 0) {
                            onClaimVote(claimId)
                        } else Unit
                    }
                    TeambrellaBroadcastManager.ON_PRIVATE_MESSAGE_READ_ACTION -> {
                        val userId = _intent.getStringExtra(TeambrellaBroadcastManager.EXTRA_USER_ID)
                        userId?.let { _userId ->
                            onPrivateMessageRead(_userId)
                        }
                    }
                    TeambrellaBroadcastManager.ON_DISCUSSION_STARTED_ACTION -> onDiscussionStarted()
                    TeambrellaBroadcastManager.ON_CLAIM_SUBMITTED_ACTION -> onClaimSubmitted()
                    else -> {

                    }
                }
            }
        }
    }

    protected open fun onTopicRead(topicId: String) = Unit
    protected open fun onProxyListChanged() = Unit
    protected open fun onClaimVote(claimId: Int) = Unit
    protected open fun onPrivateMessageRead(userId: String) = Unit
    protected open fun onDiscussionStarted() = Unit
    protected open fun onClaimSubmitted() = Unit
}

open class TeambrellaActivityBroadcastReceiver() : TeambrellaBroadcastReceiver(), TeambrellaActivityLifecycle {

    @Suppress("MemberVisibilityCanBePrivate")
    protected var resumed = false
    protected var started = false

    override fun onCreate(context: Context, savedInstanceState: Bundle?) {
        TeambrellaBroadcastManager(context).registerReceiver(this)
    }

    override fun onStart() {
        started = true
    }

    override fun onResume() {
        resumed = true
    }

    override fun onSaveInstanceState(outState: Bundle) = Unit

    override fun onPause() {
        resumed = false
    }

    override fun onStop() {
        started = false
    }

    override fun onDestroy(context: Context) {
        TeambrellaBroadcastManager(context).unregisterReceiver(this)
    }
}
