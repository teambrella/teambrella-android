package com.teambrella.android.data.base

import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.dagger.Dependencies
import com.teambrella.android.ui.base.TeambrellaDataHostActivity
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observables.ConnectableObservable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Named


private fun <T> Observable<T>.subscribeAutoDispose(onNext: (T) -> Unit, onError: (Throwable) -> Unit,
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

open class TeambrellaDataPagerFragment : Fragment() {

    companion object {
        const val EXTRA_URI = "uri"
        const val EXTRA_PROPERTY = "property"

        @JvmOverloads
        fun <T> createInstance(uri: Uri? = null, property: String? = null, _class: Class<T>): T
                where T : TeambrellaDataPagerFragment {
            val fragment: T = _class.newInstance()
            fragment.apply {
                arguments = Bundle().apply {
                    putParcelable(EXTRA_URI, uri)
                    putString(EXTRA_PROPERTY, property)
                }
            }

            return fragment
        }
    }

    private lateinit var _pager: IDataPager<JsonArray>

    val pager: IDataPager<JsonArray>
        get() = _pager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        arguments?.let {
            _pager = createLoader(it)
        }
    }

    protected open fun createLoader(args: Bundle): IDataPager<JsonArray> {
        val uri = args.getParcelable<Uri>(EXTRA_URI)
        val property = args.getString(EXTRA_PROPERTY)
        val loader = TeambrellaDataPagerLoader(uri, property)
        (context as TeambrellaDataHostActivity).component.inject(loader)
        return loader
    }
}

@Suppress("PropertyName")
abstract class ATeambrellaDataPagerLoader() : IDataPager<JsonArray> {

    private val connectableObservable: ConnectableObservable<Notification<JsonObject>>
    protected val publisher = PublishSubject.create<Notification<JsonObject>>()
    protected var array = JsonArray()
    protected var _hasNextError = false
    protected var _isNextLoading = false
    protected var _hasNext = true
    protected var _hasPreviousError = false
    protected var _isPreviousLoading = false
    protected var _hasPrevious = true
    protected var _nextIndex = 0
    protected var _previousIndex = 0

    init {
        connectableObservable = publisher.publish()
        connectableObservable.connect()
    }

    override val loadedData: JsonArray
        get() = array
    override val dataObservable: Observable<Notification<JsonObject>>
        get() = connectableObservable
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
                    .map({
                        it.metadata = JsonObject().apply {
                            reload = false
                            forced = force
                            direction = TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION
                            size = getPageableData(it).size()
                        }
                        it
                    })
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
                .map({
                    it.metadata = JsonObject().apply {
                        reload = true
                        forced = true
                        direction = TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION
                        size = getPageableData(it).size()
                    }
                    it
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext({
                    _nextIndex = 0
                    array = JsonArray()
                })
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
        publisher.onNext(Notification.createOnNext(data))
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
        publisher.onNext(Notification.createOnError<JsonObject>(throwable))
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
        private const val LIMIT = 200
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
                    .map({
                        it.metadata = (it.metadata ?: JsonObject()).apply {
                            reload = false
                            forced = force
                            direction = TeambrellaModel.ATTR_METADATA_NEXT_DIRECTION
                            size = getPageableData(it).size()
                        }
                        it
                    })
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
                    .map({
                        it.metadata = (it.metadata ?: JsonObject()).apply {
                            reload = false
                            forced = force
                            direction = TeambrellaModel.ATTR_METADATA_PREVIOUS_DIRECTION
                            size = getPageableData(it).size()
                        }
                        it
                    })
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
            publisher.onNext(Notification.createOnNext(response))
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
        publisher.onNext(Notification.createOnNext(response))
    }

    private fun onError(error: Throwable) {
        publisher.onNext(Notification.createOnError(error))
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
        publisher.onNext(Notification.createOnNext(response))
    }


    override fun reload() = reload(chatUri)
    override fun reload(uri: Uri) = Unit
}


