package com.teambrella.android.ui.chat

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.UriMatcher
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.api.model.json.JsonWrapper
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.data.base.IDataPager
import com.teambrella.android.data.base.*
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.services.TeambrellaNotificationManager
import com.teambrella.android.services.TeambrellaNotificationServiceClient
import com.teambrella.android.services.push.INotificationMessage
import com.teambrella.android.ui.MainActivity
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.ui.base.*
import com.teambrella.android.ui.base.ATeambrellaActivity
import com.teambrella.android.ui.base.TeambrellaBroadcastManager
import com.teambrella.android.ui.base.TeambrellaDataViewModel
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.claim.IClaimActivity
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan
import com.teambrella.android.util.ImagePicker
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.TeambrellaDateUtils
import com.teambrella.android.util.log.Log

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.LinkedHashSet
import java.util.Date
import java.util.UUID

import io.reactivex.Notification
import io.reactivex.Observable

import com.teambrella.android.services.push.CREATED_POST
import com.teambrella.android.services.push.PRIVATE_MSG
import com.teambrella.android.services.push.TOPIC_MESSAGE_NOTIFICATION

/**
 * Claim chat
 */
class ChatActivity : ATeambrellaActivity(), IChatActivity, IClaimActivity {
	
	private var mUri: Uri? = null
	private var mTopicId: String? = null
	private var mAction: String? = null
	private var mUserId: String? = null
	private var mUserName: String? = null;
	private var mTeamId: Int = 0;
	private var mTeammateId: Int = 0;
	private var restoredUris: Boolean = false
	
	private var mMessageView: TextView? = null
	private var mImagePicker: ImagePicker? = null
	private var mTitle: TextView? = null
	private var mSubtitle: TextView? = null
	private var mIcon: ImageView? = null
	private var mClient: ChatNotificationClient? = null
	private var mNotificationManager: TeambrellaNotificationManager? = null
	private var mNotificationHelpView: View? = null
	private var mMuteStatus: IChatActivity.MuteStatus? = null
	private var mVote = -1f
	private var mLastRead = -1L
	var isFullAccess = false
		private set
	var isMyChat = false
		private set
	
	private var mContainer: View? = null
	private var mSnackBar: Snackbar? = null
	private var requestInProcess: Uri? = null
	private val urisToProcess = LinkedHashSet<Uri>()
	
	private var mChatBroadCastManager: TeambrellaBroadcastManager? = null
	
	override val muteStatus; get() = mMuteStatus
	override val userName; get() = mUserName
	override val teamId; get() = mTeamId
	override val teammateId; get() = mTeammateId
	override val claimId; get() = intent.getIntExtra(EXTRA_CLAIM_ID, 0)
	override val objectName; get() = intent.getStringExtra(EXTRA_OBJECT_NAME)
	override val userId; get() = intent.getStringExtra(EXTRA_USER_ID)
	override val imageUri; get() = intent.getStringExtra(EXTRA_IMAGE_URI)
	override val chatUri: Uri; get() = intent.getParcelableExtra(EXTRA_URI)
	
	
	private val chatViewModel
		get() = ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel::class.java)
	
	private val pinUnpinViewModel
		get() = ViewModelProviders.of(this).get(PIN_UNPIN_DATA, TeambrellaDataViewModel::class.java)
	
	
	override val dataTags: Array<String>
		get() {
			if (mAction != null) {
				when (mAction) {
					SHOW_CLAIM_CHAT_ACTION -> return arrayOf(CLAIM_DATA_TAG, VOTE_DATA_TAG, PIN_UNPIN_DATA)
					SHOW_FEED_CHAT_ACTION -> return arrayOf(PIN_UNPIN_DATA)
					SHOW_TEAMMATE_CHAT_ACTION -> return arrayOf(PIN_UNPIN_DATA)
				}
			}
			return arrayOf()
		}
	
	override val dataPagerTags: Array<String>
		get() = arrayOf(DATA_FRAGMENT_TAG)
	
	override val isRequestable: Boolean
		get() = true
	
	
	override fun onCreate(savedInstanceState: Bundle?) {
		val intent = intent
		window.setBackgroundDrawable(resources.getDrawable(R.drawable.chat_window_background))
		
		mChatBroadCastManager = TeambrellaBroadcastManager(this)
		
		mUri = intent.getParcelableExtra(EXTRA_URI)
		mTopicId = intent.getStringExtra(EXTRA_TOPIC_ID)
		mUserId = intent.getStringExtra(EXTRA_USER_ID)
		mUserName = intent.getStringExtra(EXTRA_USER_NAME)
		mTeamId = intent.getIntExtra(EXTRA_TEAM_ID, 0)
		mAction = intent.getStringExtra(EXTRA_ACTION)
		isMyChat = mUserId == TeambrellaUser.get(this).userId
		
		mLastRead = savedInstanceState?.getLong(EXTRA_LAST_READ, -1) ?: -1
		
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_chat)
		component.inject(this)
		
		mNotificationManager = TeambrellaNotificationManager(this)
		
		val actionBar = supportActionBar
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true)
			actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector)
			if (mAction != null && mAction == SHOW_CONVERSATION_CHAT) {
				actionBar.displayOptions = actionBar.displayOptions or ActionBar.DISPLAY_SHOW_CUSTOM
				actionBar.setCustomView(R.layout.chat_toolbar_view)
				val view = actionBar.customView
				mTitle = view.findViewById(R.id.title)
				mSubtitle = view.findViewById(R.id.subtitle)
				mIcon = view.findViewById(R.id.icon)
				val parent = view.parent as Toolbar
				parent.setPadding(0, 0, 0, 0)
				parent.setContentInsetsAbsolute(0, 0)
			}
			
		}
		
		mImagePicker = ImagePicker(this)
		val fragmentManager = supportFragmentManager
		
		val transaction = fragmentManager.beginTransaction()
		
		if (fragmentManager.findFragmentByTag(UI_FRAGMENT_TAG) == null) {
			transaction.add(R.id.container, createDataFragment(arrayOf(DATA_FRAGMENT_TAG), KChatFragment::class.java), UI_FRAGMENT_TAG)
		}
		
		if (!transaction.isEmpty) {
			transaction.commit()
		}
		
		
		mNotificationHelpView = findViewById(R.id.notification_help)
		
		mMessageView = findViewById(R.id.text)
		findViewById<View>(R.id.send_text).setOnClickListener { v -> this.onSendText(v) }
		findViewById<View>(R.id.send_image).setOnClickListener { v -> this.startImagePicking() }
		findViewById<View>(R.id.add_photos).setOnClickListener { v -> this.startTakingPhoto() }
		
		var needHideSendImage: Boolean? = false
		var needShowContinueJoining = false
		if (mAction != null) {
			when (mAction) {
				SHOW_TEAMMATE_CHAT_ACTION -> setTitle(R.string.application)
				
				SHOW_CLAIM_CHAT_ACTION -> {
					val incidentDate = intent.getStringExtra(EXTRA_DATE)
					if (incidentDate != null) {
						setClaimTitle(incidentDate)
					} else {
						setTitle(R.string.claim)
					}
					
				}
				SHOW_CONVERSATION_CHAT -> {
					setTitle(R.string.private_conversation)
					needHideSendImage = false
					if (mTitle != null) {
						mTitle!!.setText(R.string.private_conversation)
					}
					
					if (mSubtitle != null) {
						mSubtitle!!.text = intent.getStringExtra(EXTRA_USER_NAME)
					}
					
					val mImageUri = intent.getStringExtra(EXTRA_IMAGE_URI)
					if (mImageUri != null && mIcon != null) {
						GlideApp.with(this).load(getImageLoader()!!.getImageUrl(mImageUri))
								.apply(RequestOptions().transforms(CenterCrop(), CircleCrop())
										.placeholder(R.drawable.picture_background_circle))
								.into(mIcon!!)
						//mIcon.setOnClickListener(v -> TeammateActivity.start(this, mTeamId, mUserId, intent.getStringExtra(EXTRA_USER_NAME), mImageUri));
					}
				}
				
				SHOW_FEED_CHAT_ACTION -> setTitle(intent.getStringExtra(EXTRA_TITLE))
			}
		}
		
		when (intent.getIntExtra(EXTRA_TEAM_ACCESS_LEVEL, TeambrellaModel.TeamAccessLevel.FULL_ACCESS)) {
			TeambrellaModel.TeamAccessLevel.FULL_ACCESS -> isFullAccess = true
			else -> {
				needHideSendImage = true
				if (!isMyChat) {
					needShowContinueJoining = true
				}
			}
		}
		
		findViewById<View>(R.id.inputActive).visibility = if (needShowContinueJoining) View.GONE else View.VISIBLE
		findViewById<View>(R.id.continueJoining).visibility = if (needShowContinueJoining) View.VISIBLE else View.GONE
		
		if (needHideSendImage!!) {
			findViewById<View>(R.id.send_image).visibility = View.GONE
			val params = mMessageView!!.layoutParams as RelativeLayout.LayoutParams
			params.leftMargin = resources.getDimensionPixelSize(R.dimen.margin_8)
			mMessageView!!.layoutParams = params
		}
		
		findViewById<View>(R.id.send_text).visibility = View.GONE
		mMessageView!!.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(s: Editable) {
				val text = mMessageView!!.text.toString().trim { it <= ' ' }
				//Log.v(LOG_TAG, "[anim] A(" + text + ")");
				if (text.length > 0) {
					setAnimation(false, findViewById(R.id.add_photos))
					setAnimation(true, findViewById(R.id.send_text))
				} else {
					setAnimation(true, findViewById(R.id.add_photos))
					setAnimation(false, findViewById(R.id.send_text))
				}
			}
			
			override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
				val text = mMessageView!!.text.toString().trim { it <= ' ' }
				//Log.v(LOG_TAG, "[anim] B(" + text + ")");
			}
			
			override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
				val text = mMessageView!!.text.toString().trim { it <= ' ' }
				//Log.v(LOG_TAG, "[anim] C(" + text + ")");
			}
		})
		
		findViewById<View>(R.id.continueJoining).setOnClickListener { v -> startActivity(MainActivity.getLaunchIntent(this, MainActivity.ACTION_SHOW_MY_TOPIC, mTeamId)) }
		
		mClient = ChatNotificationClient(this)
		mClient!!.connect()
		
		setResult(RESULT_OK)
	}
	
	protected fun setAnimation(fadeIn: Boolean, view: View) {
		val anim = AlphaAnimation((if (fadeIn) 0 else 1).toFloat(), (if (fadeIn) 1 else 0).toFloat())
		anim.interpolator = if (fadeIn) DecelerateInterpolator() else AccelerateInterpolator()
		anim.duration = 100
		anim.setAnimationListener(object : Animation.AnimationListener {
			override fun onAnimationStart(animation: Animation) {
				if (fadeIn) {
					Log.v(LOG_TAG, "[anim] setAnimation: onAnimationStart(fadeIn) $view")
					view.visibility = View.VISIBLE
				}
			}
			
			override fun onAnimationEnd(animation: Animation) {
				if (!fadeIn) {
					Log.v(LOG_TAG, "[anim] setAnimation: onAnimationStart(fadeOut) $view")
					view.visibility = View.GONE
				}
				view.animation = null
			}
			
			override fun onAnimationRepeat(animation: Animation) {}
		})
		if (view.visibility == View.GONE && fadeIn || view.visibility == View.VISIBLE && !fadeIn) {
			Log.v(LOG_TAG, "[anim] setAnimation: setAnimation() $view")
			view.startAnimation(anim)
		}
	}
	
	override fun onPostCreate(savedInstanceState: Bundle?) {
		super.onPostCreate(savedInstanceState)
		getPager(DATA_FRAGMENT_TAG).dataObservable
				.observe(this, Observer { it?.let { this.onDataUpdated(it) }})
	}
	
	override fun onDestroy() {
		super.onDestroy()
		mClient!!.disconnect()
	}
	
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		if (mAction == null || mAction != SHOW_CONVERSATION_CHAT) {
			if (mMuteStatus != null) {
				
//				if (canBeInMarksOnlyMode) {
//
//				} else
				if (isFullAccess) {
					menu.add(0, R.id.pin, 0, null)
							.setIcon(R.drawable.ic_pin_grey).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
				}
				
				when (mMuteStatus) {
					IChatActivity.MuteStatus.DEFAULT, IChatActivity.MuteStatus.MUTED -> menu.add(0, R.id.unmute, 0, null)
							.setIcon(R.drawable.ic_icon_bell_muted).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
					IChatActivity.MuteStatus.UMMUTED -> menu.add(0, R.id.mute, 0, null)
							.setIcon(R.drawable.ic_icon_bell).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
				}
			}
		}
		return super.onCreateOptionsMenu(menu)
	}
	
	
	private fun onSendText(v: View) {
		val text = mMessageView!!.text.toString().trim { it <= ' ' }
		if (!TextUtils.isEmpty(text)) {
			val model = ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel::class.java)
			when (mAction) {
				SHOW_CONVERSATION_CHAT -> {
					val uuid = UUID.randomUUID().toString()
					model.addPendingMessage(uuid, text, -1f)
					queuedRequest(TeambrellaUris.getNewConversationMessageUri(mUserId, uuid, text, null))
					StatisticHelper.onPrivateMessage(this)
				}
				else -> {
					val uuid = UUID.randomUUID().toString()
					model.addPendingMessage(uuid, text, mVote)
					queuedRequest(TeambrellaUris.getNewPostUri(mTopicId, uuid, text, null))
					StatisticHelper.onChatMessage(this, mTeamId, mTopicId, StatisticHelper.MESSAGE_TEXT)
				}
			}
		}
		mMessageView!!.text = null
	}
	
	
	fun startImagePicking() {
		mImagePicker!!.startPicking(getString(R.string.choose))
	}
	
	fun startTakingPhoto() {
		mImagePicker!!.startTakingPhoto(getString(R.string.choose))
	}
	
	protected fun getRequestUri(response: Notification<JsonObject>): Uri {
		val requestUriString = Observable.just<JsonObject>(response.value)
				.map<JsonWrapper>( { JsonWrapper(it) })
				.map { jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS) }
				.map { jsonWrapper -> jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI) }
				.blockingFirst(null)
		return Uri.parse(requestUriString)
	}
	
	@Synchronized
	fun restoreUrisToProcess() {
		val storedUris = TeambrellaUser.get(this).pendingUris
		for (uri in storedUris) {
			if (TeambrellaUris.sUriMatcher.match(uri) == UriMatcher.NO_MATCH) {
				continue
			}
			queuedRequest(uri)
		}
	}
	
	@Synchronized
	fun queuedRequest(uri: Uri) {
		urisToProcess.add(uri)
		TeambrellaUser.get(this).pendingUris = urisToProcess
		
		if (requestInProcess == null) {
			requestInProcess = uri
			request(uri)
		}
	}
	
	@Synchronized
	override fun onRequestResult(response: Notification<JsonObject>) {
		val isServerError = response.error != null && response.error is TeambrellaServerException
		
		if (response.isOnNext || isServerError && response.isOnError) {
			var uri: Uri? = null
			if (isServerError) {
				val exception = response.error as TeambrellaException
				uri = exception.uri
			} else {
				uri = getRequestUri(response)
			}
			
			if (uri == requestInProcess) {
				requestInProcess = null
			}
			Log.v(LOG_TAG, "(onRequestResult) Removing Uri: " + uri!!)
			urisToProcess.remove(uri)
			TeambrellaUser.get(this).pendingUris = urisToProcess
			
			if (!isServerError) {
				// Process request
				when (TeambrellaUris.sUriMatcher.match(uri)) {
					TeambrellaUris.NEW_FILE,
					TeambrellaUris.NEW_FILE_CONVERSATION -> {
						val array = Observable.just<JsonObject>(response.value)
								.map<JsonWrapper>( { JsonWrapper(it) })
								.map { jsonWrapper -> jsonWrapper.getJsonArray(TeambrellaModel.ATTR_DATA) }.blockingFirst()
						var uriNew: Uri? = null
						when (mAction) {
							SHOW_CONVERSATION_CHAT -> {
								uriNew = TeambrellaUris.getNewConversationMessageUri(mUserId, uri.getQueryParameter(TeambrellaUris.KEY_ID), null, array.toString())
							}
							else -> {
								uriNew = TeambrellaUris.getNewPostUri(mTopicId, uri.getQueryParameter(TeambrellaUris.KEY_ID), null, array.toString())
							}
						}
						Log.v(LOG_TAG, "(onRequestResult) Adding Uri: " + uriNew!!)
						urisToProcess.add(uriNew)
						StatisticHelper.onChatMessage(this, mTeamId, mTopicId, StatisticHelper.MESSAGE_IMAGE)
					}
					TeambrellaUris.NEW_POST,
					TeambrellaUris.NEW_PRIVATE_MESSAGE -> {
						getPager(DATA_FRAGMENT_TAG).loadNext(true)
					}
					TeambrellaUris.MUTE -> {
						Observable.fromArray<JsonObject>(response.value)
							.map<JsonWrapper>({ JsonWrapper(it) })
							.map { jsonWrapper -> jsonWrapper.getBoolean(TeambrellaModel.ATTR_DATA, false) }
							.doOnNext { isMuted ->
								mMuteStatus = if (isMuted) IChatActivity.MuteStatus.MUTED else IChatActivity.MuteStatus.UMMUTED
								invalidateOptionsMenu()
							}.blockingFirst()
					}
				}
			}
			
			
			val iter = urisToProcess.iterator()
			while (iter.hasNext()) {
				val storedUri = iter.next()
				// Process Files last, updates posts as they come
				val uriType = TeambrellaUris.sUriMatcher.match(storedUri)
				if (uriType != TeambrellaUris.NEW_FILE && uriType != TeambrellaUris.NEW_FILE_CONVERSATION) {
					Log.v(LOG_TAG, "(onRequestResult) Processing Uri-1: $uri")
					queuedRequest(storedUri)
					return
				}
			}
			if (urisToProcess.size > 0) {
				Log.v(LOG_TAG, "(onRequestResult) Processing Uri-2: $uri")
				queuedRequest(urisToProcess.iterator().next())
			}
		}
		
		if (!isServerError && response.isOnError) {
			val exception = response.error as TeambrellaException
			val uri = exception.uri
			if (uri == requestInProcess) {
				requestInProcess = null
			}
			when (TeambrellaUris.sUriMatcher.match(uri)) {
				TeambrellaUris.NEW_FILE,
				TeambrellaUris.NEW_FILE_CONVERSATION,
				TeambrellaUris.NEW_POST,
				TeambrellaUris.DELETE_POST,
				TeambrellaUris.NEW_PRIVATE_MESSAGE,
				TeambrellaUris.SET_POST_LIKE ->
					{
						Log.v(LOG_TAG, "(onRequestResult - error) Adding Uri: $uri")
						urisToProcess.add(uri)
						showSnackBar(exception)
					}
			}
		}
	}
	
	
	private fun showSnackBar(exception: TeambrellaException) {
		@StringRes val message: Int
		
		var shouldRetry: Boolean? = false
		if (exception is TeambrellaClientException) {
			shouldRetry = true
			val cause = exception.cause
			message = if (cause is SocketTimeoutException || cause is UnknownHostException)
				R.string.no_internet_connection
			else
				R.string.something_went_wrong_error
		} else {
			message = R.string.something_went_wrong_error
		}
		
		if (mSnackBar == null && shouldRetry!!) {
			val uri = exception.uri
			mContainer = findViewById(R.id.container)
			mSnackBar = Snackbar.make(mContainer!!, message, Snackbar.LENGTH_INDEFINITE)
					.setAction(R.string.retry) { v ->
						when (TeambrellaUris.sUriMatcher.match(uri)) {
							TeambrellaUris.NEW_FILE,
							TeambrellaUris.NEW_FILE_CONVERSATION,
							TeambrellaUris.NEW_POST,
							TeambrellaUris.DELETE_POST,
							TeambrellaUris.NEW_PRIVATE_MESSAGE,
							TeambrellaUris.SET_POST_LIKE ->
								if (urisToProcess.size > 0) {
									Log.v(LOG_TAG, "(showSnackBar) Processing Uri-1: $uri")
									queuedRequest(urisToProcess.iterator().next())
								}
								else -> {
									val pager = getPager(DATA_FRAGMENT_TAG)
									pager.reload(uri)
								}
						}
					}
					.setActionTextColor(resources.getColor(R.color.lightGold))
			
			mSnackBar!!.addCallback(object : Snackbar.Callback() {
				override fun onShown(sb: Snackbar?) {
					(sb!!.view.layoutParams as CoordinatorLayout.LayoutParams).behavior = null
				}
				
				override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
					super.onDismissed(transientBottomBar, event)
					mContainer!!.translationY = 0.0f
					mContainer!!.setPadding(0, -Math.round(0f), 0, 0)
					mSnackBar = null
				}
				
			})
			mSnackBar!!.show()
		}
	}
	
	@SuppressLint("CheckResult")
	private fun onDataUpdated(response: Notification<JsonObject>) {

		fun processUpdate(data: JsonObject) {
			val discussion = data.discussionPart
			mVote = data.voting?.myVote ?: mVote
			mTeammateId = data.basic?.teammateId ?: mTeammateId
			mUserName = data.basic?.name ?: mUserName
			mTopicId = discussion.topicId ?: mTopicId
			
			if (mAction == SHOW_CLAIM_CHAT_ACTION) {
				val incidentDate = data.basic?.incidentDate
				if (incidentDate != null) {
					setClaimTitle(incidentDate)
				}
			}
			if (discussion.muted != null) {
				if (discussion.muted ?: false) {
					mMuteStatus = IChatActivity.MuteStatus.MUTED
				} else {
					if (mAction != null && mAction != SHOW_CONVERSATION_CHAT) {
						if (mMuteStatus == IChatActivity.MuteStatus.DEFAULT) {
							showNotificationHelp()
						}
					}
					mMuteStatus = IChatActivity.MuteStatus.UMMUTED
				}
			} else {
				if (mMuteStatus == null) {
					mMuteStatus = IChatActivity.MuteStatus.DEFAULT
				}
			}
			
			invalidateOptionsMenu()
			
			val lastRead = discussion.lastRead ?: -1L
			if (lastRead > mLastRead && mAction != null) {
				when (mAction) {
					SHOW_CONVERSATION_CHAT -> {
						mChatBroadCastManager!!.notifyPrivateMessageRead(mUserId!!)
						mNotificationManager!!.cancelPrivateChatNotification(mUserId)
					}
					else -> { mChatBroadCastManager!!.notifyTopicRead(mTopicId!!) }
				}
			}
			mLastRead = lastRead
		}
		
		if (response.isOnNext) {
			if (mTopicId != null) {
				mNotificationManager!!.cancelChatNotification(mTopicId)
			}
			Observable.fromArray<JsonObject>(response.value)
					.map { value -> value.data }
					.doOnNext { data ->
						data?.let { processUpdate(data) }
					}.blockingFirst()
			
			// Let chat load first
			if (!restoredUris) {
				restoredUris = true
				restoreUrisToProcess()
			}
		} else {
			if (response.error is TeambrellaException) {
				showSnackBar(response.error as TeambrellaException)
			}
		}
	}
	
	override fun onPause() {
		super.onPause()
		mClient?.onPause()
	}
	
	
	override fun onResume() {
		super.onResume()
		mClient?.onResume()
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		when (item.itemId) {
			android.R.id.home -> {
				onBackPressed()
				return true
			}
			R.id.mute, R.id.unmute -> {
				showNotificationSettings()
				return true
			}
			R.id.pin -> {
				showPinTopicDialog()
				return true
			}
		}
		return super.onOptionsItemSelected(item)
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
		super.onActivityResult(requestCode, resultCode, data)
		
		val result = mImagePicker!!.onActivityResult(requestCode, resultCode, data)
		if (result != null) {
			result.subscribeAutoDispose({ descriptor ->
				val uuid = UUID.randomUUID().toString()
				ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel::class.java)
						.addPendingImage(uuid, descriptor.file.getAbsolutePath(), descriptor.ratio, descriptor.cameraUsed)
				when (mAction) {
					SHOW_CONVERSATION_CHAT -> {
						queuedRequest(TeambrellaUris.getNewConversationFileUri(descriptor.file.getAbsolutePath(), uuid))
					}
					else -> {
						queuedRequest(TeambrellaUris.getNewFileUri(descriptor.file.getAbsolutePath(), uuid, descriptor.cameraUsed))
					}
				}
				null
			}, { throwable -> null }// SnakBar is shown in onRequestResult
					, { null })
		} else {
			getPager(DATA_FRAGMENT_TAG).loadNext(true)
			if (mAction == SHOW_CLAIM_CHAT_ACTION) {
				load(CLAIM_DATA_TAG)
			}
		}
	}
	
	fun deletePost(postId: String) {
		ViewModelProviders.of(this).get(DATA_FRAGMENT_TAG, ChatViewModel::class.java).deleteMyImage(postId)
		queuedRequest(TeambrellaUris.getDeletePostUri(postId))
	}
	
	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		mImagePicker!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
	}
	
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putLong(EXTRA_LAST_READ, mLastRead)
	}
	
	override fun getDataConfig(tag: String): Bundle? {
		when (tag) {
			CLAIM_DATA_TAG -> return getDataConfig(TeambrellaUris.getClaimUri(intent.getIntExtra(EXTRA_CLAIM_ID, -1)))
			VOTE_DATA_TAG -> return getDataConfig()
			PIN_UNPIN_DATA -> return getDataConfig(TeambrellaUris.getTopicPinUri(intent.getStringExtra(EXTRA_TOPIC_ID)), true)
		}
		return super.getDataConfig(tag)
	}
	
	override fun getDataPagerConfig(tag: String): Bundle? {
		if (tag == DATA_FRAGMENT_TAG) {
			return getPagerConfig(mUri!!)
		}
		return super.getDataPagerConfig(tag)
	}
	
	override fun <T : TeambrellaPagerViewModel> getPagerViewModelClass(tag: String): Class<T>? {
		if (tag == DATA_FRAGMENT_TAG) {
			return ChatViewModel::class.java as Class<T>
		}
		return super.getPagerViewModelClass(tag)
	}
	
	override fun setTitle(title: CharSequence) {
		val s = SpannableString(title)
		s.setSpan(AkkuratBoldTypefaceSpan(this), 0, s.length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
		super.setTitle(s)
	}
	
	private fun showNotificationHelp() {
		mNotificationHelpView!!.visibility = View.VISIBLE
		mNotificationHelpView!!.postDelayed({ mNotificationHelpView!!.visibility = View.GONE }, 5000)
	}
	
	
	private fun showNotificationSettings() {
		val fragmentManager = supportFragmentManager
		if (fragmentManager.findFragmentByTag(NOTIFICATION_SETTINGS_FRAGMENT_TAG) == null) {
			NotificationsSettingsDialogFragment.instance.show(fragmentManager, NOTIFICATION_SETTINGS_FRAGMENT_TAG)
		}
	}
	
	
	private fun showPinTopicDialog() {
		val fragmentManager = supportFragmentManager
		if (fragmentManager.findFragmentByTag(PIN_UNPIN_FRAGMENT_TAG) == null) {
			PinTopicDialogFragment().show(fragmentManager, PIN_UNPIN_FRAGMENT_TAG)
		}
	}
	
	fun showMessageMenuDialog(item: JsonObject) {
		val fragmentManager = supportFragmentManager
		if (fragmentManager.findFragmentByTag(MESSAGE_MENU_FRAGMENT_TAG) == null) {
			MessageMenuDialogFragment.getInstance(item).show(fragmentManager, MESSAGE_MENU_FRAGMENT_TAG)
		}
	}
	
	
	private inner class ChatNotificationClient internal constructor(context: Context) : TeambrellaNotificationServiceClient(context) {
		
		private var mResumed: Boolean = false
		private var mReloadOnResume: Boolean = false
		
		
		override fun onPushMessage(message: INotificationMessage): Boolean {
			
			val messageTopicId = message.topicId
			val messageUserId = message.senderUserId
			
			when (message.cmd) {
				CREATED_POST -> when (mAction) { SHOW_CLAIM_CHAT_ACTION, SHOW_FEED_CHAT_ACTION, SHOW_TEAMMATE_CHAT_ACTION ->
					if (messageTopicId == mTopicId
						&& messageUserId != null
						&& messageUserId != TeambrellaUser.get(this@ChatActivity).userId) {
						if (mResumed) {
							getPager(DATA_FRAGMENT_TAG).loadNext(true)
						} else {
							mReloadOnResume = true
						}
						return mResumed
					}
				}
				
				PRIVATE_MSG -> if (mAction == SHOW_CONVERSATION_CHAT && messageUserId == mUserId) {
					if (mResumed) {
						getPager(DATA_FRAGMENT_TAG).loadNext(true)
					} else {
						mReloadOnResume = true
					}
					return mResumed
				}
				
				TOPIC_MESSAGE_NOTIFICATION -> {
					if (messageTopicId == mTopicId) {
						if (!mResumed) {
							mReloadOnResume = true
						} else {
							getPager(DATA_FRAGMENT_TAG).loadNext(true)
						}
					}
					return mResumed && messageTopicId != null && messageTopicId == mTopicId
				}
			}
			
			return false
		}
		
		fun onResume() {
			mResumed = true
			if (mReloadOnResume) {
				getPager(DATA_FRAGMENT_TAG).loadNext(true)
				mReloadOnResume = false
			}
		}
		
		fun onPause() {
			mResumed = false
			
		}
	}
	
	override fun setChatMuted(muted: Boolean) {
		request(TeambrellaUris.getSetChatMutedUri(mTopicId, muted))
	}
	
	override fun setMyMessageVote(postId: String, vote: Int) {
		chatViewModel.setMyMessageVote(postId, vote)
		queuedRequest(TeambrellaUris.getSetPostLikeUri(postId, vote))
	}
	
	override fun setMarkedPost(postId: String, isMarked: Boolean?) {
		chatViewModel.setMarked(postId, isMarked!!)
		queuedRequest(TeambrellaUris.getSetMarkedUri(postId, isMarked))
	}
	
	override fun setMainProxy(userId: String) {
		chatViewModel.setProxy(userId, true, true)
		queuedRequest(TeambrellaUris.getSetProxyPositionUri(0, userId, teamId))
	}
	
	override fun addProxy(userId: String) {
		chatViewModel.setProxy(userId, true, false)
		queuedRequest(TeambrellaUris.getSetMyProxyUri(userId, true))
	}
	
	override fun removeProxy(userId: String) {
		chatViewModel.setProxy(userId, false, false)
		queuedRequest(TeambrellaUris.getSetMyProxyUri(userId, false))
	}
	
	override fun setTitle(title: String) {
		super.setTitle(title)
	}
	
	override fun setSubtitle(subtitle: String) {
		// nothing to do
	}
	
	override fun postVote(vote: Int) {
		ViewModelProviders.of(this).get(VOTE_DATA_TAG, TeambrellaDataViewModel::class.java)
				.load(TeambrellaUris.getClaimVoteUri(intent.getIntExtra(EXTRA_CLAIM_ID, -1), vote))
		TeambrellaBroadcastManager(this).notifyClaimVote(claimId)
	}
	
	override fun showSnackBar(text: Int) {
		// nothing to do
	}
	
	override fun launchActivity(intent: Intent) {
		// nothing to do
	}
	
	
	override fun pinTopic() {
		pinUnpinViewModel.load(TeambrellaUris.getUpdateTopicUri(intent.getStringExtra(EXTRA_TOPIC_ID), 1))
	}
	
	override val pinTopicObservable; get() = getObservable(PIN_UNPIN_DATA)
	
	override fun unpinTopic() {
		pinUnpinViewModel.load(TeambrellaUris.getUpdateTopicUri(intent.getStringExtra(EXTRA_TOPIC_ID), -1))
	}
	
	override fun resetPin() {
		pinUnpinViewModel.load(TeambrellaUris.getUpdateTopicUri(intent.getStringExtra(EXTRA_TOPIC_ID), 0))
	}
	
	private fun setClaimTitle(incidentDate: String) {
		val date = TeambrellaDateUtils.getDate(incidentDate)
		val current = Date()
		val isTheSameYear = date != null && date.year == current.year
		setTitle(getString(R.string.claim_title_date_format_string, TeambrellaDateUtils.getDatePresentation(this, if (isTheSameYear) TeambrellaDateUtils.TEAMBRELLA_UI_DATE_CHAT_SHORT else TeambrellaDateUtils.TEAMBRELLA_UI_DATE, incidentDate)))
	}
	
	companion object {
		
		private val LOG_TAG = ChatActivity::class.java.simpleName
		
		private val EXTRA_URI = "uri"
		private val EXTRA_TOPIC_ID = "topicId"
		private val EXTRA_TEAM_ID = "extra_team_id"
		private val EXTRA_USER_ID = "user_id"
		private val EXTRA_USER_NAME = "user_name"
		private val EXTRA_IMAGE_URI = "image_uri"
		private val EXTRA_CLAIM_ID = "claim_id"
		private val EXTRA_OBJECT_NAME = "object_name"
		private val EXTRA_TEAM_ACCESS_LEVEL = "team_access_level"
		private val EXTRA_TITLE = "title"
		private val EXTRA_DATE = "date"
		private val EXTRA_LAST_READ = "las_read"
		private val EXTRA_ACTION = "chat_action"
		
		
		private val DATA_FRAGMENT_TAG = "data_fragment_tag"
		private val PIN_UNPIN_DATA = "pin_unpin_data_fragment"
		private val UI_FRAGMENT_TAG = "ui_fragment_tag"
		val CLAIM_DATA_TAG = "claim_data_tag"
		val VOTE_DATA_TAG = "vote_data_tag"
		private val NOTIFICATION_SETTINGS_FRAGMENT_TAG = "notification_settings"
		private val PIN_UNPIN_FRAGMENT_TAG = "pin_unpin"
		private val MESSAGE_MENU_FRAGMENT_TAG = "message_menu"
		
		private val SHOW_TEAMMATE_CHAT_ACTION = "show_teammate_chat_action"
		private val SHOW_CLAIM_CHAT_ACTION = "show_claim_chat_action"
		private val SHOW_FEED_CHAT_ACTION = "show_feed_chat_action"
		private val SHOW_CONVERSATION_CHAT = "show_conversation_chat_action"
		
		fun startConversationChat(context: Context, userId: String?, userName: String?, imageUri: String?) {
			context.startActivity(getConversationChat(context, userId, userName, imageUri))
		}
		
		fun getConversationChat(context: Context?, userId: String?, userName: String?, imageUri: String?): Intent {
			return Intent(context, ChatActivity::class.java)
					.putExtra(EXTRA_USER_ID, userId)
					.putExtra(EXTRA_URI, TeambrellaUris.getConversationChatUri(userId))
					.putExtra(EXTRA_USER_NAME, userName)
					.putExtra(EXTRA_IMAGE_URI, imageUri)
					.putExtra(EXTRA_ACTION, SHOW_CONVERSATION_CHAT)
					.setAction(SHOW_CONVERSATION_CHAT + userId)
		}
		
		fun getClaimChat(context: Context?, teamId: Int, claimId: Int, objectName: String?, imageUri: String?, topicId: String?, accessLevel: Int, date: String?): Intent {
			return Intent(context, ChatActivity::class.java)
					.putExtra(EXTRA_TEAM_ID, teamId)
					.putExtra(EXTRA_CLAIM_ID, claimId)
					.putExtra(EXTRA_OBJECT_NAME, objectName)
					.putExtra(EXTRA_IMAGE_URI, imageUri)
					.putExtra(EXTRA_TOPIC_ID, topicId)
					.putExtra(EXTRA_URI, TeambrellaUris.getClaimChatUri(claimId))
					.putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
					.putExtra(EXTRA_DATE, date)
					.putExtra(EXTRA_ACTION, SHOW_CLAIM_CHAT_ACTION)
					.setAction(SHOW_CLAIM_CHAT_ACTION + topicId)
		}
		
		fun getTeammateChat(context: Context?, teamId: Int, userId: String?, userName: String?, imageUri: String?, topicId: String?, accessLevel: Int): Intent {
			return Intent(context, ChatActivity::class.java)
					.putExtra(EXTRA_TEAM_ID, teamId)
					.putExtra(EXTRA_USER_ID, userId)
					.putExtra(EXTRA_USER_NAME, userName)
					.putExtra(EXTRA_IMAGE_URI, imageUri)
					.putExtra(EXTRA_TOPIC_ID, topicId)
					.putExtra(EXTRA_URI, TeambrellaUris.getTeammateChatUri(teamId, userId))
					.putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
					.putExtra(EXTRA_ACTION, SHOW_TEAMMATE_CHAT_ACTION)
					.setAction(SHOW_TEAMMATE_CHAT_ACTION + topicId)
		}
		
		
		fun getFeedChat(context: Context?, title: String?, topicId: String?, teamId: Int, accessLevel: Int): Intent {
			return Intent(context, ChatActivity::class.java)
					.putExtra(EXTRA_TEAM_ID, teamId)
					.putExtra(EXTRA_TOPIC_ID, topicId)
					.putExtra(EXTRA_TITLE, title)
					.putExtra(EXTRA_URI, TeambrellaUris.getFeedChatUri(topicId))
					.putExtra(EXTRA_TEAM_ACCESS_LEVEL, accessLevel)
					.putExtra(EXTRA_ACTION, SHOW_FEED_CHAT_ACTION)
					.setAction(SHOW_FEED_CHAT_ACTION + topicId)
		}
	}
}
