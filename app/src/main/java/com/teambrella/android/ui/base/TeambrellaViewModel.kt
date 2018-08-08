package com.teambrella.android.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.google.gson.JsonObject
import com.teambrella.android.data.base.ATeambrellaDataPagerLoader
import com.teambrella.android.data.base.TeambrellaDataLoader
import com.teambrella.android.data.base.TeambrellaDataPagerLoader
import com.teambrella.android.data.base.TeambrellaRequestLoader
import com.teambrella.android.ui.base.dagger.ATeambrellaDaggerActivity
import io.reactivex.Notification

const val EXTRA_URI = "uri"
const val EXTRA_LOAD_ON_CREATE = "load_on_create"
const val EXTRA_PROPERTY = "property"
const val EXTRA_LIMIT = "limit"

var Bundle.uri: Uri?
    get() = this.getParcelable(EXTRA_URI)
    set(value) {
        this.putParcelable(EXTRA_URI, value)
    }

var Bundle.loadOnCreate: Boolean
    get() = this.getBoolean(EXTRA_LOAD_ON_CREATE, false)
    set(value) {
        this.putBoolean(EXTRA_LOAD_ON_CREATE, value)
    }

var Bundle.property: String?
    get() = this.getString(EXTRA_PROPERTY)
    set(value) {
        this.putString(EXTRA_PROPERTY, value)
    }

var Bundle.limit: Int
    get() = this.getInt(EXTRA_LIMIT, 50)
    set(value) {
        this.putInt(EXTRA_LIMIT, value)
    }

abstract class TeambrellaViewModel : ViewModel() {

    var isInit = false


    open fun init(context: Context, config: Bundle?) {
        isInit = true
    }
}

open class TeambrellaDataViewModel : TeambrellaViewModel() {

    protected lateinit var loader: TeambrellaDataLoader
    private var uri: Uri? = null


    val observable: LiveData<Notification<JsonObject>>
        get() = loader.observable

    override fun init(context: Context, config: Bundle?) {
        loader = TeambrellaDataLoader()
        (context as ATeambrellaDaggerActivity<*>).component.inject(loader)
        uri = config?.uri
        if (config?.loadOnCreate == true) {
            load()
        }
        super.init(context, config)
    }

    fun load() {
        uri?.let { loader.load(it, null) }
    }

    fun load(uri: Uri?) {
        uri?.let { loader.load(it, null) }
    }
}

open class TeambrellaPagerViewModel : TeambrellaViewModel() {

    lateinit var dataPager: ATeambrellaDataPagerLoader

    override fun init(context: Context, config: Bundle?) {
        dataPager = getDataPagerLoader(config)
        (context as ATeambrellaDaggerActivity<*>).component.inject(dataPager as TeambrellaDataPagerLoader)
        super.init(context, config)
    }

    open fun getDataPagerLoader(config: Bundle?) = TeambrellaDataPagerLoader(config?.uri
            ?: throw IllegalArgumentException()
            , config.property, config.limit ?: 50)
}


class TeambrellaRequestViewModel : TeambrellaViewModel() {

    private val requestLoader = TeambrellaRequestLoader()

    val observable = requestLoader.observable

    override fun init(context: Context, config: Bundle?) {
        (context as ATeambrellaDaggerActivity<*>).component.inject(requestLoader)
        super.init(context, config)
    }

    fun start() = requestLoader.start()

    fun stop() = requestLoader.stop()

    fun request(uri: Uri?) {
        uri?.let { requestLoader.request(it) }
    }

    fun request(context: Context, privateKey: String, uri: Uri?) {
        uri?.let { requestLoader.request(context, privateKey, uri) }
    }
}