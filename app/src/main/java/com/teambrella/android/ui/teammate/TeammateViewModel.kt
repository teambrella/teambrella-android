package com.teambrella.android.ui.teammate

import com.teambrella.android.api.data
import com.teambrella.android.api.discussionPart
import com.teambrella.android.api.topicId
import com.teambrella.android.api.unreadCount
import com.teambrella.android.ui.base.TeambrellaDataViewModel

class TeammateViewModel : TeambrellaDataViewModel() {
    fun markTopicRead(topicId: String) {
        loader.update {
            val discussion = it?.data?.discussionPart
            if (discussion?.topicId == topicId) {
                discussion.unreadCount = 0
                return@update true
            }
            return@update false
        }
    }
}