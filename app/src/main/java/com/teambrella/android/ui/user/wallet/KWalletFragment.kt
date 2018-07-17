package com.teambrella.android.ui.user.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.TeambrellaModel
import com.teambrella.android.api.cryptoBalance
import com.teambrella.android.api.currencyRate
import com.teambrella.android.api.data
import com.teambrella.android.backup.WalletBackupManager
import com.teambrella.android.ui.CosignersActivity
import com.teambrella.android.ui.IMainDataHost
import com.teambrella.android.ui.QRCodeActivity
import com.teambrella.android.ui.TeambrellaUser
import com.teambrella.android.ui.base.ADataProgressFragment
import com.teambrella.android.ui.wallet.getLaunchIntent
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets
import com.teambrella.android.ui.withdraw.WithdrawActivity
import com.teambrella.android.util.AmountCurrencyUtil
import com.teambrella.android.util.ConnectivityUtils
import com.teambrella.android.util.QRCodeUtils
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.log.Log
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*


class KWalletFragment : ADataProgressFragment<IMainDataHost>(), WalletBackupManager.IWalletBackupListener {
    companion object {
        val LOG_TAG: String = KWalletFragment::class.java.simpleName
        const val EXTRA_BACKUP = "extra_backup"
    }

    private val user: TeambrellaUser by lazy(LazyThreadSafetyMode.NONE, { TeambrellaUser.get(context) })
    private val cryptoBalanceView: TextView? by ViewHolder(R.id.crypto_balance)
    private val balanceView: TextView? by ViewHolder(R.id.balance)
    private val currencyView: TextView? by ViewHolder(R.id.currency)
    private val withdrawView: View? by ViewHolder(R.id.withdraw)
    private val transactionsView: View? by ViewHolder(R.id.transactions)
    private val cosignersView: View? by ViewHolder(R.id.cosigners)
    private val cosignersAvatarView: TeambrellaAvatarsWidgets? by ViewHolder(R.id.cosigners_avatar)
    private val cosignersCountView: TextView? by ViewHolder(R.id.cosigners_count)
    private val qrCodeView: ImageView? by ViewHolder(R.id.qr_code)
    private val fundWalletView: TextView? by ViewHolder(R.id.fund_wallet)
    private val uninterruptedCoverageCryptoValue: TextView? by ViewHolder(R.id.for_uninterrupted_coverage_crypto_value)
    private val uninterruptedCoverageCurrencyValue: TextView? by ViewHolder(R.id.for_uninterrupted_coverage_currency_value)
    private val backupWalletButton: View? by ViewHolder(R.id.backup_wallet)
    private val backupWalletMessage: View? by ViewHolder(R.id.wallet_not_backed_up_message)
    private var showBackupInfoOnShow: Boolean = false
    private var isWalletBackedUp: Boolean? = null


    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        savedInstanceState?.let {
            dataHost.load(tags[0])
            setContentShown(false)
        }

        return inflater.inflate(R.layout.fragment_wallet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currencyView?.text = getString(R.string.milli_ethereum)

        balanceView?.text = context?.getString(R.string.amount_format_string
                , AmountCurrencyUtil.getCurrencySign(dataHost.currency)
                , 0)

        dataHost.fundAddress?.let {
            Observable.just(it).map { QRCodeUtils.createBitmap(it) }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ qrCodeView?.setImageBitmap(it) }, {})

            fundWalletView?.isEnabled = true
            fundWalletView?.setOnClickListener { _ -> QRCodeActivity.startQRCode(context, it) }
            qrCodeView?.visibility = View.VISIBLE
            qrCodeView?.setOnClickListener { _ -> QRCodeActivity.startQRCode(context, it) }

        }

        AmountCurrencyUtil.setCryptoAmount(uninterruptedCoverageCryptoValue, 0f)
        uninterruptedCoverageCurrencyValue?.text = context?.getString(R.string.amount_format_string
                , AmountCurrencyUtil.getCurrencySign(dataHost.currency), 0)

        dataHost.addWalletBackupListener(this)

        savedInstanceState?.let {
            if (it.containsKey(EXTRA_BACKUP)) {
                backupWalletMessage?.visibility =
                        if (it.getBoolean(EXTRA_BACKUP, false)) View.GONE else View.VISIBLE
                backupWalletButton?.visibility =
                        if (it.getBoolean(EXTRA_BACKUP, false)) View.VISIBLE else View.GONE
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onReload() {
        super.onReload()
        if (!user.isDemoUser) {
            dataHost.backUpWallet(false)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (!isVisibleToUser && showBackupInfoOnShow) {
            showWalletBackupInfo()
        }
    }

    /**
     *  Refresh views as soon as new data available
     */
    override fun onDataUpdated(notification: Notification<JsonObject>) {
        if (notification.isOnNext) {
            val data = notification.value?.data
            val cryptoBalance = data?.cryptoBalance ?: 0f
            val currencyRate = data?.currencyRate ?: 0f
            val stringId = if (cryptoBalance > 1) R.string.ethereum else R.string.milli_ethereum

            cryptoBalanceView?.text = when (stringId) {
                R.string.ethereum -> String.format(Locale.US, "%.2f", cryptoBalance)
                R.string.milli_ethereum -> String.format(Locale.US, "%d", Math.round(cryptoBalance * 1000))
                else -> null
            }

            balanceView?.text = context?.getString(R.string.amount_format_string
                    , AmountCurrencyUtil.getCurrencySign(dataHost.currency), Math.round(cryptoBalance * currencyRate))


            if (!user.isDemoUser) {
                dataHost.backUpWallet(false)
            }

            val forUninterruptedCoverage = Math.abs(data?.get(TeambrellaModel.ATTR_DATA_RECOMMENDED_CRYPTO)?.asFloat
                    ?: 0f)

            AmountCurrencyUtil.setCryptoAmount(uninterruptedCoverageCryptoValue, forUninterruptedCoverage)
            uninterruptedCoverageCurrencyValue?.text = context?.getString(R.string.amount_format_string, AmountCurrencyUtil.getCurrencySign(dataHost.currency), Math.round(forUninterruptedCoverage
                    * (data?.currencyRate ?: 0f)))

            Observable.just(data)
                    .flatMap { Observable.fromIterable(data?.get(TeambrellaModel.ATTR_DATA_COSIGNERS)?.asJsonArray) }
                    .map { it.asJsonObject.get(TeambrellaModel.ATTR_DATA_AVATAR).asString }
                    .toList()
                    .subscribe({
                        cosignersAvatarView?.setAvatars(imageLoader, it, 0)
                        cosignersCountView?.text = it.size.toString()
                    }, {

                    })

            cosignersView?.setOnClickListener {
                CosignersActivity.start(context
                        , data?.get(TeambrellaModel.ATTR_DATA_COSIGNERS)?.asJsonArray?.toString()
                        , dataHost.teamId)
            }

            transactionsView?.setOnClickListener { startActivity(getLaunchIntent(context!!, dataHost.teamId, dataHost.currency, currencyRate)) }
            withdrawView?.setOnClickListener { WithdrawActivity.start(context, dataHost.teamId, dataHost.currency, currencyRate) }
        }
        setContentShown(true)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        dataHost.removeWalletBackupListener(this)
    }

    override fun onWalletSaved(force: Boolean) {
        backupWalletButton?.visibility = View.VISIBLE
        backupWalletMessage?.visibility = View.GONE
        if (force) {
            Toast.makeText(context, R.string.your_wallet_is_backed_up, Toast.LENGTH_SHORT).show()
            StatisticHelper.onWalletSaved(context, user.userId)
        }
        isWalletBackedUp = true
        user.isWalletBackedUp = true
    }

    override fun onWalletSaveError(code: Int, force: Boolean) {
        if (code == WalletBackupManager.IWalletBackupListener.RESOLUTION_REQUIRED) {
            backupWalletMessage?.visibility = View.VISIBLE
            backupWalletMessage?.setOnClickListener { dataHost.showWalletBackupDialog() }
            isWalletBackedUp = false
            showWalletBackupInfo()
        } else {
            if (code != WalletBackupManager.IWalletBackupListener.CANCELED) {
                if (force) {
                    dataHost.showSnackBar(if (ConnectivityUtils.isNetworkAvailable(context))
                        R.string.something_went_wrong_error
                    else
                        R.string.no_internet_connection)
                }
                Log.reportNonFatal(LOG_TAG, RuntimeException("Unable to save key " + if (force) "force" else "no force"))
            }

            if (!force) {
                backupWalletMessage?.visibility = View.GONE
            }
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isWalletBackedUp?.let {
            outState.putBoolean(EXTRA_BACKUP, it)
        }
    }

    override fun onWalletRead(key: String?, force: Boolean) {

    }

    override fun onWalletReadError(code: Int, force: Boolean) {

    }

    private fun showWalletBackupInfo() {
        if (!user.isDemoUser) {
            showBackupInfoOnShow = if (userVisibleHint) {
                if (!user.isBackupInfoDialogShown) {
                    dataHost.showWalletBackupDialog()
                    user.setBackupInfodialogShown(true)
                }
                false
            } else {
                true
            }
        }
    }
}