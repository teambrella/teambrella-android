package com.teambrella.android.ui.teammate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.ui.base.AKDataFragment
import io.reactivex.Notification


class TeammateContactsFragment : AKDataFragment<ITeammateActivity>() {


    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_teammate_contacts, container, false)
    }

    override fun onDataUpdated(notification: Notification<JsonObject>?) {

    }
}
