package com.teambrella.android.data.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

class TeambrellaDataLoader {

    @Inject
    @field:Named(Dependencies.TEAMBRELLA_SERVER)
    lateinit var server: TeambrellaServer


    val observable: LiveData<Notification<JsonObject>> = MutableLiveData()


    fun load(uri: Uri, data: JsonObject? = null) {
        server.requestObservable(uri, data)?.map { response: JsonObject ->
            response.status?.uri = uri.toString()
            return@map response
        }?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeAutoDispose(this::onNext, this::onError, this::onComplete)

    }

    internal fun update(updater: (JsonObject?) -> Boolean) {
        observable.value?.let { _notification ->
            var updated = false;
            if (_notification.isOnNext) {
                updated = updater(_notification.value)
            }
            if (updated) {
                onNext(_notification.value!!)
            }
        }
    }

    private fun onNext(item: JsonObject) {
        (observable as MutableLiveData).postValue(Notification.createOnNext<JsonObject>(item))
    }

    private fun onError(throwable: Throwable) {
        (observable as MutableLiveData).postValue(Notification.createOnError(throwable))
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