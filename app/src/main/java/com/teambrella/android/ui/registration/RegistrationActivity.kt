package com.teambrella.android.ui.registration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.widget.*
import com.teambrella.android.BuildConfig
import com.teambrella.android.R
import com.teambrella.android.image.glide.GlideApp


fun startRegistration(context: Context) {
    context.startActivity(Intent(context, RegistrationActivity::class.java))
}

class RegistrationActivity : AppCompatActivity() {

    private lateinit var modelView: AutoCompleteTextView
    private lateinit var locationView: AutoCompleteTextView
    private lateinit var nameView: EditText
    private lateinit var emailView: EditText
    private lateinit var teamIconView: ImageView
    private lateinit var teamNameView: TextView
    private lateinit var teamCountryView: TextView
    private lateinit var agreement: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.activity_registration)

        modelView = findViewById(R.id.model_value)
        locationView = findViewById(R.id.location_value)
        nameView = findViewById(R.id.name_value)
        emailView = findViewById(R.id.email_value)

        teamIconView = findViewById(R.id.team_icon)
        teamNameView = findViewById(R.id.team_name)
        teamCountryView = findViewById(R.id.team_country)

        agreement = findViewById(R.id.agreement)

        modelView.setAdapter(CarAdapter(this))
        locationView.setAdapter(CityAdapter(this))
        locationView.onItemClickListener = ItemClickListener()
        modelView.onItemClickListener = ItemClickListener()


        ViewModelProviders.of(this).get(RegistrationViewModel::class.java).regInfo.observe(this, Observer { regInfo ->
            regInfo?.teamIcon?.let {
                GlideApp.with(teamIconView).load(Uri.Builder().scheme(BuildConfig.SCHEME).authority(BuildConfig.AUTHORITY).appendEncodedPath(it).build())
                        .into(teamIconView)
            }
            teamNameView.text = regInfo?.teamName
            teamCountryView.text = regInfo?.teamCountry
        })


        agreement.text = Html.fromHtml(getString(R.string.terms_of_services_agreement))

    }

    private inner class ItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            findViewById<View>(currentFocus.nextFocusForwardId).requestFocus()
        }
    }
}