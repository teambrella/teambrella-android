package com.teambrella.android.ui.registration.login

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Handler
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.CustomTabsOptions
import com.auth0.android.provider.WebAuthProvider
import com.auth0.android.result.Credentials
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.teambrella.android.R
import java.util.*

class FacebookLoginController {

    private val callbackManager = CallbackManager.Factory.create()

    fun login(activity: Activity, onSuccess: (String) -> Unit, onError: () -> Unit, onCancel: () -> Unit) {
        val loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                loginManager.unregisterCallback(callbackManager)
                onSuccess.invoke(result.accessToken.token)
                loginManager.logOut()
            }

            override fun onCancel() {
                loginManager.unregisterCallback(callbackManager)
                onCancel.invoke()
            }

            override fun onError(error: FacebookException?) {
                loginManager.unregisterCallback(callbackManager)
                onError.invoke()
            }
        })

        val permissions = LinkedList<String>()
        permissions.add("public_profile")
        permissions.add("email")
        loginManager.logInWithReadPermissions(activity, permissions)

    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }
}

class VKLoginController(val onSuccess: (String?) -> Unit, val onError: () -> Unit, val onCancel: () -> Unit) {


    private var handler = Handler()

    fun login(activity: Activity) {
        WebAuthProvider.init(Auth0(activity).apply { isOIDCConformant = true })
                .withScheme("app")
                .withConnection("vkontakte")
                .withCustomTabsOptions(CustomTabsOptions.newBuilder().withToolbarColor(R.color.colorPrimary).showTitle(true).build())
                .start(activity, object : AuthCallback {
                    override fun onFailure(dialog: Dialog) {
                        handler.post {
                            if (waitForLoginResponse) {
                                onError.invoke()
                                waitForLoginResponse = false
                                handler.removeCallbacks(vkLoginTimeOut)
                            }
                        }
                    }

                    override fun onFailure(exception: AuthenticationException) {
                        handler.post {
                            if (waitForLoginResponse) {
                                onError.invoke()
                                waitForLoginResponse = false
                                handler.removeCallbacks(vkLoginTimeOut)
                            }
                        }
                    }

                    override fun onSuccess(credentials: Credentials) {
                        handler.post {
                            if (waitForLoginResponse) {
                                onSuccess.invoke(credentials.accessToken)
                                waitForLoginResponse = false
                                handler.removeCallbacks(vkLoginTimeOut)
                            }
                        }
                    }
                })

        waitForLoginResponse = true
    }

    fun onActivityStart() {
        if (waitForLoginResponse) {
            handler.removeCallbacks(vkLoginTimeOut)
            handler.postDelayed(vkLoginTimeOut, 3000)
        }
    }

    private val vkLoginTimeOut = Runnable {
        onCancel.invoke()
        waitForLoginResponse = false
    }

    private var waitForLoginResponse: Boolean = false
}
