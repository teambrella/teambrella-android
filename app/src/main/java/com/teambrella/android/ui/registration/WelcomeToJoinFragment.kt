package com.teambrella.android.ui.registration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.teambrella.android.BuildConfig
import com.teambrella.android.R
import com.teambrella.android.image.glide.GlideApp

class WelcomeToJoinFragment : Fragment() {

    private lateinit var teamIconView: ImageView
    private lateinit var teamNameView: TextView
    private lateinit var teamCountryView: TextView
    private lateinit var welcomeTitle: TextView
    private lateinit var welcomeDescription: TextView
    private lateinit var facebookLogin: View
    private lateinit var vkLogin: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_to_join, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamIconView = view.findViewById(R.id.team_icon)
        teamNameView = view.findViewById(R.id.team_name)
        teamCountryView = view.findViewById(R.id.team_country)
        facebookLogin = view.findViewById(R.id.facebook_login)
        vkLogin = view.findViewById(R.id.vk_login)
        welcomeTitle = view.findViewById(R.id.welcome_to_join_title)
        welcomeDescription = view.findViewById(R.id.welcome_to_join_description)


        activity?.let { _activity ->
            val viewModel = ViewModelProviders.of(_activity).get(RegistrationViewModel::class.java)
            viewModel.regInfo.observe(this, Observer { regInfo ->
                regInfo?.teamIcon?.let {
                    GlideApp.with(teamIconView).load(Uri.Builder().scheme(BuildConfig.SCHEME).authority(BuildConfig.AUTHORITY).appendEncodedPath(it).build())
                            .apply(RequestOptions().transforms(RoundedCorners(_activity.resources.getDimensionPixelSize(R.dimen.rounded_corners_4dp))))
                            .into(teamIconView)
                }
                teamNameView.text = regInfo?.teamName
                teamCountryView.text = regInfo?.teamCountry

                facebookLogin.setOnClickListener { _ ->
                    viewModel.onFacebookLogin(_activity)
                }

                vkLogin.setOnClickListener { _ ->
                    viewModel.onVkLogin(_activity)
                }

                welcomeTitle.text = regInfo?.welcomeTitle
                welcomeDescription.text = regInfo?.welcomeMessage

            })
        }

    }
}