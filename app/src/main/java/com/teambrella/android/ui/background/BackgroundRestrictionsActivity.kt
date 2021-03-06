package com.teambrella.android.ui.background

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.teambrella.android.R
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.WelcomeActivity
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.startHuaweiProtectApp


class BackgroundRestrictionsActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_DONE = "extra_done"
        private const val OPEN_SETTINGS = "open_settings"
        private const val DONE = "done"
        private const val CANCEL = "cancel"

        private const val SOURCE = "source"
        private const val NOTIFICATION = "notification"

        fun getNotificationIntent(context: Context): Intent = Intent(context, BackgroundRestrictionsActivity::class.java)
                .putExtra(SOURCE, NOTIFICATION)
    }

    private var isDone = false
    private val actionProgress: ProgressBar? by lazy(LazyThreadSafetyMode.NONE) { findViewById<ProgressBar>(R.id.action_progress) }
    private val backProgress: ProgressBar? by lazy(LazyThreadSafetyMode.NONE) { findViewById<ProgressBar>(R.id.back_progress) }
    private val action: TextView? by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.action) }
    private val back: TextView? by lazy(LazyThreadSafetyMode.NONE) { findViewById<TextView>(R.id.back) }
    private val animatedGuide: ImageView? by lazy(LazyThreadSafetyMode.NONE) { findViewById<ImageView>(R.id.animated_guide) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_background_app)

        savedInstanceState?.let {
            isDone = it.getBoolean(EXTRA_DONE, false)
        }

        action?.setOnClickListener(this::startBackgroundAppSettings)
        back?.setOnClickListener(this::finish)


        animatedGuide?.let {
            GlideApp.with(this).asGif().load(R.drawable.protected_app_guide).into(it)
        }

    }

    override fun onStart() {
        super.onStart()
        if (isDone) {
            actionProgress?.visibility = View.GONE
            backProgress?.visibility = View.GONE
            action?.text = getString(R.string.huawei_protected_app_done)
            back?.text = getString(R.string.huawei_protected_app_action)
            action?.setOnClickListener(this::finish)
            back?.setOnClickListener(this::startBackgroundAppSettings)
        }
    }

    private fun startBackgroundAppSettings(view: View) {
        StatisticHelper.onBackgroundRestrictionScreenAction(this, OPEN_SETTINGS)
        isDone = startHuaweiProtectApp()
        if (isDone) {
            when (view.id) {
                R.id.action -> {
                    actionProgress?.visibility = View.VISIBLE
                    action?.text = null
                }
                R.id.back -> {
                    backProgress?.visibility = View.VISIBLE
                    back?.text = null
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        StatisticHelper.onBackgroundRestrictionScreenAction(this, CANCEL)
    }

    private fun finish(view: View) {
        StatisticHelper.onBackgroundRestrictionScreenAction(this, DONE)
        finish()
    }


    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState?.putBoolean(EXTRA_DONE, isDone)
    }

    override fun finish() {
        super.finish()
        intent?.let {
            if (it.getStringExtra(SOURCE) == NOTIFICATION) {
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
        }
    }
}