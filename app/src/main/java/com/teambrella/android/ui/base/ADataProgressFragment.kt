package com.teambrella.android.ui.base

import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.teambrella.android.R
import com.teambrella.android.data.base.IDataHost

abstract class ADataProgressFragment<T : IDataHost> : ADataFragment<T>() {

    private val contentView: ViewGroup? by ViewHolder(R.id.content)
    private val dataView: ViewGroup? by ViewHolder(R.id.data)
    private val errorView: ViewGroup? by ViewHolder(R.id.error)
    private val refreshable: SwipeRefreshLayout? by ViewHolder(R.id.refreshable)
    private val handler = Handler()


    protected var isRefreshable: Boolean
        get() = refreshable?.isEnabled ?: false
        set(value) {
            refreshable?.isEnabled = value
        }

    var isRefreshing: Boolean
        get() = refreshable?.isRefreshing ?: false
        set(value) {
            refreshable?.isRefreshing = value
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_progress, container, false)
        view.findViewById<ViewGroup>(R.id.data)?.addView(onCreateContentView(inflater, container, savedInstanceState))
        handler.postDelayed(postponedRefreshing, 1000)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        @Suppress("DEPRECATION")
        refreshable?.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        refreshable?.setOnRefreshListener { this@ADataProgressFragment.onReload() }
        setContentShown(false)
    }


    protected fun setContentShown(shown: Boolean, error: Boolean) {
        if (!shown) {
            handler.postDelayed(postponedRefreshing, 1000)
        } else {
            handler.removeCallbacks(postponedRefreshing)
            refreshable?.isRefreshing = false
        }

        contentView?.visibility = if (shown) View.VISIBLE else View.GONE
        dataView?.visibility = if (error) View.GONE else View.VISIBLE
        errorView?.visibility = if (error) View.VISIBLE else View.GONE
    }

    protected fun setContentShown(shown: Boolean) {
        setContentShown(shown, false)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(postponedRefreshing)
    }


    protected abstract fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View


    protected open fun onReload() {
        dataHost.load(tags[0])
    }

    private val postponedRefreshing = Runnable {
        refreshable?.isRefreshing = true
    }
}