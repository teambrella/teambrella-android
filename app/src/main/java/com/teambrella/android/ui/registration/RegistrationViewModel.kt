package com.teambrella.android.ui.registration

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.os.Handler
import com.auth0.android.Auth0
import com.auth0.android.authentication.AuthenticationException
import com.auth0.android.provider.AuthCallback
import com.auth0.android.provider.CustomTabsOptions
import com.auth0.android.provider.WebAuthProvider
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
import com.teambrella.android.ui.registration.join.JoinServer
import java.util.*


enum class UIState {
    WELCOME,
    REGISTRATION,
    PLEASE_WAIT_WELCOME
}


data class RegistrationInfo(val initObject: RegistrationInfo? = null,
                            val teamIcon: String? = initObject?.teamIcon,
                            val teamName: String? = initObject?.teamName,
                            val teamCountry: String? = initObject?.teamCountry,
                            val welcomeTitle: String? = initObject?.welcomeTitle,
                            val welcomeMessage: String? = initObject?.welcomeMessage,
                            val model: String? = initObject?.model,
                            val city: String? = initObject?.city,
                            val userName: String? = initObject?.userName,
                            val email: String? = initObject?.email,
                            val uiState: UIState = UIState.WELCOME
)

class RegistrationViewModel : ViewModel() {
    private val _regInfo = MutableLiveData<RegistrationInfo>()
    private val facebookLoginController = FacebookLoginController()
    private val vkLoginController = VKLoginController(
            { _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.REGISTRATION)) },
            { _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.WELCOME)) },
            { _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.WELCOME)) })


    private val joinServer = JoinServer()


    val regInfo: LiveData<RegistrationInfo>
        get() = _regInfo

    init {
        _regInfo.postValue(RegistrationInfo(null, teamIcon = "/content/uploads/0/car.png", teamName = "Антикаско", teamCountry = "Russia"))
    }

    fun onFacebookLogin(activity: Activity) {

        fun onSuccess(token: String?) {
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.REGISTRATION))
        }

        fun onError() {
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.WELCOME))
        }

        fun onCancel() {
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.WELCOME))
        }

        facebookLoginController.login(activity, ::onSuccess, ::onError, ::onCancel)
    }

    fun onVkLogin(activity: Activity) {
        vkLoginController.login(activity)
        _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.PLEASE_WAIT_WELCOME))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookLoginController.onActivityResult(requestCode, resultCode, data)
    }

    fun onActivityStart() {
        vkLoginController.onActivityStart()
    }

    fun getWelcomeScreen(teamId: Int, invite: String?) {

        fun onSuccess(data: JsonObject?) {
            _regInfo.postValue(RegistrationInfo(_regInfo.value,
                    teamName = data.data?.teamName,
                    teamIcon = data.data?.teamLogo,
                    welcomeTitle = data.data?.welcomeTitle,
                    welcomeMessage = data.data?.welcomeText))
        }

        fun onError() {

        }

        joinServer.getWelcomeScreen(teamId, invite, ::onSuccess, ::onError)
    }
}

private class FacebookLoginController {

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

private class VKLoginController(val onSuccess: (String?) -> Unit, val onError: () -> Unit, val onCancel: () -> Unit) {


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

                    override fun onSuccess(credentials: com.auth0.android.result.Credentials) {
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


