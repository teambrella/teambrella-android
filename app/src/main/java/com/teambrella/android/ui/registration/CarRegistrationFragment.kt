package com.teambrella.android.ui.registration

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.teambrella.android.BuildConfig
import com.teambrella.android.R
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.ui.registration.cars.CarsAdapter
import com.teambrella.android.ui.registration.location.CityAdapter


class CarRegistrationFragment : Fragment() {

    private lateinit var modelView: AutoCompleteTextView
    private lateinit var locationView: AutoCompleteTextView
    private lateinit var nameView: EditText
    private lateinit var emailView: EditText
    private lateinit var teamIconView: ImageView
    private lateinit var teamNameView: TextView
    private lateinit var teamCountryView: TextView
    private lateinit var agreement: TextView
    private lateinit var registerView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_car_registration, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        modelView = view.findViewById(R.id.model_value)
        locationView = view.findViewById(R.id.location_value)
        nameView = view.findViewById(R.id.name_value)
        emailView = view.findViewById(R.id.email_value)

        teamIconView = view.findViewById(R.id.team_icon)
        teamNameView = view.findViewById(R.id.team_name)
        teamCountryView = view.findViewById(R.id.team_country)
        agreement = view.findViewById(R.id.agreement)
        registerView = view.findViewById(R.id.register)

        activity?.let { _activity ->
            modelView.setAdapter(CarsAdapter(_activity))
            locationView.setAdapter(CityAdapter(_activity))
            locationView.onItemClickListener = LocationClickListener()
            modelView.onItemClickListener = ModelClickListener()
            ViewModelProviders.of(_activity).get(RegistrationViewModel::class.java).regInfo.observe(this, Observer { regInfo ->
                regInfo?.teamIcon?.let {
                    GlideApp.with(teamIconView).load(Uri.Builder().scheme(BuildConfig.SCHEME).authority(BuildConfig.AUTHORITY).appendEncodedPath(it).build())
                            .apply(RequestOptions().transforms(RoundedCorners(_activity.resources.getDimensionPixelSize(R.dimen.rounded_corners_4dp))))
                            .into(teamIconView)
                }
                teamNameView.text = regInfo?.teamName
                teamCountryView.text = regInfo?.teamCountry
                modelView.setText(regInfo?.model)
                locationView.setText(regInfo?.city)
                emailView.setText(regInfo?.email)
                nameView.setText(regInfo?.userName)
            })
            agreement.text = Html.fromHtml(getString(R.string.terms_of_services_agreement))
            agreement.movementMethod = LinkMovementMethod.getInstance()

            registerView.setOnClickListener { register(_activity) }
        }
    }

    private inner class LocationClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            modelView.requestFocus()
        }
    }

    private inner class ModelClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val model = parent?.adapter?.getItem(position)
            if (model is String) {
                model.trim().takeIf {
                    it[it.length - 4].isDigit() && it[it.length - 3].isDigit()
                            && it[it.length - 2].isDigit() && it[it.length - 1].isDigit()
                }.let {
                    if (it != null) {
                        val imm = modelView.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(modelView.windowToken, 0)
                        registerView.requestFocus()
                    } else {
                        modelView.setText(model, true)
                        modelView.postDelayed({ modelView.showDropDown() }, 100)
                    }
                }
            }
        }
    }

    fun register(context: Context) {

        val name = nameView.text?.trim()?.toString()

        if (name == null || name.isEmpty()) {
            nameView.error = getString(R.string.registration_error_name)
            return
        }

        val email = emailView.text?.trim()?.toString()

        if (email == null || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailView.error = getString(R.string.registration_error_email)
            return
        }

        val location = locationView.text?.trim()?.toString()

        if (location == null || location.isEmpty()) {
            locationView.error = getString(R.string.registration_error_location)
            return
        }

        val model = modelView.text?.trim()?.toString()

        if (model == null || model.isEmpty()) {
            modelView.error = getString(R.string.registration_error_model)
            return
        }

        viewModel?.registerUser(context, name, email, location, model)
    }


    private val viewModel: RegistrationViewModel?
        get() = activity?.let { ViewModelProviders.of(it).get(RegistrationViewModel::class.java) }
}