package com.teambrella.android.data.base

import android.content.Context
import android.net.Uri
import com.google.gson.JsonObject
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.status
import com.teambrella.android.api.uri
import com.teambrella.android.dagger.Dependencies
import com.teambrella.android.ui.TeambrellaUser
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class TeambrellaRequestLoader {

    @Inject
    @field:Named(Dependencies.TEAMBRELLA_SERVER)
    lateinit var server: TeambrellaServer

    private var mIsActive: Boolean = false
    private val mBuffer = LinkedList<Notification<JsonObject>>()
    private val mPublisher = PublishSubject.create<Notification<JsonObject>>()
    private val mConnectable = mPublisher.publish()

    val observable: Observable<Notification<JsonObject>>
        get() = mConnectable

    init {
        mConnectable.connect()
    }

    /**
     * Start emitting events
     */
    fun start() {
        mIsActive = true
        while (!mBuffer.isEmpty()) {
            mPublisher.onNext(mBuffer.poll())
        }
    }

    fun request(uri: Uri) {
        server?.let {
            request(it, uri)
        }
    }


    fun request(context: Context, privateKey: String, uri: Uri) {
        val user = TeambrellaUser.get(context)
        request(TeambrellaServer(context, privateKey, user.deviceCode, user.getInfoMask(context)), uri)
    }


    private fun request(server: TeambrellaServer, uri: Uri) {
        server.requestObservable(uri, null)
                .map {
                    it.apply {
                        status?.uri = uri.toString()
                    }
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeAutoDispose({ onNext(it) }, { onError(it) }, { onComplete() })
    }


    /**
     * Stop emitting events
     */
    fun stop() {
        mIsActive = false
    }


    private fun onNext(value: JsonObject) {
        if (mIsActive) {
            mPublisher.onNext(Notification.createOnNext(value))
        } else {
            mBuffer.add(Notification.createOnNext(value))
        }
    }

    private fun onError(throwable: Throwable) {
        if (mIsActive) {
            mPublisher.onNext(Notification.createOnError(throwable))
        } else {
            mBuffer.add(Notification.createOnError(throwable))
        }
    }

    private fun onComplete() {

    }

    companion object {


        /**
         * Tag for logging
         */
        private val LOG_TAG = TeambrellaRequestFragment::class.java.simpleName
    }
}
