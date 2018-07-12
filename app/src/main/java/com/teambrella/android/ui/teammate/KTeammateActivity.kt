package com.teambrella.android.ui.teammate

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.services.TeambrellaNotificationServiceClient
import com.teambrella.android.services.push.INotificationMessage
import com.teambrella.android.ui.base.*
import com.teambrella.android.ui.chat.ChatActivity
import io.reactivex.Notification
import io.reactivex.disposables.Disposable

private const val TEAMMATE_URI = "teammate_uri"
private const val TEAMMATE_NAME = "teammate_name"
private const val TEAMMATE_PICTURE = "teammate_picture"
private const val TEAMMATE_USER_ID = "teammate_user_id"
private const val TEAM_ID = "team_id"


fun getTeammateIntent(context: Context, teamId: Int, userId: String, name: String?, userPictureUri: String?): Intent {
    return Intent(context, KTeammateActivity::class.java)
            .putExtra(TEAMMATE_USER_ID, userId)
            .putExtra(TEAMMATE_URI, TeambrellaUris.getTeammateUri(teamId, userId))
            .putExtra(TEAMMATE_NAME, name)
            .putExtra(TEAM_ID, teamId)
            .putExtra(TEAMMATE_PICTURE, userPictureUri)
}

fun startTeammateActivity(context: Context, teamId: Int, userId: String, name: String?, userPictureUri: String?) {
    context.startActivity(getTeammateIntent(context, teamId, userId, name, userPictureUri))
}

class KTeammateActivity : ATeambrellaActivity(), ITeammateActivity {

    companion object {
        private const val TEAMMATE = "teammate"
        private const val VOTE = "vote"
        private const val PROXY = "proxy"
        private const val UI = "ui"
    }

    override val dataTags: Array<String> = arrayOf(TEAMMATE, VOTE, PROXY)

    private var userId: String? = null
        get() = field ?: intent.getStringExtra(TEAMMATE_USER_ID)

    private var teammateId: Int? = null

    private var userName: String? = null
        get() = field ?: intent.getStringExtra(TEAMMATE_NAME)

    private var avatar: String? = null
        get() = field ?: intent.getStringExtra(TEAMMATE_PICTURE)

    private var topicId: String? = null

    private var mSnackBar: Snackbar? = null


    private var teammateDisposable: Disposable? = null

    private lateinit var teambrellaBroadcastManager: TeambrellaBroadcastManager
    private lateinit var titleView: TextView
    private lateinit var notificationClient: TeammateNotificationClient


    override fun onCreate(savedInstanceState: Bundle?) {
        teambrellaBroadcastManager = TeambrellaBroadcastManager(this)
        teambrellaBroadcastManager.registerReceiver(chatBroadCastReceiver)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiity_teammate)

        supportFragmentManager?.apply {
            if (findFragmentByTag(UI) == null) {
                beginTransaction()
                        .add(R.id.container, ADataProgressFragment.getInstance(arrayOf(TEAMMATE, VOTE, PROXY), KTeammateFragment::class.java), UI)
                        .commit()
            }
        }

        supportActionBar?.apply {

            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector)
            displayOptions = displayOptions or ActionBar.DISPLAY_SHOW_CUSTOM

            setCustomView(R.layout.teammate_toolbar_view)

            customView.apply {
                (parent as Toolbar).apply {
                    setPadding(0, 0, 0, 0)
                    setContentInsetsAbsolute(0, 0)
                }
            }

            titleView = customView.findViewById<TextView>(R.id.title).apply {
                text = intent?.getStringExtra(TEAMMATE_NAME)
            }

            customView.findViewById<View>(R.id.send_message)?.apply {
                if (user.userId == intent.getStringExtra(TEAMMATE_USER_ID)) {
                    visibility = View.GONE
                } else {
                    setOnClickListener {
                        ChatActivity.startConversationChat(this@KTeammateActivity, userId, userName, avatar)
                    }
                }
            }
        }

        notificationClient = TeammateNotificationClient(this)
        notificationClient.connect()

    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : TeambrellaDataViewModel> getDataViewModelClass(tag: String): Class<T>? = when (tag) {
        TEAMMATE -> TeammateViewModel::class.java as Class<T>
        else -> super.getDataViewModelClass(tag)
    }

    override fun getDataConfig(tag: String): Bundle? = when (tag) {
        TEAMMATE -> Bundle().apply { this.uri = intent?.getParcelableExtra(TEAMMATE_URI) }
        VOTE, PROXY -> Bundle()
        else -> super.getDataConfig(tag)
    }

    override fun setTitle(title: CharSequence?) {
        titleView.text = title
    }

    override fun onStart() {
        super.onStart()
        teammateDisposable = getObservable(TEAMMATE).subscribe(this::onDataUpdated)
    }

    override fun onResume() {
        super.onResume()
        notificationClient.onPause()
    }

    override fun onPause() {
        super.onPause()
        notificationClient.onResume()
    }

    override fun onStop() {
        super.onStop()
        teammateDisposable?.takeIf { !it.isDisposed }?.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        notificationClient.disconnect()
        teambrellaBroadcastManager.unregisterReceiver(chatBroadCastReceiver)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            notification.value.data.let { _data ->
                teammateId = _data.intId
                _data.basic?.let { _basic ->
                    userId = _basic.userId
                    userName = _basic.userName
                    avatar = _basic.avatar
                }
                _data.discussionPart?.let { _discussion ->
                    topicId = _discussion.topicId
                }
            }
        }
    }

    override fun postVote(vote: Double) {
        teammateId?.let {
            ViewModelProviders.of(this).get(VOTE, TeambrellaDataViewModel::class.java)
                    .load(TeambrellaUris.getTeammateVoteUri(it, vote))
        }
    }

    override fun setAsProxy(set: Boolean) {
        userId?.let {
            ViewModelProviders.of(this).get(PROXY, TeambrellaDataViewModel::class.java)
                    .load(TeambrellaUris.setMyProxyUri(it, set))
        }
    }

    override fun isItMe(): Boolean = (userId == user.userId)


    override fun getCurrency(): String? = null

    override fun showSnackBar(text: Int) {
        if (mSnackBar == null) {
            mSnackBar = Snackbar.make(findViewById(R.id.container), text, Snackbar.LENGTH_LONG)
            mSnackBar?.addCallback(object : Snackbar.Callback() {
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    mSnackBar = null
                }
            })
            mSnackBar?.show()
        }
    }

    override fun getTeamId(): Int = intent.getIntExtra(TEAM_ID, 0)

    override fun getTeammateId(): Int = teammateId ?: 0

    override fun launchActivity(intent: Intent?) {
        startActivityForResult(intent, 3)
    }


    private inner class TeammateNotificationClient(context: Context) : TeambrellaNotificationServiceClient(context) {

        private var mResumed: Boolean = false
        private var mUpdateOnResume: Boolean = false

        override fun onPushMessage(message: INotificationMessage): Boolean {
            val topicId = message.topicId
            if (topicId != null && topicId == this@KTeammateActivity.topicId) {
                if (mResumed) {
                    load(TEAMMATE)
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
                load(TEAMMATE)
                mUpdateOnResume = false
            }
        }
    }


    private val chatBroadCastReceiver = object : TeambrellaBroadcastReceiver() {
        override fun onTopicRead(topicId: String) {
            super.onTopicRead(topicId)
            ViewModelProviders.of(this@KTeammateActivity)
                    .get(TEAMMATE, TeammateViewModel::class.java)
                    .markTopicRead(topicId)
        }
    }
}