package com.teambrella.android.ui.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.dagger.ContextModule
import com.teambrella.android.dagger.TeambrellaImageLoaderModule
import com.teambrella.android.dagger.TeambrellaServerModule
import com.teambrella.android.dagger.TeambrellaUserModule
import com.teambrella.android.data.base.IDataHost
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.ui.base.dagger.ATeambrellaDaggerActivity
import io.reactivex.Notification
import io.reactivex.disposables.Disposable

@JvmOverloads
fun getDataConfig(uri: Uri? = null, loadOnCreate: Boolean = false) = Bundle().apply {
    this.uri = uri
    this.loadOnCreate = loadOnCreate
}

@JvmOverloads
fun getPagerConfig(uri: Uri, property: String? = null, limit: Int = 50) = Bundle().apply {
    this.uri = uri
    this.property = property
    this.limit = limit
}


abstract class ATeambrellaDataHostActivity : ATeambrellaDaggerActivity<ITeambrellaComponent>(), IDataHost {

    private var requestDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataTags.forEach { tag ->
            ViewModelProviders.of(this).get(tag, getDataViewModelClass(tag)
                    ?: TeambrellaDataViewModel::class.java).apply {
                if (!isInit) {
                    init(this@ATeambrellaDataHostActivity, getDataConfig(tag))
                }
            }
        }

        dataPagerTags.forEach { tag ->
            ViewModelProviders.of(this).get(tag, getPagerViewModelClass(tag)
                    ?: TeambrellaPagerViewModel::class.java).apply {
                if (!isInit) {
                    init(this@ATeambrellaDataHostActivity, getDataPagerConfig(tag))
                }
            }
        }

        if (isRequestable) {
            ViewModelProviders.of(this).get(TeambrellaRequestViewModel::class.java).apply {
                if (!isInit) {
                    init(this@ATeambrellaDataHostActivity, null)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (isRequestable) {
            ViewModelProviders.of(this).get(TeambrellaRequestViewModel::class.java).apply {
                requestDisposable = observable.subscribe(this@ATeambrellaDataHostActivity::onRequestResult)
                start()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRequestable) {
            requestDisposable?.takeIf { !it.isDisposed }?.dispose()
            ViewModelProviders.of(this).get(TeambrellaRequestViewModel::class.java).stop()
        }
    }

    override fun getObservable(tag: String): LiveData<Notification<JsonObject>> =
            when {
                dataPagerTags.contains(tag) -> ViewModelProviders.of(this).get(tag, getPagerViewModelClass(tag)
                        ?: TeambrellaPagerViewModel::class.java).dataPager.dataObservable
                dataTags.contains(tag) -> ViewModelProviders.of(this).get(tag, getDataViewModelClass(tag)
                        ?: TeambrellaDataViewModel::
                        class.java).observable

                else -> throw IllegalArgumentException()
            }

    override fun load(tag: String) = ViewModelProviders.of(this).get(tag, getDataViewModelClass(tag)
            ?: TeambrellaDataViewModel::class.java).load()

    override fun getPager(tag: String): IDataPager<JsonArray> = ViewModelProviders.of(this).get(tag, getPagerViewModelClass(tag)
            ?: TeambrellaPagerViewModel::class.java).dataPager

    protected open val isRequestable = false

    protected fun request(uri: Uri) {
        ViewModelProviders.of(this).get(TeambrellaRequestViewModel::class.java).request(uri)
    }

    protected fun request(uri: Uri, privateKey: String) {
        ViewModelProviders.of(this).get(TeambrellaRequestViewModel::class.java).request(this, privateKey, uri)
    }

    protected open fun onRequestResult(response: Notification<JsonObject>) {

    }


    override fun createComponent(): ITeambrellaComponent {
        return DaggerITeambrellaComponent.builder()
                .contextModule(ContextModule(this))
                .teambrellaUserModule(TeambrellaUserModule())
                .teambrellaServerModule(TeambrellaServerModule())
                .teambrellaImageLoaderModule(TeambrellaImageLoaderModule())
                .build()
    }

    protected open fun <T : TeambrellaDataViewModel> getDataViewModelClass(tag: String): Class<T>? = null
    protected open fun <T : TeambrellaPagerViewModel> getPagerViewModelClass(tag: String): Class<T>? = null
    protected open fun getDataConfig(tag: String): Bundle? = null
    protected open fun getDataPagerConfig(tag: String): Bundle? = null
    protected open val dataTags: Array<String> = emptyArray()
    protected open val dataPagerTags: Array<String> = emptyArray()

}