package com.teambrella.android.ui.chat

import android.app.Dialog
import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import io.reactivex.Notification

class PinTopicDialogFragment : BottomSheetDialogFragment() {

    private var chatActivity: IChatActivity? = null

    private lateinit var pinTitleView: TextView
    private lateinit var pinTextView: TextView
    private lateinit var unpinTitleView: TextView
    private lateinit var unpinTextView: TextView
    private lateinit var isPinSelectedView: View
    private lateinit var isUnpinSelectedView: View
    private lateinit var pinTopicView: View
    private lateinit var unpinTopicView: View


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        chatActivity = context as IChatActivity
    }

    override fun onStart() {
        super.onStart()
        chatActivity?.pinTopicObservable?.observeForever(observer)
    }

    override fun onStop() {
        super.onStop()
        chatActivity?.pinTopicObservable?.removeObserver(observer)
    }

    override fun onDetach() {
        super.onDetach()
        chatActivity = null;
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
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_pin_topic, null, false)

        view.findViewById<View>(R.id.close).setOnClickListener { dismiss() }

        pinTitleView = view.findViewById(R.id.pin_title)
        pinTextView = view.findViewById(R.id.pin_text)
        unpinTitleView = view.findViewById(R.id.unpin_title)
        unpinTextView = view.findViewById(R.id.unpin_text)
        isPinSelectedView = view.findViewById(R.id.is_pin_selected)
        isUnpinSelectedView = view.findViewById(R.id.is_unpin_selected)
        pinTopicView = view.findViewById(R.id.pin_topic)
        unpinTopicView = view.findViewById(R.id.unpin_topic)

        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        return dialog
    }


    private fun onDataUpdated(notification: Notification<JsonObject>?) {
        if (notification?.isOnNext == true) {
            val data = notification.value.data
            pinTextView.text = data.pinText
            pinTitleView.text = data.pinTitle
            unpinTextView.text = data.unpinText
            unpinTitleView.text = data.unpinTitle
            val myPin = data.myPin ?: 0
            isPinSelectedView.visibility = if (myPin > 0) View.VISIBLE else View.GONE
            isUnpinSelectedView.visibility = if (myPin < 0) View.VISIBLE else View.GONE

            pinTopicView.setOnClickListener {
                if (myPin > 0) {
                    chatActivity?.resetPin()
                } else {
                    chatActivity?.pinTopic()
                }
            }

            unpinTopicView.setOnClickListener {
                if (myPin < 0) {
                    chatActivity?.resetPin()
                } else {
                    chatActivity?.unpinTopic()
                }
            }
        }
    }


    private val observer = Observer<Notification<JsonObject>> { this.onDataUpdated(it) }

}