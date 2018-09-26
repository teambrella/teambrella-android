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
import com.teambrella.android.R
import java.util.*


enum class UIState {
    WELCOME,
    REGISTRATION
}


data class RegistrationInfo(val initObject: RegistrationInfo? = null,
                            val teamIcon: String? = initObject?.teamIcon,
                            val teamName: String? = initObject?.teamName,
                            val teamCountry: String? = initObject?.teamCountry,
                            val model: String? = initObject?.model,
                            val city: String? = initObject?.city,
                            val userName: String? = initObject?.userName,
                            val email: String? = initObject?.email,
                            val uiState: UIState = UIState.WELCOME
)

class RegistrationViewModel : ViewModel() {
    private val _regInfo = MutableLiveData<RegistrationInfo>()
    private val facebookLoginController = FacebookLoginController()
    private val vkLoginController = VKLoginController()

    val regInfo: LiveData<RegistrationInfo>
        get() = _regInfo

    init {
        _regInfo.postValue(RegistrationInfo(null, teamIcon = "/content/uploads/0/car.png", teamName = "Антикаско", teamCountry = "Россия"))
    }

    fun onFacebookLogin(activity: Activity) {
        facebookLoginController.login(activity) { _ ->
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.REGISTRATION))
        }
    }

    fun onVkLogin(activity: Activity) {
        vkLoginController.login(activity) { _ ->
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.REGISTRATION))
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookLoginController.onActivityResult(requestCode, resultCode, data)
    }
}

private class FacebookLoginController {

    private val callbackManager = CallbackManager.Factory.create()

    fun login(activity: Activity, onSuccess: (String) -> Unit) {
        val loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                loginManager.unregisterCallback(callbackManager)
                onSuccess.invoke(result.accessToken.token)
                loginManager.logOut()
            }

            override fun onCancel() {
                loginManager.unregisterCallback(callbackManager)
            }

            override fun onError(error: FacebookException?) {
                loginManager.unregisterCallback(callbackManager)
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

private class VKLoginController {


    private var handler = Handler()

    fun login(activity: Activity, onSuccess: (String?) -> Unit) {
        WebAuthProvider.init(Auth0(activity).apply { isOIDCConformant = true })
                .withScheme("app")
                .withConnection("vkontakte")
                .withCustomTabsOptions(CustomTabsOptions.newBuilder().withToolbarColor(R.color.colorPrimary).showTitle(true).build())
                .start(activity, object : AuthCallback {
                    override fun onFailure(dialog: Dialog) {

                    }

                    override fun onFailure(exception: AuthenticationException) {

                    }

                    override fun onSuccess(credentials: com.auth0.android.result.Credentials) {
                        handler.post {
                            onSuccess.invoke(credentials.accessToken)
                        }
                    }
                })
    }
}


