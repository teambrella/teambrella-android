package com.teambrella.android.ui.chat

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.teambrella.android.R

/**
 * Notifications Settings
 */
class NotificationsSettingsDialogFragment : BottomSheetDialogFragment() {
	
	
	private var mChatActivity: IChatActivity? = null
	
	override fun onAttach(context: Context?) {
		super.onAttach(context)
		mChatActivity = context as IChatActivity?
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		val dialog = object : BottomSheetDialog(context!!, R.style.InfoDialog) {
			override fun onCreate(savedInstanceState: Bundle?) {
				super.onCreate(savedInstanceState)
				val window = window
				if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
					window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
				}
			}
		}
		
		val view = LayoutInflater.from(context).inflate(R.layout.dialog_notifications_setttings, null, false)
		view.findViewById<View>(R.id.close).setOnClickListener { v -> dismiss() }
		
		val list = view.findViewById<LinearLayout>(R.id.list)
		val inflater = LayoutInflater.from(context)
		val muteStatus = if (mChatActivity != null) mChatActivity!!.muteStatus else IChatActivity.MuteStatus.DEFAULT
		val unmute = inflater.inflate(R.layout.list_item_chat_notification_option, list, false)
		
		initListItem(unmute, VIEW_TYPE_UNMUTE, muteStatus == IChatActivity.MuteStatus.UMMUTED)
		list.addView(unmute)
		unmute.setOnClickListener { v ->
			if (mChatActivity != null) {
				mChatActivity!!.setChatMuted(false)
				dismiss()
			}
		}
		val mute = inflater.inflate(R.layout.list_item_chat_notification_option, list, false)
		initListItem(mute, VIEW_TYPE_MUTE, muteStatus == IChatActivity.MuteStatus.MUTED || muteStatus == IChatActivity.MuteStatus.DEFAULT)
		list.addView(mute)
		mute.setOnClickListener { v ->
			if (mChatActivity != null) {
				mChatActivity!!.setChatMuted(true)
				dismiss()
			}
		}
		dialog.setCanceledOnTouchOutside(true)
		dialog.setContentView(view)
		return dialog
	}
	
	override fun onDetach() {
		super.onDetach()
		mChatActivity = null
	}
	
	private fun initListItem(itemView: View, viewType: Int, checked: Boolean) {
		val icon = itemView.findViewById<ImageView>(R.id.icon)
		val title = itemView.findViewById<TextView>(R.id.title)
		val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
		val isSelected = itemView.findViewById<View>(R.id.isSelected)
		when (viewType) {
			VIEW_TYPE_UNMUTE -> {
				icon.setImageResource(R.drawable.ic_icon_bell_green)
				title.setText(R.string.notification_option_unmuted_title)
				subtitle.setText(R.string.notification_option_unmuted_description)
				isSelected.visibility = if (checked) View.VISIBLE else View.INVISIBLE
			}
			VIEW_TYPE_MUTE -> {
				icon.setImageResource(R.drawable.ic_icon_bell_muted_red)
				title.setText(R.string.notification_option_muted_title)
				subtitle.setText(R.string.notification_option_muted_description)
				isSelected.visibility = if (checked) View.VISIBLE else View.INVISIBLE
			}
		}
	}
	
	companion object {
		
		private val VIEW_TYPE_UNMUTE = 0
		private val VIEW_TYPE_MUTE = 1
		
		val instance: NotificationsSettingsDialogFragment
			get() = NotificationsSettingsDialogFragment()
	}
}
