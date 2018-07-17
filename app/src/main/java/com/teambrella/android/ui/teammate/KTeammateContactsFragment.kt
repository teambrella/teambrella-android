package com.teambrella.android.ui.teammate

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.basic
import com.teambrella.android.api.data
import com.teambrella.android.api.facebookUrl
import com.teambrella.android.ui.base.ADataFragment
import com.teambrella.android.util.log.Log
import io.reactivex.Notification


private const val LOG_TAG: String = "TeammateContactsFragment"

class TeammateContactsFragment : ADataFragment<ITeammateActivity>() {

    private val facebookLink: TextView? by ViewHolder(R.id.facebook_link)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_teammate_contacts, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val data = notification.value?.data?.basic
            data?.let { basic ->
                basic.facebookUrl?.let { str ->

                    var uri: Uri? = null
                    try {
                        uri = Uri.parse(str)
                    } catch (e: Exception) {
                        Log.e(LOG_TAG, e.toString())
                    }
                    Uri.parse(str)
                    val view = this.view?.findViewById<View>(R.id.contacts_panel)
                    facebookLink?.text = "fb.com/" + ((uri?.lastPathSegment) ?: "")
                    view?.setOnClickListener {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW).setData(uri))
                        } catch (e: Exception) {
                            Log.e(LOG_TAG, e.toString())
                        }
                    }
                }
            }
        }
    }
}
