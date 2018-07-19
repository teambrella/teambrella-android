package com.teambrella.android.ui.claim

import android.content.Intent
import android.support.annotation.StringRes

import com.teambrella.android.data.base.IDataHost

/**
 * Claim activity interface
 */
interface IClaimActivity : IDataHost {

    val claimId: Int

    val teamId: Int

    fun setTitle(title: String)

    fun setSubtitle(subtitle: String)

    fun postVote(vote: Int)

    fun showSnackBar(@StringRes text: Int)

    fun launchActivity(intent: Intent)

}
