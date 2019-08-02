package com.teambrella.android.ui.teammate

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.teambrella.android.data.base.subscribeAutoDispose
import com.teambrella.android.services.TeambrellaNotificationServiceClient
import com.teambrella.android.services.push.INotificationMessage
import com.teambrella.android.ui.base.*
import com.teambrella.android.ui.chat.ChatActivity
import com.teambrella.android.ui.dialog.ProgressDialogFragment
import com.teambrella.android.util.ImagePicker
import io.reactivex.Notification

fun getTeammateIntent(context: Context, teamId: Int, userId: String, name: String?, userPictureUri: String?) = Intent(context, TeammateActivity::class.java).apply {
    this.userId = userId
    this.teamId = teamId
    this.userName = name
    this.avatar = userPictureUri
    this.uri = TeambrellaUris.getTeammateUri(teamId, userId)
}

fun startTeammateActivity(context: Context, teamId: Int, userId: String, name: String?, userPictureUri: String?) {
    context.startActivity(getTeammateIntent(context, teamId, userId, name, userPictureUri))
}

class TeammateActivity : ATeambrellaActivity(), ITeammateActivity {

    companion object {
        private const val TEAMMATE = "teammate"
        private const val VOTE = "vote"
        private const val PROXY = "proxy"
        private const val UI = "ui"
        private const val PLEASE_WAIT_DIALOG_FRAGMENT_TAG = "please_wait_tag"
    }

    override val dataTags: Array<String> = arrayOf(TEAMMATE, VOTE, PROXY)

    private var userId: String? = null
        get() = field ?: intent.userId

    private var teammateId: Int? = null

    private var userName: String? = null
        get() = field ?: intent.userName

    private var avatar: String? = null
        get() = field ?: intent.avatar

    private var topicId: String? = null

    private var mSnackBar: Snackbar? = null

    private lateinit var teambrellaBroadcastManager: TeambrellaBroadcastManager
    private lateinit var titleView: TextView
    private lateinit var notificationClient: TeammateNotificationClient
    private lateinit var imagePicker: ImagePicker
    private var showPleaseWaitOnResume = false


    override fun onCreate(savedInstanceState: Bundle?) {
        teambrellaBroadcastManager = TeambrellaBroadcastManager(this)
        teambrellaBroadcastManager.registerReceiver(chatBroadCastReceiver)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teammate)

        supportFragmentManager?.apply {
            if (findFragmentByTag(UI) == null) {
                beginTransaction()
                        .add(R.id.container, createDataFragment(arrayOf(TEAMMATE, VOTE, PROXY), TeammateFragment::class.java), UI)
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
                text = intent?.userName
            }

            customView.findViewById<View>(R.id.send_message)?.apply {
                visibility = View.GONE
            }
        }

        notificationClient = TeammateNotificationClient(this)
        notificationClient.connect()

        getObservable(TEAMMATE).observe(this, Observer { notification ->
            this@TeammateActivity.onDataUpdated(notification ?: throw kotlin.RuntimeException())
        })


        imagePicker = ImagePicker(this)

    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : TeambrellaDataViewModel> getDataViewModelClass(tag: String): Class<T>? = when (tag) {
        TEAMMATE -> TeammateViewModel::class.java as Class<T>
        else -> super.getDataViewModelClass(tag)
    }

    override fun getDataConfig(tag: String): Bundle? = when (tag) {
        TEAMMATE -> Bundle().apply { this.uri = intent?.uri }
        VOTE, PROXY -> Bundle()
        else -> super.getDataConfig(tag)
    }

    override fun setTitle(title: CharSequence?) {
        titleView.text = title
    }


    override fun onResume() {
        super.onResume()
        notificationClient.onPause()
        if (showPleaseWaitOnResume) {
            showPleaseWaitOnResume = false
            showPleaseWait()
        }
    }

    override fun onPause() {
        super.onPause()
        notificationClient.onResume()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePicker.onActivityResult(requestCode, resultCode, data)?.apply {
            showPleaseWaitOnResume = true
            subscribeAutoDispose({ t ->
                request(TeambrellaUris.setAvatarUri(t.file.absolutePath))
            }, {
                showPleaseWaitOnResume = false
                hidePleaseWait()
            }, { })
        }
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


                _data.team?.let { _team ->
                    supportActionBar?.customView?.findViewById<View>(R.id.send_message)?.apply {
                        when {
                            user.userId == intent.userId ||
                                    _team.teamAccessLevel == TeambrellaModel.TeamAccessLevel.READ_ONLY_ALL_AND_STEALTH
                            -> {
                                visibility = View.GONE
                            }
                            else -> {
                                visibility = View.VISIBLE
                                setOnClickListener {
                                    ChatActivity.startConversationChat(this@TeammateActivity, userId, userName, avatar)
                                }
                            }
                        }
                    }
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
                    .load(TeambrellaUris.getSetMyProxyUri(it, set))
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

    override fun getTeamId(): Int = intent.teamId

    override fun getTeammateName(): String? = userName

    override fun getTeammateId(): Int = teammateId ?: 0

    override val isRequestable = true

    override fun launchActivity(intent: Intent?) {
        startActivityForResult(intent, 3)
    }

    override fun setAvatar() {
        imagePicker.startPicking(getString(R.string.select_profile_photo))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        imagePicker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onRequestResult(response: Notification<JsonObject>) {
        super.onRequestResult(response)
        response.takeIf { it.isOnNext }?.value?.status?.uri?.let {
            when (TeambrellaUris.sUriMatcher.match(Uri.parse(it))) {
                TeambrellaUris.SET_AVATAR -> {
                    showPleaseWaitOnResume = false
                    hidePleaseWait()
                    load(TEAMMATE)
                }
            }
        }
    }

    private inner class TeammateNotificationClient(context: Context) : TeambrellaNotificationServiceClient(context) {

        private var mResumed: Boolean = false
        private var mUpdateOnResume: Boolean = false

        override fun onPushMessage(message: INotificationMessage): Boolean {
            val topicId = message.topicId
            if (topicId != null && topicId == this@TeammateActivity.topicId) {
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


    private fun showPleaseWait() {
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(PLEASE_WAIT_DIALOG_FRAGMENT_TAG) == null) {
            ProgressDialogFragment().show(fragmentManager, PLEASE_WAIT_DIALOG_FRAGMENT_TAG)
        }
    }

    private fun hidePleaseWait() {
        val fragmentManager = supportFragmentManager
        val fragment = fragmentManager.findFragmentByTag(PLEASE_WAIT_DIALOG_FRAGMENT_TAG)
        if (fragment != null && fragment is ProgressDialogFragment) {
            fragment.dismiss()
        }
    }


    private val chatBroadCastReceiver = object : TeambrellaBroadcastReceiver() {
        override fun onTopicRead(topicId: String) {
            super.onTopicRead(topicId)
            ViewModelProviders.of(this@TeammateActivity)
                    .get(TEAMMATE, TeammateViewModel::class.java)
                    .markTopicRead(topicId)
        }
    }
}