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

class MessageMenuDialogFragment : BottomSheetDialogFragment() {

    private var chatActivity: IChatActivity? = null

    private lateinit var upvoteTitleView: TextView
    private lateinit var downvoteTitleView: TextView
    private lateinit var isUpvoteSelectedView: View
    private lateinit var isDownvoteSelectedView: View
    private lateinit var upvoteMessageView: View
    private lateinit var downvoteMessageView: View
    private lateinit var postId : String
    private var vote : Int = 0

    companion object {
        fun getInstance(postId: String, vote: Int): MessageMenuDialogFragment {
            var fragment = MessageMenuDialogFragment()
            fragment.postId = postId
            fragment.vote = vote
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

        isUpvoteSelectedView.visibility = if (vote > 0) View.VISIBLE else View.GONE
        isDownvoteSelectedView.visibility = if (vote < 0) View.VISIBLE else View.GONE

        upvoteMessageView.setOnClickListener {
            chatActivity?.setMyMessageVote(postId, if (vote > 0) 0 else 1)
            dismiss()
        }

        downvoteMessageView.setOnClickListener {
            chatActivity?.setMyMessageVote(postId, if (vote < 0) 0 else -1)
            dismiss()
        }

        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(view)
        return dialog
    }
}