package com.teambrella.android.ui

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.teambrella.android.R
import com.teambrella.android.api.TeambrellaModel

/**
 * Notifications Settings
 */
class TeamNotificationsSettingsDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val VIEW_TYPE_NEVER = 0
        private const val VIEW_TYPE_DAILY = 1
        private const val VIEW_TYPE_ONCE_EVERY_3_DAYS = 2
        private const val VIEW_TYPE_ONCE_A_MONTH = 3
    }


    private var mainDataHost: IMainDataHost? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mainDataHost = context as IMainDataHost?
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
        val teamNotificationSettings = mainDataHost?.teamNotificationSettings
                ?: TeambrellaModel.TeamNotifications.DAILY
        list.addView(inflater.inflate(R.layout.list_item_chat_notification_option, list, false).apply {
            initListItem(this, VIEW_TYPE_DAILY, teamNotificationSettings == TeambrellaModel.TeamNotifications.DAILY)
            setOnClickListener {
                mainDataHost?.teamNotificationSettings = TeambrellaModel.TeamNotifications.DAILY
                dismiss()
            }
        })

        list.addView(inflater.inflate(R.layout.list_item_chat_notification_option, list, false).apply {
            initListItem(this, VIEW_TYPE_ONCE_EVERY_3_DAYS, teamNotificationSettings == TeambrellaModel.TeamNotifications.ONCE_EVERY_3_DAYS)
            setOnClickListener {
                mainDataHost?.teamNotificationSettings = TeambrellaModel.TeamNotifications.ONCE_EVERY_3_DAYS
                dismiss()
            }
        })


        list.addView(inflater.inflate(R.layout.list_item_chat_notification_option, list, false).apply {
            initListItem(this, VIEW_TYPE_ONCE_A_MONTH, teamNotificationSettings == TeambrellaModel.TeamNotifications.ONCE_A_MONTH)
            setOnClickListener {
                mainDataHost?.teamNotificationSettings = TeambrellaModel.TeamNotifications.ONCE_A_MONTH
                dismiss()
            }
        })

        list.addView(inflater.inflate(R.layout.list_item_chat_notification_option, list, false).apply {
            initListItem(this, VIEW_TYPE_NEVER, teamNotificationSettings == TeambrellaModel.TeamNotifications.NEVER)
            setOnClickListener {
                mainDataHost?.teamNotificationSettings = TeambrellaModel.TeamNotifications.NEVER
                dismiss()
            }
        })


        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        return dialog
    }

    override fun onDetach() {
        super.onDetach()
        mainDataHost = null
    }

    private fun initListItem(itemView: View, viewType: Int, checked: Boolean) {
        val icon = itemView.findViewById<ImageView>(R.id.icon)
        val title = itemView.findViewById<TextView>(R.id.title)
        val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
        val isSelected = itemView.findViewById<View>(R.id.isSelected)

        isSelected.visibility = if (checked) View.VISIBLE else View.INVISIBLE

        when (viewType) {
            VIEW_TYPE_DAILY -> {
                icon.setImageResource(R.drawable.ic_icon_bell_green)
                title.setText(R.string.notification_daily_title)
                subtitle.setText(R.string.notification_daily_description)
            }
            VIEW_TYPE_ONCE_EVERY_3_DAYS -> {
                icon.setImageResource(R.drawable.ic_icon_bell_green)
                title.setText(R.string.notification_every_3_days_title)
                subtitle.setText(R.string.notification_every_3_days_description)
            }

            VIEW_TYPE_ONCE_A_MONTH -> {
                icon.setImageResource(R.drawable.ic_icon_bell_green)
                title.setText(R.string.notification_once_a_month_title)
                subtitle.setText(R.string.notification_once_a_month_description)
            }

            VIEW_TYPE_NEVER -> {
                icon.setImageResource(R.drawable.ic_icon_bell_muted_red)
                title.setText(R.string.notification_every_never_title)
                subtitle.setText(R.string.notification_every_never_description)
            }


        }
    }
}
