package com.teambrella.android.ui.claim

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.services.TeambrellaNotificationServiceClient
import com.teambrella.android.services.push.INotificationMessage
import com.teambrella.android.ui.base.*
import com.teambrella.android.ui.teammate.startTeammateActivity
import com.teambrella.android.ui.util.setAvatar
import com.teambrella.android.util.StatisticHelper
import io.reactivex.Notification

/**
 * Claim Activity
 */
class ClaimActivity : ATeambrellaActivity(), IClaimActivity {

    companion object {

        private const val CLAIM_DATA_TAG = "claim_data_tag"
        private const val VOTE_DATA_TAG = "vote_data_tag"
        private const val UI_TAG = "ui"
        private const val DEFAULT_REQUEST_CODE = 4

        fun getLaunchIntent(context: Context, id: Int, model: String?, teamId: Int): Intent {
            return Intent(context, ClaimActivity::class.java).apply {
                this.teamId = teamId
                this.claimId = id
                this.model = model
                this.uri = TeambrellaUris.getClaimUri(id)
            }
        }

        fun start(context: Context, id: Int, model: String?, teamId: Int) {
            context.startActivity(getLaunchIntent(context, id, model, teamId))
        }
    }


    override var claimId: Int = 0
        get() = if (field > 0) field else intent?.claimId ?: 0

    override var teamId: Int = 0
        get() = if (field > 0) field else intent?.teamId ?: 0


    private var titleView: TextView? = null
    private var subtitleView: TextView? = null
    private var iconView: ImageView? = null
    private var snackBar: Snackbar? = null
    private var topicId: String? = null


    private lateinit var mNotificationClient: ClaimNotificationClient
    private lateinit var mChatBroadCastManager: TeambrellaBroadcastManager

    override val dataTags: Array<String>
        get() = arrayOf(CLAIM_DATA_TAG, VOTE_DATA_TAG)

    override fun onCreate(savedInstanceState: Bundle?) {
        mChatBroadCastManager = TeambrellaBroadcastManager(this)
        mChatBroadCastManager.registerReceiver(mChatBroadCastReceiver)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_claim)

        supportFragmentManager.apply {
            if (findFragmentByTag(UI_TAG) == null) {
                beginTransaction()
                        .add(R.id.container, createDataFragment(arrayOf(CLAIM_DATA_TAG, VOTE_DATA_TAG), ClaimFragment::class.java), UI_TAG)
                        .commit()
            }
        }

        mNotificationClient = ClaimNotificationClient(this)
        mNotificationClient.connect()

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector)
            displayOptions = displayOptions or ActionBar.DISPLAY_SHOW_CUSTOM
            setCustomView(R.layout.claim_toolbar_view)
            customView?.apply {
                titleView = findViewById(R.id.title)
                subtitleView = findViewById(R.id.subtitle)
                iconView = findViewById(R.id.icon)
                (parent as Toolbar).apply {
                    setPadding(0, 0, 0, 0)
                    setContentInsetsAbsolute(0, 0)
                }
            }


        }
        titleView?.text = (intent.model)
        component.inject(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        getObservable(CLAIM_DATA_TAG).observe(this, Observer<Notification<JsonObject>> { it?.let { this.onDataUpdated(it) } })
    }


    override fun getDataConfig(tag: String): Bundle? {
        when (tag) {
            CLAIM_DATA_TAG -> return getDataConfig(intent.uri)
            VOTE_DATA_TAG -> return getDataConfig()
        }
        return super.getDataConfig(tag)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : TeambrellaDataViewModel> getDataViewModelClass(tag: String) = when (tag) {
        CLAIM_DATA_TAG -> ClaimViewModel::class.java as Class<T>
        else -> super.getDataViewModelClass<T>(tag)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setTitle(title: String) {
        titleView?.text = title
    }

    override fun setSubtitle(subtitle: String) {
        subtitleView?.text = subtitle
    }


    override fun postVote(vote: Int) {
        ViewModelProviders.of(this).get(VOTE_DATA_TAG, TeambrellaDataViewModel::class.java)
                .load(TeambrellaUris.getClaimVoteUri(claimId, vote))
        StatisticHelper.onClaimVote(this, teamId, claimId, vote)
        TeambrellaBroadcastManager(this).notifyClaimVote(claimId)
    }


    override fun showSnackBar(@StringRes text: Int) {
        if (snackBar == null) {
            snackBar = Snackbar.make(findViewById(R.id.container), text, Snackbar.LENGTH_LONG)
            snackBar?.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    snackBar = null
                }
            })
            snackBar?.show()
        }
    }

    override fun onResume() {
        super.onResume()
        mNotificationClient.onResume()
    }


    override fun onPause() {
        super.onPause()
        mNotificationClient.onPause()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        load(CLAIM_DATA_TAG)
        super.onActivityResult(requestCode, resultCode, data)
    }


    override fun onDestroy() {
        super.onDestroy()
        mNotificationClient.disconnect()
        mChatBroadCastManager.unregisterReceiver(mChatBroadCastReceiver)
    }

    override fun launchActivity(intent: Intent) {
        startActivityForResult(intent, DEFAULT_REQUEST_CODE)
    }

    private fun onDataUpdated(notification: Notification<JsonObject>) {
        notification.takeIf { it.isOnNext }?.value?.data?.let { _data ->
            _data.basic?.let { _basic ->
                val pictureUri = _basic.avatar
                iconView?.setAvatar(getImageLoader()?.getImageUrl(pictureUri))
                iconView?.setOnClickListener {
                    startTeammateActivity(this@ClaimActivity, intent.teamId
                            ?: 0, _basic.userId!!, _basic.name, pictureUri)
                }
                titleView?.text = _basic.model
            }

            topicId = _data.discussion?.topicId
            claimId = _data.intId ?: 0
        }
    }

    private fun markTopicRead(topicId: String?) {
        ViewModelProviders.of(this).get(CLAIM_DATA_TAG, ClaimViewModel::class.java).markTopicRead(topicId!!)
    }

    private inner class ClaimNotificationClient internal constructor(context: Context) : TeambrellaNotificationServiceClient(context) {

        private var mResumed: Boolean = false
        private var mUpdateOnResume: Boolean = false

        override fun onPushMessage(message: INotificationMessage): Boolean {
            val topicId = message.topicId
            if (topicId != null && topicId == this@ClaimActivity.topicId) {
                if (mResumed) {
                    load(CLAIM_DATA_TAG)
                } else {
                    mUpdateOnResume = true
                }
            }
            return false
        }

        internal fun onPause() {
            mResumed = false
        }

        internal fun onResume() {
            mResumed = true
            if (mUpdateOnResume) {
                load(CLAIM_DATA_TAG)
                mUpdateOnResume = false
            }
        }
    }

    private val mChatBroadCastReceiver = object : TeambrellaBroadcastReceiver() {
        override fun onTopicRead(topicId: String) {
            super.onTopicRead(topicId)
            if (topicId == this@ClaimActivity.topicId) {
                markTopicRead(this@ClaimActivity.topicId)
            }
        }
    }
}
