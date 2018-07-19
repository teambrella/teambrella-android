package com.teambrella.android.ui.base

import android.arch.lifecycle.*
import android.content.Context
import android.os.Bundle
import android.view.View
import com.google.gson.JsonObject
import com.teambrella.android.data.base.IDataHost
import io.reactivex.Notification
import kotlin.reflect.KProperty

private const val EXTRA_DATA_TAGS = "extra_data_tags"

fun <T : ADataFragment<*>> createDataFragment(tags: Array<String>, clazz: Class<T>): T {
    return try {
        clazz.newInstance().apply {
            arguments = Bundle().apply {
                putStringArray(EXTRA_DATA_TAGS, tags)
            }
        }
    } catch (e: InstantiationException) {
        throw RuntimeException("unable to create fragment")
    } catch (e: IllegalAccessException) {
        throw RuntimeException("unable to create fragment")
    }
}


abstract class ADataFragment<T : IDataHost> : TeambrellaFragment() {

    protected val tags: Array<String> by lazy(LazyThreadSafetyMode.NONE) {
        arguments?.getStringArray(EXTRA_DATA_TAGS)
                ?: throw IllegalArgumentException("No data tags provided")
    }

    protected val lifecycleOwner = FragmentViewLifecycleOwner()

    protected lateinit var dataHost: T

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        @Suppress("UNCHECKED_CAST")
        dataHost = (context as T) ?: throw IllegalStateException("Data host not found")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleOwner.onViewCreated()
        tags.forEach { dataHost.getObservable(it).observe(lifecycleOwner, observer) }
    }

    override fun onStart() {
        super.onStart()
        lifecycleOwner.onStart()
    }

    override fun onResume() {
        super.onResume()
        lifecycleOwner.onResume()
    }

    override fun onPause() {
        super.onPause()
        lifecycleOwner.onPause()
    }

    override fun onStop() {
        super.onStop()
        lifecycleOwner.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleOwner.onDestroyView()
    }

    private val observer = Observer<Notification<JsonObject>> { _notification ->
        _notification?.let {
            onDataUpdated(it)
        } ?: throw IllegalArgumentException("data is null")
    }


    protected abstract fun onDataUpdated(notification: Notification<JsonObject>)


    protected inner class ViewHolder<T : View>(private val id: Int) : LifecycleObserver {

        init {
            lifecycleOwner.lifecycle.addObserver(this)
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

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun reset() {
            _value = null
        }
    }

    protected class FragmentViewLifecycleOwner : LifecycleOwner {

        private val registry: LifecycleRegistry = LifecycleRegistry(this)
        fun onViewCreated() = registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onDestroyView() = registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onStart() = registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStop() = registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onResume() = registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onPause() = registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        override fun getLifecycle() = registry
    }
}