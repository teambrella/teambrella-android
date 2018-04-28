package com.teambrella.android.data.base

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.gson.JsonObject
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.status
import com.teambrella.android.api.uri
import com.teambrella.android.dagger.Dependencies
import com.teambrella.android.ui.base.TeambrellaDataHostActivity
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named


private const val EXTRA_URI = "uri"
private const val EXTRA_LOAD_ON_CREATE = "load_on_create"

@JvmOverloads
fun <T> createInstance(uri: Uri? = null, loadOnCreate: Boolean = false, _class: Class<T>): T
        where T : TeambrellaDataFragment {
    val fragment: T = _class.newInstance()
    fragment.apply {
        arguments = Bundle().apply {
            putParcelable(EXTRA_URI, uri)
            putBoolean(EXTRA_LOAD_ON_CREATE, loadOnCreate)
        }
    }

    return fragment;
}

@JvmOverloads
fun createInstance(uri: Uri? = null, loadOnCreate: Boolean = false): TeambrellaDataFragment {
    val fragment = TeambrellaDataFragment()
    fragment.apply {
        arguments = Bundle().apply {
            putParcelable(EXTRA_URI, uri)
            putBoolean(EXTRA_LOAD_ON_CREATE, loadOnCreate)
        }
    }

    return fragment;
}


open class TeambrellaDataFragment : Fragment() {

    val uri: Uri?
        get() = arguments?.getParcelable(EXTRA_URI)
    var loader: TeambrellaDataLoader? = null
    val observable: Observable<Notification<JsonObject>>?
        get() = loader?.observable


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loader = createLoader()
        (context as TeambrellaDataHostActivity).component.inject(loader)
        if (arguments?.getBoolean(EXTRA_LOAD_ON_CREATE, false) == true) {
            load()
        }
    }

    open fun createLoader(): TeambrellaDataLoader = TeambrellaDataLoader()

    fun load() {
        this.uri?.let {
            loader?.load(it, null)
        }
    }

    fun load(uri: Uri) {
        loader?.load(uri, null)
    }
}

open class TeambrellaDataLoader {

    private val publisher: PublishSubject<Notification<JsonObject>> = PublishSubject.create()
    private val connectable: ConnectableObservable<Notification<JsonObject>>

    @Inject
    @field:Named(Dependencies.TEAMBRELLA_SERVER)
    lateinit var server: TeambrellaServer

    init {
        connectable = publisher.replay(1)
        connectable.connect()
    }


    val observable: Observable<Notification<JsonObject>>
        get() = connectable


    fun load(uri: Uri, data: JsonObject?) {
        server.requestObservable(uri, data)?.map { response: JsonObject ->
            response.status?.uri = uri.toString()
            return@map response
        }?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(this::onNext, this::onError, this::onComplete)

    }

    internal fun update(updater: (JsonObject?) -> Boolean) {
        observable.doOnNext({ _notification ->
            var updated = false;
            if (_notification.isOnNext) {
                updated = updater(_notification.value)
            }
            if (updated) {
                onNext(_notification.value!!)
            }
        })?.blockingFirst()
    }

    private fun onNext(item: JsonObject) {
        publisher.onNext(Notification.createOnNext<JsonObject>(item))
    }

    private fun onError(throwable: Throwable) {
        publisher.onNext(Notification.createOnError(throwable))
    }

    private fun onComplete() {

    }

}