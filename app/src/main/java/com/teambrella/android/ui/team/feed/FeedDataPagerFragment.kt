package com.teambrella.android.ui.team.feed

import android.net.Uri
import android.os.Bundle
import com.google.gson.JsonArray
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.data.base.TeambrellaDataPagerFragment
import com.teambrella.android.data.base.TeambrellaDataPagerLoader
import com.teambrella.android.ui.base.TeambrellaDataHostActivity
import java.lang.RuntimeException

class FeedDataPagerFragment : TeambrellaDataPagerFragment() {

    override fun createLoader(args: Bundle?): IDataPager<JsonArray> {
        var loader: TeambrellaDataPagerLoader? = null
        args?.getParcelable<Uri>(EXTRA_URI)?.let {
            loader = FeedDataPagerLoader(it)
            (context as TeambrellaDataHostActivity).component.inject(loader)
        }
        return loader ?: throw RuntimeException("Feed data pager loader has not been created")
    }

    fun markTopicRead(topicId: String) {
        getLoader()?.markTopicRead(topicId)
    }


    private fun getLoader(): FeedDataPagerLoader? = pager as FeedDataPagerLoader
}