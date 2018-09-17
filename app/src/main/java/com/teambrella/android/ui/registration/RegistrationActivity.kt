package com.teambrella.android.ui.registration

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.EditText
import com.teambrella.android.R


fun startRegistration(context: Context) {
    context.startActivity(Intent(context, RegistrationActivity::class.java))
}

class RegistrationActivity : AppCompatActivity() {

    private lateinit var modelView: AutoCompleteTextView
    private lateinit var locationView: AutoCompleteTextView
    private lateinit var nameView: EditText
    private lateinit var emailView: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.activity_registration)
        modelView = findViewById(R.id.model_value)
        locationView = findViewById(R.id.location_value)
        nameView = findViewById(R.id.name_value)
        emailView = findViewById(R.id.email_value)
        modelView.setAdapter(CarAdapter(this))
        locationView.setAdapter(CityAdapter(this))
        locationView.onItemClickListener = ItemClickListener()
        modelView.onItemClickListener = ItemClickListener()


    }

    private inner class ItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val focus = currentFocus
            if (focus != null) {
                val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(focus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

            }
        }
    }
}