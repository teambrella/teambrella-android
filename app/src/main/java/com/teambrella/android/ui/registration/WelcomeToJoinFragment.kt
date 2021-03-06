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
    private lateinit var inputContainer: View
    private lateinit var letsGoView: View


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_welcome_to_join, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamIconView = view.findViewById(R.id.team_icon)
        teamNameView = view.findViewById(R.id.team_name)
        teamCountryView = view.findViewById(R.id.team_country)

        welcomeTitle = view.findViewById(R.id.welcome_to_join_title)
        welcomeDescription = view.findViewById(R.id.welcome_to_join_description)
        inputContainer = view.findViewById(R.id.input_container)
        letsGoView = view.findViewById(R.id.lets_go)


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

                welcomeTitle.text = regInfo?.welcomeTitle
                welcomeDescription.text = regInfo?.welcomeMessage


                inputContainer.visibility = when (regInfo?.uiState) {
                    UIState.WELCOME_PRELOAD -> View.GONE
                    else -> View.VISIBLE
                }

                letsGoView.setOnClickListener {
                    viewModel.continueRegistration()
                }

            })
        }

    }
}