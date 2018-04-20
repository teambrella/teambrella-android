package com.teambrella.android.ui.base

import android.view.View
import com.teambrella.android.data.base.IDataHost
import kotlin.reflect.KProperty

abstract class AKDataProgressFragment<T : IDataHost> : ADataProgressFragment<T>() {

    private val mViewHolders = mutableListOf<ViewHolder<*>>()


    override fun onDestroyView() {
        super.onDestroyView()
        mViewHolders.forEach { it.reset() }
    }

    protected inner class ViewHolder<T : View>(private val id: Int) {

        init {
            mViewHolders.add(this)
        }

        private var _value: T? = null

        operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            if (_value == null) {
                _value = view?.findViewById(id)
            }
            return _value
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {

        }

        fun reset() {
            _value = null
        }
    }
}

