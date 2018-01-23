package com.teambrella.android.ui.user.wallet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.view.MenuItem
import com.teambrella.android.R
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.data.base.TeambrellaDataFragment
import com.teambrella.android.data.base.TeambrellaDataPagerFragment
import com.teambrella.android.ui.base.ADataPagerProgressFragment
import com.teambrella.android.ui.base.TeambrellaDataHostActivity


private const val TEAM_ID_EXTRA = "team_id"

fun getLaunchIntent(context: Context, teamId: Int): Intent {
    return Intent(context, WalletTransactionsActivity::class.java)
            .putExtra(TEAM_ID_EXTRA, teamId)
}


/**
 * Wallet Transactions Activity
 */
class WalletTransactionsActivity : TeambrellaDataHostActivity() {

    private object Tags {
        const val DATA_TAG: String = "data_tag"
        const val UI_TAG: String = "ui_tag"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_fragment)
        val actionBar = supportActionBar
        actionBar?.let {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.transactions)
            actionBar.setHomeAsUpIndicator(VectorDrawableCompat.create(resources, R.drawable.ic_arrow_back, null))
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

    override fun getDataTags(): Array<String> {
        return arrayOf()
    }

    override fun getPagerTags(): Array<String> {
        return arrayOf(Tags.DATA_TAG)
    }

    override fun getDataFragment(tag: String?): TeambrellaDataFragment? {
        return null
    }

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

    override fun getDataPagerFragment(tag: String?): TeambrellaDataPagerFragment? {
        return when (tag) {
            Tags.DATA_TAG -> TeambrellaDataPagerFragment.getInstance(TeambrellaUris.getWalletTransactions(intent.getIntExtra(TEAM_ID_EXTRA, 0))
                    , null
                    , TeambrellaDataPagerFragment::class.java)
            else -> null
        }
    }
}
