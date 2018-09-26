package com.teambrella.android.api

import android.content.UriMatcher
import android.net.Uri
import com.teambrella.android.BuildConfig

object TeambrellaLinks {

    const val JOIN = 1

    private const val JOIN_SEGMENT = "join"

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        uriMatcher.addURI(BuildConfig.AUTHORITY, "$JOIN_SEGMENT/#", JOIN)
    }
    
    fun match(uri: Uri) = uriMatcher.match(uri)

}