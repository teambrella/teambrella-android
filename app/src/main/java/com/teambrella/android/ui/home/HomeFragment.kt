package com.teambrella.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.ui.AMainLandingFragment
import com.teambrella.android.ui.base.createDataFragment
import com.teambrella.android.util.ConnectivityUtils
import io.reactivex.Notification


class KHomeFragment : AMainLandingFragment() {


    companion object {
        const val CARDS_FRAGMENT_TAG = "cards_fragment_tag"
        const val COVERAGE_FRAGMENT_TAG = "coverage_fragment_tag"
    }

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private val refreshingRunnable = Runnable {
        swipeRefreshLayout.isRefreshing = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        swipeRefreshLayout = view.findViewById(R.id.refreshable)
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        swipeRefreshLayout.setOnRefreshListener { dataHost.load(tags[0]) }
        return view
    }


    override fun onStop() {
        swipeRefreshLayout.removeCallbacks(refreshingRunnable)
        super.onStop()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        childFragmentManager.beginTransaction().apply {
            if (childFragmentManager.findFragmentByTag(CARDS_FRAGMENT_TAG) == null) {
                add(R.id.top_container, createDataFragment(tags, HomeCardsFragment::class.java)
                        , CARDS_FRAGMENT_TAG)
            }

            if (childFragmentManager.findFragmentByTag(COVERAGE_FRAGMENT_TAG) == null) {
                add(R.id.bottom_container, createDataFragment(tags, HomeCoverageAndWalletFragment::class.java)
                        , COVERAGE_FRAGMENT_TAG)
            }

        }.let {
            if (!it.isEmpty) {
                it.commit()
            }
        }
        swipeRefreshLayout.postDelayed(refreshingRunnable, 1000)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>) {
        super.onDataUpdated(notification)
        swipeRefreshLayout.isRefreshing = false
        swipeRefreshLayout.removeCallbacks(refreshingRunnable)
        if (notification.isOnError) {
            dataHost.showSnackBar(if (ConnectivityUtils.isNetworkAvailable(context!!))
                R.string.something_went_wrong_error else R.string.no_internet_connection)
        }

    }

    var refreshingEnabled: Boolean
        get() = swipeRefreshLayout.isEnabled
        set(value) {
            swipeRefreshLayout.isEnabled = value
        }
}