package com.teambrella.android.ui.chat

import android.arch.lifecycle.LiveData
import android.net.Uri

import com.google.gson.JsonObject
import com.teambrella.android.data.base.IDataHost
import com.teambrella.android.ui.base.ITeambrellaDaggerActivity

import io.reactivex.Notification

/**
 * Chat Activity
 */
interface IChatActivity : IDataHost, ITeambrellaDaggerActivity {
	
	val teamId: Int
	val teammateId: Int
	val chatUri: Uri
	val claimId: Int
	val objectName: String
	val userId: String
	val userName: String?
	val imageUri: String
	
	val muteStatus: MuteStatus?
	
	val pinTopicObservable: LiveData<Notification<JsonObject>>
	
	enum class MuteStatus {
		DEFAULT,
		MUTED,
		UMMUTED
	}
	
	fun setChatMuted(muted: Boolean)
	fun pinTopic()
	fun unpinTopic()
	fun resetPin()
	
	fun setMyMessageVote(postId: String, vote: Int)
	fun setMarkedPost(postId: String, isMarked: Boolean?)
	fun setMainProxy(userId: String)
	fun addProxy(userId: String)
	fun removeProxy(userId: String)
}
