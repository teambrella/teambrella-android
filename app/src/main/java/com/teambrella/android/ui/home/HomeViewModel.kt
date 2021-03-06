package com.teambrella.android.ui.home

import com.teambrella.android.api.cards
import com.teambrella.android.api.data
import com.teambrella.android.api.topicId
import com.teambrella.android.api.unreadCount
import com.teambrella.android.ui.base.TeambrellaDataViewModel

class HomeViewModel : TeambrellaDataViewModel() {
    fun markTopicRead(topicId: String) {
        loader.update {
            var updated = false
            it?.data?.cards?.forEach { element ->
                if (element?.asJsonObject?.topicId == topicId) {
                    element.asJsonObject.unreadCount = 0
                    updated = true
                }
            }
            return@update updated
        }
    }
}