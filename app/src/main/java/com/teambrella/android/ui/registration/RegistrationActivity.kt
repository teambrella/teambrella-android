package com.teambrella.android.ui.registration

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.teambrella.android.R
import com.teambrella.android.ui.dialog.ProgressDialogFragment
import com.teambrella.android.util.ConnectivityUtils
import java.net.SocketTimeoutException
import java.net.UnknownHostException


fun startRegistration(context: Context, data: Uri) {
    context.startActivity(Intent(context, RegistrationActivity::class.java).setData(data))
}

class RegistrationActivity : AppCompatActivity() {

    companion object {
        const val WELCOME_FRAGMENT_TAG = "welcome_tag"
        const val REGISTRATION_FRAGMENT_TAG = "registration_fragment_tag"
        const val PLEASE_WAIT_DIALOG = "please_wait_tag"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        setContentView(R.layout.activity_registration)

        viewModel.regInfo.observe(this, Observer { _regInfo ->
            when (_regInfo?.uiState) {
                UIState.WELCOME_PRELOAD -> showWelcomeScreen(false, _regInfo.error)
                UIState.WELCOME -> showWelcomeScreen(false, _regInfo.error)
                UIState.REGISTRATION -> showRegistrationScreen(false)
                UIState.PLEASE_WAIT_WELCOME -> showWelcomeScreen(true, _regInfo.error)
            }
        })

        if (savedInstanceState == null) {
            getWelcomeScreen()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onActivityStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    private val viewModel: RegistrationViewModel
        get() = ViewModelProviders.of(this).get(RegistrationViewModel::class.java)


    private fun showRegistrationScreen(wait: Boolean) {
        supportFragmentManager.let { fragmentManager ->
            fragmentManager.beginTransaction().apply {
                if (fragmentManager.findFragmentByTag(REGISTRATION_FRAGMENT_TAG) == null) {
                    val fragment = fragmentManager.findFragmentById(R.id.fragment_container)
                    if (fragment != null) {
                        remove(fragment)
                    }
                    add(R.id.fragment_container, CarRegistrationFragment(), REGISTRATION_FRAGMENT_TAG)
                }

                val fragment = fragmentManager.findFragmentByTag(PLEASE_WAIT_DIALOG)

                if (wait && fragment == null) {
                    ProgressDialogFragment().show(fragmentManager, PLEASE_WAIT_DIALOG)
                }

                if (!wait && fragment != null) {
                    (fragment as DialogFragment).dismissAllowingStateLoss()
                }

            }.takeIf { !it.isEmpty }?.commit()
        }
    }

    private fun showWelcomeScreen(wait: Boolean, error: Throwable?) {
        supportFragmentManager.let { fragmentManager ->
            fragmentManager.beginTransaction().apply {
                if (fragmentManager.findFragmentByTag(WELCOME_FRAGMENT_TAG) == null) {
                    val fragment = fragmentManager.findFragmentById(R.id.fragment_container)
                    if (fragment != null) {
                        remove(fragment)
                    }
                    add(R.id.fragment_container, WelcomeToJoinFragment(), WELCOME_FRAGMENT_TAG)
                }

                val fragment = fragmentManager.findFragmentByTag(PLEASE_WAIT_DIALOG)

                if (wait && fragment == null) {
                    ProgressDialogFragment().show(fragmentManager, PLEASE_WAIT_DIALOG)
                }

                if (!wait && fragment != null) {
                    (fragment as DialogFragment).dismissAllowingStateLoss()
                }

            }.takeIf { !it.isEmpty }?.commit()
        }

        error?.let { _error ->
            val isInternetAvailable = ConnectivityUtils.isNetworkAvailable(this)
            if (_error is UnknownHostException
                    || _error is SocketTimeoutException || !isInternetAvailable) {
                Snackbar.make(findViewById(R.id.input_container), R.string.no_internet_connection, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.retry) { _ ->
                            getWelcomeScreen()
                        }
                        .setActionTextColor(resources.getColor(R.color.lightGold))
                        .show()
            } else {
                Snackbar.make(findViewById(R.id.input_container), R.string.unable_to_connect_try_later, Snackbar.LENGTH_LONG)
                        .addCallback(object : Snackbar.Callback() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                finish()
                            }
                        })
                        .show()
            }
        }
    }

    private fun getWelcomeScreen() {
        val joinUri = intent.data
        val teamId = Integer.parseInt(joinUri.lastPathSegment)
        viewModel.getWelcomeScreen(teamId, joinUri?.getQueryParameter("invite"))
    }
}
