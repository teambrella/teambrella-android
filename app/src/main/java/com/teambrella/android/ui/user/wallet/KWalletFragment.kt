package com.teambrella.android.ui.user.wallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.gson.JsonObject
import com.teambrella.android.R
import com.teambrella.android.api.*
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
import com.teambrella.android.util.StatisticHelper
import com.teambrella.android.util.log.Log
import io.reactivex.Notification
import io.reactivex.Observable
import java.text.DecimalFormat
import java.text.SimpleDateFormat
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
    private val fundWalletView: TextView? by ViewHolder(R.id.fund_wallet)
    private val backupWalletButton: View? by ViewHolder(R.id.backup_wallet)
    private val backupWalletMessage: View? by ViewHolder(R.id.wallet_not_backed_up_message)
    private val showPrivateKeyButton: View? by ViewHolder(R.id.show_private_key)
    private val spentThisYearView: TextView? by ViewHolder(R.id.spentThisYear)
    private val spentThisMonthView: TextView? by ViewHolder(R.id.spentThisMonth)
    private val spentThisYearLabel: TextView? by ViewHolder(R.id.spentThisYearLabel)
    private val spentThisMonthLabel: TextView? by ViewHolder(R.id.spentThisMonthLabel)
    private val etherScanView: View? by ViewHolder(R.id.imageEtherScan)
    private val fundWalletCommentView: TextView? by ViewHolder(R.id.fundWalletComment)
    private val decimalFormat = DecimalFormat.getInstance()
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
                , decimalFormat.format(0))

        dataHost.fundAddress?.let {
            fundWalletView?.isEnabled = true
            fundWalletView?.setOnClickListener { _ -> QRCodeActivity.startQRCode(context, it, QRCodeActivity.QRTYPE_ADDRESS) }
        }

//        AmountCurrencyUtil.setCryptoAmount(uninterruptedCoverageCryptoValue, 0f)
//        uninterruptedCoverageCurrencyValue?.text = context?.getString(R.string.amount_format_string
//                , AmountCurrencyUtil.getCurrencySign(dataHost.currency), decimalFormat.format(0))

        dataHost.addWalletBackupListener(this)

        savedInstanceState?.let {
            if (it.containsKey(EXTRA_BACKUP)) {
                backupWalletMessage?.visibility =
                        if (it.getBoolean(EXTRA_BACKUP, false)) View.GONE else View.VISIBLE
                backupWalletButton?.visibility =
                        if (it.getBoolean(EXTRA_BACKUP, false)) View.VISIBLE else View.GONE
            }
        }

        showPrivateKeyButton?.setOnClickListener {
            QRCodeActivity.startQRCode(context, TeambrellaUser.get(context).privateKey, QRCodeActivity.QRTYPE_KEY)
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

            cryptoBalanceView?.text = String.format(Locale.US, "%.0f", cryptoBalance*1000)
            balanceView?.text = context?.getString(R.string.amount_format_string
                    , AmountCurrencyUtil.getCurrencySign(dataHost.currency), decimalFormat.format(Math.round(cryptoBalance * currencyRate)))

            spentThisYearLabel?.text =  view!!.context.getString(R.string.in_year, Date().year + 1900).capitalize()
            spentThisMonthLabel?.text = view!!.context.getString(R.string.in_month, SimpleDateFormat("LLLL", Locale.getDefault()).format(Date())).capitalize()
            data?.amountFiatYear?.let {
                AmountCurrencyUtil.setSignedAmountWithBadge(spentThisYearView, Math.round(it), dataHost.currency)
            }
            data?.amountFiatMonth?.let {
                AmountCurrencyUtil.setSignedAmountWithBadge(spentThisMonthView, Math.round(it), dataHost.currency)
            }
            val hasFundWalletComment = !data?.fundWalletComment.isNullOrEmpty()
            fundWalletCommentView?.visibility = if (hasFundWalletComment) View.VISIBLE else View.GONE
            if (hasFundWalletComment) {
                fundWalletCommentView?.text = data?.fundWalletComment
            }

            Observable.just(data)
                    .flatMap { Observable.fromIterable(data?.get(TeambrellaModel.ATTR_DATA_COSIGNERS)?.asJsonArray) }
                    .map { it.asJsonObject.get(TeambrellaModel.ATTR_DATA_AVATAR).asString }
                    .toList()
                    .subscribe({
                        cosignersAvatarView?.setAvatars(imageLoader, it, 0)
                        cosignersCountView?.text = it.size.toString()
                    }, {

                    })?.takeIf { it.isDisposed }?.dispose()

            cosignersView?.visibility = View.VISIBLE
            cosignersView?.setOnClickListener {
                CosignersActivity.start(context
                        , data?.get(TeambrellaModel.ATTR_DATA_COSIGNERS)?.asJsonArray?.toString()
                        , dataHost.teamId)
            }

            transactionsView?.setOnClickListener { startActivity(getLaunchIntent(context!!, dataHost.teamId, dataHost.currency, currencyRate)) }
            withdrawView?.isEnabled = true
            withdrawView?.setOnClickListener { WithdrawActivity.start(context, dataHost.teamId, dataHost.currency, currencyRate) }

            val hasContract = !data?.contractAdress.isNullOrEmpty()
            etherScanView?.visibility = if (hasContract) View.VISIBLE else View.GONE
            if (hasContract) {
                etherScanView?.setOnClickListener {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://etherscan.com/address/" + data?.contractAdress))
                    startActivity(browserIntent)
                }
            }
        } else {
            cryptoBalanceView?.text = String.format(Locale.US, "%d", 0)
            balanceView?.text = context?.getString(R.string.amount_format_string
                    , AmountCurrencyUtil.getCurrencySign(dataHost.currency), decimalFormat.format(0))
            cosignersView?.visibility = View.GONE
            withdrawView?.isEnabled = false
            transactionsView?.setOnClickListener { startActivity(getLaunchIntent(context!!, dataHost.teamId, dataHost.currency, 0f)) }
            etherScanView?.visibility = View.GONE
        }

        if (!user.isDemoUser) {
            dataHost.backUpWallet(false)
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