package com.teambrella.android.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import com.teambrella.android.BuildConfig
import com.teambrella.android.R
import com.teambrella.android.data.base.IDataHost
import com.teambrella.android.util.log.Log
import io.reactivex.Notification

/**
 * Base data pager progress fragment
 */
abstract class ADataPagerProgressFragment<T : IDataHost> : ADataProgressFragment<T>() {

    protected val list: RecyclerView? by ViewHolder(R.id.list)

    protected val itemTouchHelper: ItemTouchHelper = ItemTouchHelper(ItemTouchCallback())

    protected open val contentLayout: Int
        @LayoutRes
        get() = R.layout.fragment_list

    protected val adapter: ATeambrellaDataPagerAdapter?
        get() = list?.let {
            it.adapter as ATeambrellaDataPagerAdapter
        }


    protected open val isLongPressDragEnabled: Boolean
        get() = false

    protected abstract fun createAdapter(): ATeambrellaDataPagerAdapter


    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = inflater.inflate(contentLayout, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list?.apply {
            layoutManager = object : LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
                override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
                    try {
                        super.onLayoutChildren(recycler, state)
                    } catch (e: Throwable) {
                        if (BuildConfig.DEBUG) {
                            Log.e("TEST", e.toString())
                        }
                    }
                }
            }
            itemTouchHelper.attachToRecyclerView(this)
            adapter = createAdapter().apply {
                component.inject(this)
            }
        }

        val pager = dataHost.getPager(tags[0])
        if (pager.loadedData.size() == 0 && pager.hasNext) {
            pager.reload()
            setContentShown(false)
        } else {
            setContentShown(true)
        }
    }


    override fun onDataUpdated(notification: Notification<JsonObject>) {
        setContentShown(true)
    }

    override fun onReload() {
        dataHost.getPager(tags[0]).reload()
    }

    protected open fun onDraggingFinished(viewHolder: RecyclerView.ViewHolder) {

    }

    override fun onDestroyView() {
        adapter?.destroy()
        super.onDestroyView()
    }

    private inner class ItemTouchCallback internal constructor()
        : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

        override fun isLongPressDragEnabled(): Boolean {
            return this@ADataPagerProgressFragment.isLongPressDragEnabled
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            adapter?.exchangeItems(viewHolder, target)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // nothing to do
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG ->
                    //viewHolder.itemView.setAlpha(0.9f);
                    isRefreshable = false
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            //viewHolder.itemView.setAlpha(1f);
            this@ADataPagerProgressFragment.onDraggingFinished(viewHolder)
            isRefreshable = true
        }
    }

}
