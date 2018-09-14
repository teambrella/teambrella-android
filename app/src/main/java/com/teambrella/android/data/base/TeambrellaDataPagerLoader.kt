package com.teambrella.android.data.base

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.net.Uri
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.dagger.Dependencies
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named

@Suppress("PropertyName")
abstract class ATeambrellaDataPagerLoader() : IDataPager<JsonArray> {

    protected val _observable: MutableLiveData<Notification<JsonObject>> = MutableLiveData()
    protected var array = JsonArray()
    protected var _hasNextError = false
    protected var _isNextLoading = false
    protected var _hasNext = true
    protected var _hasPreviousError = false
    protected var _isPreviousLoading = false
    protected var _hasPrevious = true
    protected var _nextIndex = 0
    protected var _previousIndex = 0

    override val loadedData: JsonArray
        get() = array
    override val dataObservable: LiveData<Notification<JsonObject>>
        get() = _observable
    override val hasNext: Boolean
        get() = _hasNext
    override val hasPrevious: Boolean
        get() = _hasPrevious
    override val hasNextError: Boolean
        get() = _hasNextError
    override val isNextLoading: Boolean
        get() = _isNextLoading
    override val hasPreviousError: Boolean
        get() = _hasPreviousError
    override val isPreviousLoading: Boolean
        get() = _isPreviousLoading
}

open class TeambrellaDataPagerLoader(private val uri: Uri, private val property: String? = null, private val limit: Int = 50)
    : ATeambrellaDataPagerLoader() {

    override val itemChangeObservable = PublishSubject.create<Int>()

    @Inject
    @field:Named(Dependencies.TEAMBRELLA_SERVER)
    lateinit var server: TeambrellaServer


    override fun loadNext(force: Boolean) {
        if (!_isNextLoading && (_hasNext || force)) {
            server.requestObservable(TeambrellaUris.appendPagination(uri, _nextIndex, limit), null)
                    .map(this::appendUri)
                    .subscribeOn(Schedulers.io())
                    .map {
                        it.metadata = JsonObject().apply {
                            reload = false
                            forced = force
                            direction = TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION
                            size = getPageableData(it).size()
                        }
                        it
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeAutoDispose(this::onNext, this::onError, this::onComplete)
            _isNextLoading = true
            _hasNextError = false
        }
    }

    override fun loadPrevious(force: Boolean) = Unit
    override fun reload() = reload(uri)

    override fun reload(uri: Uri) {
        server.requestObservable(TeambrellaUris.appendPagination(uri, 0, limit), null)
                .map(this::appendUri)
                .subscribeOn(Schedulers.io())
                .map {
                    it.metadata = JsonObject().apply {
                        reload = true
                        forced = true
                        direction = TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION
                        size = getPageableData(it).size()
                    }
                    it
                }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    _nextIndex = 0
                    array = JsonArray()
                }
                .subscribeAutoDispose(this::onNext, this::onError, this::onComplete)
        _isNextLoading = true
        _hasNextError = false
    }


    private fun onNext(data: JsonObject) {
        val newData = getPageableData(data)
        onAddNewData(newData)
        _hasNext = newData.size() == limit
        _nextIndex += newData.size()
        _isNextLoading = false
        _observable.postValue(Notification.createOnNext(data))
    }

    protected open fun getPageableData(src: JsonObject): JsonArray {
        return if (property == null) {
            src.get(TeambrellaModel.ATTR_DATA).asJsonArray
        } else {
            src.get(TeambrellaModel.ATTR_DATA)
                    .asJsonObject.get(property).asJsonArray
        }
    }


    /**
     * On Add new Data
     *
     * @param newData new data
     */
    protected open fun onAddNewData(newData: JsonArray) {
        array.addAll(newData)
    }

    private fun onError(throwable: Throwable) {
        _observable.postValue(Notification.createOnError<JsonObject>(throwable))
        _hasNextError = true
        _isNextLoading = false
    }

    private fun onComplete() {
        // nothing to do
    }

    private fun appendUri(response: JsonObject): JsonObject {
        response.status?.uri = uri.toString()
        return response
    }

    protected fun notifyItemChange(item: Int) {
        itemChangeObservable.onNext(item)
    }
}

open class KTeambrellaChatDataPagerLoader(private val chatUri: Uri) : ATeambrellaDataPagerLoader() {

    companion object {
        const val LIMIT = 200
    }

    override val itemChangeObservable: Observable<Int>? = null
    private var since: Long = -1L

    @Inject
    @field:Named(Dependencies.TEAMBRELLA_SERVER)
    lateinit var server: TeambrellaServer

    override fun loadNext(force: Boolean) {
        if (!_isNextLoading && (_hasNext || force)) {

            var uri = TeambrellaUris.appendPagination(chatUri, _nextIndex, LIMIT)

            if (since != -1L) {
                uri = TeambrellaUris.appendChatSince(uri, since)
            }

            server.requestObservable(uri, null)
                    .map(this::appendUri)
                    .map { postProcess(it, true) }
                    .map {
                        it.metadata = (it.metadata ?: JsonObject()).apply {
                            reload = false
                            forced = force
                            direction = TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION
                            size = getPageableData(it).size()
                        }
                        it
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeAutoDispose(this::onNext, this::onError, this::onComplete)
            _isNextLoading = true
            _hasNextError = false
        }
    }

    override fun loadPrevious(force: Boolean) {
        if (!_isPreviousLoading && (_hasPrevious || force)) {

            var uri = TeambrellaUris.appendPagination(chatUri, _previousIndex - LIMIT, LIMIT)

            if (since != -1L) {
                uri = TeambrellaUris.appendChatSince(uri, since)
            }

            server.requestObservable(uri, null)
                    .map(this::appendUri)
                    .map { postProcess(it, false) }
                    .map {
                        it.metadata = (it.metadata ?: JsonObject()).apply {
                            reload = false
                            forced = force
                            direction = TeambrellaModel.ATTR_METADATA_PREVIOUS_DIRECTION
                            size = getPageableData(it).size()
                        }
                        it
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeAutoDispose(this::onPrevious, this::onError, this::onComplete)
            _isPreviousLoading = true
            _hasPreviousError = false
        }

    }

    private fun onNext(response: JsonObject) {
        _isNextLoading = false
        val size = response.metadata?.originalSize ?: 0
        val newData = getPageableData(response)
        array.addAll(newData)
        _hasNext = size == LIMIT
        _nextIndex += size
        if (since == -1L) {
            since = response.data?.discussionPart?.lastRead ?: 0L
            loadPrevious(true)
            return
        } else {
            _observable.postValue(Notification.createOnNext(response))
        }
    }

    private fun onPrevious(response: JsonObject) {
        val newData = getPageableData(response)
        val size = response.metadata?.originalSize ?: 0
        newData.addAll(array)
        _hasPrevious = size == LIMIT
        _previousIndex -= size
        array = newData
        _isPreviousLoading = false
        _observable.postValue(Notification.createOnNext(response))
    }

    private fun onError(error: Throwable) {
        _observable.postValue(Notification.createOnError(error))
        _hasNextError = true
        _isNextLoading = false
    }

    private fun onComplete() {
        //nothing to do
    }


    protected open fun getPageableData(src: JsonObject): JsonArray {
        return JsonArray()
    }

    protected open fun postProcess(response: JsonObject, next: Boolean) = response

    private fun appendUri(response: JsonObject): JsonObject {
        response.status?.uri = chatUri.toString()
        return response
    }


    protected open fun addPageableData(src: JsonObject, item: JsonObject) {

    }

    fun addAsNext(item: JsonObject) {
        val response = JsonObject().apply {
            metadata = JsonObject().apply {
                reload = true
                forced = true
                direction = TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION
                size = 1
            }
            status = JsonObject().apply {
                uri = chatUri.toString()
            }
        }
        addPageableData(response, item)
        array.add(item)
        _observable.postValue(Notification.createOnNext(response))
    }


    override fun reload() = reload(chatUri)
    override fun reload(uri: Uri) = Unit
}
