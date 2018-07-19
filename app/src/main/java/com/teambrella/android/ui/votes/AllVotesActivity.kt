package com.teambrella.android.ui.votes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.teambrella.android.R
import com.teambrella.android.api.TeambrellaModel
import com.teambrella.android.api.server.TeambrellaUris
import com.teambrella.android.ui.base.*


/**
 * All Votes Activity
 */
class AllVotesActivity : ATeambrellaActivity(), IAllVoteActivity {

    override val uri: Uri by lazy(LazyThreadSafetyMode.NONE) {
        intent.uri ?: throw RuntimeException()
    }

    override val teamId: Int by lazy(LazyThreadSafetyMode.NONE) {
        intent.teamId
    }

    override val dataPagerTags: Array<String>
        get() = arrayOf(ALL_VOTES_DATA_TAG)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_one_fragment)

        supportFragmentManager.apply {
            if (findFragmentByTag(ALL_VOTES_UI_TAG) == null) {
                beginTransaction().add(R.id.container
                        , createDataFragment(arrayOf(ALL_VOTES_DATA_TAG)
                        , AllVotesFragment::class.java), ALL_VOTES_UI_TAG)
                        .commit()
            }
        }

        setTitle(R.string.all_votes)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector)
        }
    }


    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    override fun getDataPagerConfig(tag: String) = when (tag) {
        ALL_VOTES_DATA_TAG -> getPagerConfig(uri, TeambrellaModel.ATTR_DATA_VOTERS)
        else -> super.getDataPagerConfig(tag)
    }


    companion object {

        private const val ALL_VOTES_DATA_TAG = "data_tag"
        private const val ALL_VOTES_UI_TAG = "ui_tag"

        fun startClaimAllVotes(context: Context, teamId: Int, claimId: Int) {
            context.startActivity(Intent(context, AllVotesActivity::class.java).apply {
                this.teamId = teamId
                this.claimId = claimId
                this.uri = TeambrellaUris.getAllVotesForClaim(teamId, claimId)
            })
        }

        fun startTeammateAllVotes(context: Context, teamId: Int, teammateId: Int) {
            context.startActivity(Intent(context, AllVotesActivity::class.java).apply {
                this.teamId = teamId
                this.teammateId = teammateId
                this.uri = TeambrellaUris.getAllVotesForTeammate(teamId, teammateId)
            })
        }
    }
}
