package com.teambrella.android.ui.team.feed

import android.os.Bundle
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.uri
import java.lang.IllegalArgumentException

class FeedViewModel : TeambrellaPagerViewModel() {
    override fun getDataPagerLoader(config: Bundle?) = FeedDataPagerLoader(config?.uri
            ?: throw IllegalArgumentException())

    fun markTopicRead(topicId: String) =
            (dataPager as FeedDataPagerLoader).markTopicRead(topicId)

}