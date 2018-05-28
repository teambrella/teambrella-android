package com.teambrella.android.data.base

import android.net.Uri
import com.google.gson.JsonObject
import io.reactivex.Notification
import io.reactivex.Observable

interface IDataPager<T> {
    val loadedData: T
    val dataObservable: Observable<Notification<JsonObject>>
    val itemChangeObservable: Observable<Int>?
    val hasNext: Boolean
    val hasPrevious: Boolean
    val hasNextError: Boolean
    val isNextLoading: Boolean
    val hasPreviousError: Boolean
    val isPreviousLoading: Boolean
    fun loadNext(force: Boolean)
    fun loadPrevious(force: Boolean)
    fun reload()
    fun reload(uri: Uri)
}