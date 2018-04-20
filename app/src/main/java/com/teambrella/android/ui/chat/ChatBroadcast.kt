package com.teambrella.android.ui.chat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager

class ChatBroadCastManager(context: Context) {

    companion object {
        const val ON_MESSAGE_READ_ACTION = "on_message_read_action";
        const val EXTRA_TOPIC_ID = "topicId"
    }

    private val broadCastManager = LocalBroadcastManager.getInstance(context)

    fun registerReceiver(receiver: ChatBroadCastReceiver) {
        broadCastManager.registerReceiver(receiver, IntentFilter(ON_MESSAGE_READ_ACTION))
    }

    fun unregisterReceiver(receiver: ChatBroadCastReceiver) {
        broadCastManager.unregisterReceiver(receiver)
    }

    fun notifyTopicRead(topicId: String) {
        broadCastManager.sendBroadcast(Intent(ON_MESSAGE_READ_ACTION)
                .putExtra(EXTRA_TOPIC_ID, topicId))
    }

}

open class ChatBroadCastReceiver() : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let { _intent ->
            _intent.action?.let { _action ->
                if (_action == ChatBroadCastManager.ON_MESSAGE_READ_ACTION) {
                    val topicId = _intent.getStringExtra(ChatBroadCastManager.EXTRA_TOPIC_ID)
                    topicId?.let { _topicId ->
                        onTopicRead(_topicId)
                    }
                }
            }
        }
    }

    protected open fun onTopicRead(topicId: String) {

    }
}
