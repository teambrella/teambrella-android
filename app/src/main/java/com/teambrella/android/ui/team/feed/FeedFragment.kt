package com.teambrella.android.ui.team.feed

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.teambrella.android.R
import com.teambrella.android.ui.AMainDataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaDataPagerAdapter
import com.teambrella.android.ui.base.TeambrellaDataPagerAdapter

/**
 * Feed Fragment
 */
class FeedFragment : AMainDataPagerProgressFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dividerItemDecoration = object : com.teambrella.android.ui.widget.DividerItemDecoration(view.context.resources.getDrawable(R.drawable.divder)) {
            override fun canDrawChild(view: View, parent: RecyclerView): Boolean {
                var position = parent.getChildAdapterPosition(view)
                var drawDivider = canDrawChild(position, parent)
                if (drawDivider && ++position < parent.adapter?.itemCount ?: 0) {
                    drawDivider = canDrawChild(position, parent)
                }
                return drawDivider
            }

            private fun canDrawChild(position: Int, parent: RecyclerView): Boolean {
                var drawDivider = true
                when (parent.adapter?.getItemViewType(position)) {
                    VIEW_TYPE_HEADER,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_LOADING,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_ERROR,
                    TeambrellaDataPagerAdapter.VIEW_TYPE_BOTTOM -> drawDivider = false
                }
                return drawDivider
            }
        }

        list?.addItemDecoration(dividerItemDecoration)
    }

    override fun createAdapter(): ATeambrellaDataPagerAdapter {
        return KFeedAdapter(dataHost, dataHost.teamId, dataHost.getPager(tags[0]),
                ATeambrellaDataPagerAdapter.OnStartActivityListener { dataHost.launchActivity(it) })
    }
}
