package com.teambrella.android.ui.registration

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import com.google.gson.JsonObject
import com.teambrella.android.api.*
import com.teambrella.android.api.server.TeambrellaServer
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.backup.WalletBackupManager
import com.teambrella.android.blockchain.CryptoException
import com.teambrella.android.blockchain.EtherAccount
import com.teambrella.android.data.base.subscribeAutoDispose
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.ui.registration.join.JoinServer
import com.teambrella.android.util.log.Log
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.bitcoinj.core.DumpedPrivateKey


enum class UIState {
    WELCOME_PRELOAD,
    WELCOME,
    REGISTRATION,
    PLEASE_WAIT_WELCOME,
    PLEASE_WAIT_REGISTRATION,
    COMPLETE
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
                            val teamId: Int? = initObject?.teamId,
                            val inviteCode: String? = initObject?.inviteCode,
                            val error: Throwable? = initObject?.error,
                            val uiState: UIState = initObject?.uiState ?: UIState.WELCOME
)

class RegistrationViewModel : ViewModel() {


    companion object {
        const val LOG_TAG = "RegistrationViewModel"
    }


    private val _regInfo = MutableLiveData<RegistrationInfo>()
    private val joinServer = JoinServer()
    private lateinit var teambrellaUser: TeambrellaUser
    private var walletBackupManager: WalletBackupManager? = null


    val regInfo: LiveData<RegistrationInfo>
        get() = _regInfo


    init {
        _regInfo.postValue(RegistrationInfo(null, uiState = UIState.WELCOME_PRELOAD))
    }


    fun init(context: AppCompatActivity) {
        teambrellaUser = TeambrellaUser.get(context)
        if (walletBackupManager == null) {
            walletBackupManager = WalletBackupManager(context)
            walletBackupManager?.addBackupListener(walletBackupListener)
        } else {
            walletBackupManager?.setActivity(context)
        }
    }


    /**
     * Continue registration
     */
    fun continueRegistration() {
        walletBackupManager?.readWallet(true)
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        walletBackupManager?.onActivityResult(requestCode, resultCode, data)
    }


    /**
     * Register user on the server
     */
    fun registerUser(context: Context, name: String, email: String, location: String, model: String) {


        fun onSuccess(data: JsonObject?) {
            teambrellaUser.privateKey = teambrellaUser.pendingPrivateKey
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.COMPLETE))
        }

        fun onError(error: Throwable) {
            _regInfo.postValue(RegistrationInfo(_regInfo.value, userName = name, email = email, city = location, model = model,
                    uiState = UIState.REGISTRATION, error = error))
        }


        val privateKey = teambrellaUser.pendingPrivateKey
        val key = DumpedPrivateKey.fromBase58(null, privateKey).key

        val server = TeambrellaServer(context.applicationContext,
                teambrellaUser.pendingPrivateKey,
                teambrellaUser.deviceCode,
                teambrellaUser.getInfoMask(context.applicationContext))


        var publicKeySignature: String? = null

        try {
            publicKeySignature = EtherAccount.toPublicKeySignature(teambrellaUser.pendingPrivateKey, context.applicationContext, key.publicKeyAsHex)
        } catch (e: CryptoException) {
            Log.e(LOG_TAG, "Was unable to generate eth address from the private key. " +
                    "Only public key will be registered on the server. " +
                    "The error was: " + e.message, e)
        }

        val info = _regInfo.value

        server.requestObservable(TeambrellaUris.getRegisterUserUri(publicKeySignature), JsonObject().apply {
            this.teamId = info?.teamId
            this.invite = info?.inviteCode
            this.name = name
            this.location = location
            this.email = email
            this.carModelString = model

        }).subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribeAutoDispose(::onSuccess, ::onError) {}

        _regInfo.postValue(RegistrationInfo(_regInfo.value, error = null, uiState = UIState.PLEASE_WAIT_REGISTRATION))

    }


    /**
     * Request welcome screen
     */
    fun getWelcomeScreen(teamId: Int, invite: String?) {

        fun onSuccess(response: JsonObject?) {
            val data = response.data
            _regInfo.postValue(RegistrationInfo(_regInfo.value,
                    teamName = data?.teamName,
                    teamIcon = data?.teamLogo,
                    teamCountry = data?.teamArea,
                    userName = data?.name,
                    city = data?.location,
                    model = data?.carModelString,
                    email = data?.email,
                    welcomeTitle = data?.welcomeTitle,
                    welcomeMessage = data?.welcomeText, uiState = UIState.WELCOME))
        }

        fun onError(error: Throwable) {
            _regInfo.postValue(RegistrationInfo(_regInfo.value,
                    error = error))
        }

        _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.WELCOME_PRELOAD, error = null, teamId = teamId, inviteCode = invite))


        joinServer.getWelcomeScreen(teamId, invite, ::onSuccess, ::onError)
    }


    private val walletBackupListener = object : WalletBackupManager.IWalletBackupListener {

        override fun onWalletSaved(force: Boolean) {

        }

        override fun onWalletSaveError(code: Int, force: Boolean) {

        }

        override fun onWalletRead(key: String, force: Boolean) {
            teambrellaUser.privateKey = key
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.COMPLETE))

        }

        override fun onWalletReadError(code: Int, force: Boolean) {
            _regInfo.postValue(RegistrationInfo(_regInfo.value, uiState = UIState.REGISTRATION))
        }
    }
}

