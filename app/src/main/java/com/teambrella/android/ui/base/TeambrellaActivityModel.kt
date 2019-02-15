package com.teambrella.android.ui.base

import android.content.Intent
import android.net.Uri


private const val TEAM_ID = "team_id"
private const val USER_ID = "user_id"
private const val USER_NAME = "user_name"
private const val AVATAR = "avatar"
private const val URI = "uri"
private const val CURRENCY = "currency"
private const val CLAIM_ID = "claimId"
private const val TEAMMATE_ID = "teammateId"
private const val MODEL = "model"
private const val STAT_AS_TEAM_OR_BETTER = "statAsTeamOrBetter"
private const val STAT_AS_TEAM = "statAsTeam"
private const val IS_IT_ME = "isItMe"
private const val TEAM_VOTE = "team_vote"

var Intent?.teamId: Int
    get() = this?.getIntExtra(TEAM_ID, 0) ?: 0
    set(value) {
        this?.putExtra(TEAM_ID, value)
    }

var Intent?.currency: String?
    get() = this?.getStringExtra(CURRENCY)
    set(value) {
        this?.putExtra(CURRENCY, value)
    }

var Intent?.userId: String?
    get() = this?.getStringExtra(USER_ID)
    set(value) {
        this?.putExtra(USER_ID, value)
    }

var Intent?.userName: String?
    get() = this?.getStringExtra(USER_NAME)
    set(value) {
        this?.putExtra(USER_NAME, value)
    }

var Intent?.avatar: String?
    get() = this?.getStringExtra(AVATAR)
    set(value) {
        this?.putExtra(AVATAR, value)
    }

var Intent?.uri: Uri?
    get() = this?.getParcelableExtra(URI)
    set(value) {
        this?.putExtra(URI, value)
    }

var Intent?.claimId: Int
    get() = this?.getIntExtra(CLAIM_ID, 0) ?: 0
    set(value) {
        this?.putExtra(CLAIM_ID, value)
    }

var Intent?.teammateId: Int
    get() = this?.getIntExtra(TEAMMATE_ID, 0) ?: 0
    set(value) {
        this?.putExtra(TEAMMATE_ID, value)
    }

var Intent?.model: String?
    get() = this?.getStringExtra(MODEL)
    set(value) {
        this?.putExtra(MODEL, value)
    }

var Intent?.statAsTeamOrBetter: Float
    get() = this?.getFloatExtra(STAT_AS_TEAM_OR_BETTER,0f) ?: 0f
    set(value) {
        this?.putExtra(STAT_AS_TEAM_OR_BETTER, value)
    }

var Intent?.statAsTeam: Float
    get() = this?.getFloatExtra(STAT_AS_TEAM,0f) ?: 0f
    set(value) {
        this?.putExtra(STAT_AS_TEAM, value)
    }

var Intent?.isItMe: Boolean
    get() = this?.getBooleanExtra(IS_IT_ME,false) ?: false
    set(value) {
        this?.putExtra(IS_IT_ME, value)
    }

var Intent?.teamVote: Float
    get() = this?.getFloatExtra(TEAM_VOTE,0f) ?: 0f
    set(value) {
        this?.putExtra(TEAM_VOTE, value)
    }