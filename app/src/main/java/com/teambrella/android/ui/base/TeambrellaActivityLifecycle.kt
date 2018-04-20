package com.teambrella.android.ui.base

import android.content.Context
import android.os.Bundle

interface TeambrellaActivityLifecycle {
    fun onCreate(context: Context, savedInstanceState: Bundle?)
    fun onStart()
    fun onResume()
    fun onSaveInstanceState(outState: Bundle)
    fun onPause()
    fun onStop()
    fun onDestroy(context: Context)
}