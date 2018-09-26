package com.teambrella.android.ui.registration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.teambrella.android.R


fun startRegistration(context: Context, data: Uri) {
    context.startActivity(Intent(context, RegistrationActivity::class.java).setData(data))
}

class RegistrationActivity : AppCompatActivity() {

    companion object {
        const val WELCOME_FRAGMENT_TAG = "welcome_tag"
        const val REGISTRATION_FRAGMENT_TAG = "registration_fragment_tag"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.activity_registration)

        viewModel.regInfo.observe(this, Observer { _regInfo ->
            when (_regInfo?.uiState) {

                UIState.WELCOME -> {
                    supportFragmentManager.apply {
                        if (findFragmentByTag(WELCOME_FRAGMENT_TAG) == null) {
                            val fragment = findFragmentById(R.id.fragment_container)
                            beginTransaction().apply {
                                if (fragment != null) {
                                    remove(fragment)
                                }
                                add(R.id.fragment_container, WelcomeToJoinFragment(), WELCOME_FRAGMENT_TAG)
                            }.commit()
                        }
                    }
                }

                UIState.REGISTRATION -> {
                    supportFragmentManager.apply {
                        if (findFragmentByTag(REGISTRATION_FRAGMENT_TAG) == null) {
                            val fragment = findFragmentById(R.id.fragment_container)
                            beginTransaction().apply {
                                if (fragment != null) {
                                    remove(fragment)
                                }
                                add(R.id.fragment_container, CarRegistrationFragment(), REGISTRATION_FRAGMENT_TAG)
                            }.commit()
                        }
                    }
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    private val viewModel: RegistrationViewModel
        get() = ViewModelProviders.of(this).get(RegistrationViewModel::class.java)
}