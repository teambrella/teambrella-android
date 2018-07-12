package com.teambrella.android.data.base

import android.annotation.SuppressLint
import android.net.Uri
import com.google.gson.JsonObject
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.status
import com.teambrella.android.api.uri
import com.teambrella.android.dagger.Dependencies
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

class TeambrellaDataLoader {

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


    fun load(uri: Uri, data: JsonObject? = null) {
        server.requestObservable(uri, data)?.map { response: JsonObject ->
            response.status?.uri = uri.toString()
            return@map response
        }?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeAutoDispose(this::onNext, this::onError, this::onComplete)

    }

    @SuppressLint("CheckResult")
    internal fun update(updater: (JsonObject?) -> Boolean) {
        observable.doOnNext { _notification ->
            var updated = false;
            if (_notification.isOnNext) {
                updated = updater(_notification.value)
            }
            if (updated) {
                onNext(_notification.value!!)
            }
        }?.blockingFirst()
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


fun <T> Observable<T>.subscribeAutoDispose(onNext: (T) -> Unit, onError: (Throwable) -> Unit,
                                           onComplete: () -> Unit) {
    lateinit var disposable: Disposable
    fun dispose() {
        disposable.dispose()
    }
    disposable = this.subscribe({
        dispose()
        onNext(it)
    }, {
        dispose()
        onError(it)
    }, {
        dispose()
        onComplete()
    })
}