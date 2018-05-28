package com.teambrella.android.ui.team.feed

import android.net.Uri
import com.teambrella.android.api.topicId
import com.teambrella.android.api.unreadCount
import com.teambrella.android.data.base.TeambrellaDataPagerLoader

class FeedDataPagerLoader(uri: Uri) : TeambrellaDataPagerLoader(uri, null) {

    fun markTopicRead(topicId: String) {
        loadedData.forEachIndexed { index, item ->
            if (item?.asJsonObject?.topicId == topicId) {
                item.asJsonObject.unreadCount = 0
                notifyItemChange(index)
            }
        }

    }
}