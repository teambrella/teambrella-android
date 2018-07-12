package com.teambrella.android.ui.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.view.MenuItem
import com.teambrella.android.R
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.ATeambrellaActivity
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.getPagerConfig


private const val TEAM_ID_EXTRA = "team_id"
private const val CURRENCY_EXTRA = "currency"
private const val CRYPTO_RATE = "crypto_rate"

fun getLaunchIntent(context: Context, teamId: Int, currency: String, cryptoRate: Float): Intent {
    return Intent(context, WalletTransactionsActivity::class.java)
            .putExtra(TEAM_ID_EXTRA, teamId)
            .putExtra(CURRENCY_EXTRA, currency)
            .putExtra(CRYPTO_RATE, cryptoRate)
}


@Suppress("CAST_NEVER_SUCCEEDS")
/**
 * Wallet Transactions Activity
 */
class WalletTransactionsActivity : ATeambrellaActivity(), IWalletTransactionActivity {

    private object Tags {
        const val DATA_TAG: String = "data_tag"
        const val UI_TAG: String = "ui_tag"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_fragment)
        val actionBar = supportActionBar
        actionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setTitle(R.string.transactions)
            it.setHomeAsUpIndicator(VectorDrawableCompat.create(resources, R.drawable.ic_arrow_back, null))
        }

        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(Tags.UI_TAG) == null) {
            fragmentManager
                    .beginTransaction()
                    .add(R.id.container
                            , ADataPagerProgressFragment.getInstance(Tags.DATA_TAG, WalletTransactionsFragment::class.java)
                            , Tags.UI_TAG)
                    .commit()
        }
    }

    override val dataPagerTags: Array<String> = arrayOf(Tags.DATA_TAG)


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            return when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : TeambrellaPagerViewModel> getPagerViewModelClass(tag: String): Class<T>? = when (tag) {
        Tags.DATA_TAG -> WalletTransactionsModel::class.java as Class<T>
        else -> super.getPagerViewModelClass(tag)
    }

    override fun getDataPagerConfig(tag: String): Bundle? = when (tag) {
        Tags.DATA_TAG -> getPagerConfig(TeambrellaUris.getWalletTransactions(intent.getIntExtra(TEAM_ID_EXTRA, 0)))
        else -> super.getDataPagerConfig(tag)
    }

    override fun getTeamId() = intent?.getIntExtra(TEAM_ID_EXTRA, -1) ?: -1

    override fun getCurrency() = intent?.getStringExtra(CURRENCY_EXTRA) ?: ""

    override fun getCryptoRate() = intent?.getFloatExtra(CRYPTO_RATE, 0f) ?: 0f
}
