package com.teambrella.android.ui.votes

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.data
import com.teambrella.android.api.me
import com.teambrella.android.api.model.json.JsonWrapper
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter
import com.teambrella.android.ui.widget.DividerItemDecoration
import io.reactivex.Notification

/**
 * All Votes Fragment
 */
class AllVotesFragment : ADataPagerProgressFragment<IAllVoteActivity>() {

    override fun createAdapter(): ATeambrellaDataPagerAdapter {
        var mode = when (TeambrellaUris.sUriMatcher.match(dataHost.uri)) {
            TeambrellaUris.CLAIMS_VOTES -> AllVotesAdapter.MODE_CLAIM
            TeambrellaUris.APPLICATION_VOTES -> AllVotesAdapter.MODE_APPLICATION
            TeambrellaUris.TEAMMATE_CLAIMS_VOTES -> AllVotesAdapter.MODE_TEAMMATE_CLAIMS
            TeambrellaUris.TEAMMATE_RISKS_VOTES -> AllVotesAdapter.MODE_TEAMMATE_RISKS
            else -> -1
        }
        return AllVotesAdapter(dataHost.getPager(tags[0]), dataHost, mode)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = object : DividerItemDecoration(view.context.resources.getDrawable(R.drawable.divder)) {
            override fun canDrawChild(view: View, parent: RecyclerView): Boolean {
                var position = parent.getChildAdapterPosition(view)
                var drawDivider = canDrawChild(position, parent)
                if (drawDivider && ++position < parent.adapter.itemCount) {
                    drawDivider = canDrawChild(position, parent)
                }
                return drawDivider
            }

            private fun canDrawChild(position: Int, parent: RecyclerView): Boolean {
                var drawDivider = true
                when (parent.adapter.getItemViewType(position)) {
                    AllVotesAdapter.VIEW_TYPE_HEADER,
                    AllVotesAdapter.VIEW_TYPE_ME,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_BOTTOM,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_ERROR,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_LOADING -> drawDivider = false
                }
                return drawDivider
            }
        }

        list?.addItemDecoration(dividerItemDecoration)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        super.onDataUpdated(notification)
        notification.takeIf { it.isOnNext }?.value?.data?.me?.let {
            (adapter as AllVotesAdapter).setMyVote(JsonWrapper(it))
        }
    }
}
