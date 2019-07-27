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
import com.teambrella.android.ui.TeambrellaUser

class MessageMenuDialogFragment : BottomSheetDialogFragment() {

    private var chatActivity: IChatActivity? = null

    private lateinit var upvoteTitleView: TextView
    private lateinit var downvoteTitleView: TextView
    private lateinit var isUpvoteSelectedView: View
    private lateinit var isDownvoteSelectedView: View
    private lateinit var upvoteMessageView: View
    private lateinit var downvoteMessageView: View
    private lateinit var markView: View
    private lateinit var addProxyView: View
    private lateinit var mainProxyView: View
    private lateinit var removeProxyView: View
    private lateinit var item : JsonObject

    companion object {
        fun getInstance(item: JsonObject): MessageMenuDialogFragment {
            var fragment = MessageMenuDialogFragment()
            fragment.item = item
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        chatActivity = context as IChatActivity
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
        val view = LayoutInflater.from(context).inflate(R.layout.fragment_message_menu, null, false)

        upvoteTitleView = view.findViewById(R.id.upvote_title)
        downvoteTitleView = view.findViewById(R.id.downvote_title)
        isUpvoteSelectedView = view.findViewById(R.id.is_upvote_selected)
        isDownvoteSelectedView = view.findViewById(R.id.is_downvote_selected)
        upvoteMessageView = view.findViewById(R.id.upvote)
        downvoteMessageView = view.findViewById(R.id.downvote)
        markView = view.findViewById(R.id.mark)
        addProxyView = view.findViewById(R.id.add_proxy)
        mainProxyView = view.findViewById(R.id.main_proxy)
        removeProxyView = view.findViewById(R.id.remove_proxy)

        val vote = item.myLike ?: 0

        isUpvoteSelectedView.visibility = if (vote > 0) View.VISIBLE else View.GONE
        isDownvoteSelectedView.visibility = if (vote < 0) View.VISIBLE else View.GONE

        // Initial values
        markView.visibility = View.GONE
        upvoteMessageView.visibility = View.GONE
        downvoteMessageView.visibility = View.GONE
        addProxyView.visibility = View.GONE
        mainProxyView.visibility = View.GONE
        removeProxyView.visibility = View.GONE

        val isMy = item.userId == TeambrellaUser.get(context).userId
        if (!isMy) {
            upvoteMessageView.visibility = View.VISIBLE
            upvoteMessageView.setOnClickListener {
                chatActivity?.setMyMessageVote(item.stringId!!, if (vote > 0) 0 else 1)
                dismiss()
            }

            downvoteMessageView.visibility = View.VISIBLE
            downvoteMessageView.setOnClickListener {
                chatActivity?.setMyMessageVote(item.stringId!!, if (vote < 0) 0 else -1)
                dismiss()
            }

            val name = item.teammatePart?.name?.substringBefore(" ")

            if ((item.suggestAddingToProxies ?: false) || vote > 0) {
                if (item.teammatePart?.isMyProxy ?: false) {
                    mainProxyView.visibility = View.VISIBLE
                    (view.findViewById(R.id.main_proxy_title) as? TextView) ?.text = getString(R.string.make_main_proxy_title, name ?: "")
                    (view.findViewById(R.id.main_proxy_text) as? TextView) ?.text = getString(R.string.make_main_proxy_text, name ?: "")
                    mainProxyView.setOnClickListener {
                        chatActivity?.setMainProxy(item.userId!!)
                        dismiss()
                    }
                } else {
                    addProxyView.visibility = View.VISIBLE
                    (view.findViewById(R.id.add_proxy_title) as? TextView) ?.text = getString(R.string.add_proxy_title, name ?: "")
                    (view.findViewById(R.id.add_proxy_text) as? TextView) ?.text = getString(R.string.add_proxy_text, name ?: "")
                    addProxyView.setOnClickListener {
                        chatActivity?.addProxy(item.userId!!)
                        dismiss()
                    }
                }

            }
            if ((item.suggestRemovingFromProxies ?: false) || vote < 0) {
                if (item.teammatePart?.isMyProxy ?: false) {
                    removeProxyView.visibility = View.VISIBLE
                    (view.findViewById(R.id.remove_proxy_title) as? TextView)?.text = getString(R.string.remove_proxy_title, name
                            ?: "")
                    (view.findViewById(R.id.remove_proxy_text) as? TextView)?.text = getString(R.string.remove_proxy_text, name
                            ?: "")
                    removeProxyView.setOnClickListener {
                        chatActivity?.removeProxy(item.userId!!)
                        dismiss()
                    }
                }
            }
        }
        else {
            markView.visibility = View.VISIBLE
            (view.findViewById(R.id.is_mark_selected) as? View)?.visibility = if (item?.marked ?: false) View.VISIBLE else View.GONE
            markView.setOnClickListener {
                chatActivity?.setMarkedPost(item.stringId!!, !(item.marked ?: false))
                dismiss()
            }
        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        return dialog
    }
}