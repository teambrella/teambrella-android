package com.teambrella.android.ui.votes

import android.net.Uri

import com.teambrella.android.data.base.IDataHost

/**
 * All Vote Activity Interface
 */
interface IAllVoteActivity : IDataHost {

    val teamId: Int
    val uri: Uri
    val statAsTeam : Float
    val statAsTeamOrBetter : Float
    val teamVote : Float
    val isItMe: Boolean
    val userName: String?
}
