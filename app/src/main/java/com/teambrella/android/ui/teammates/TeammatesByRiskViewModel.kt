package com.teambrella.android.ui.teammates

import android.os.Bundle
import com.teambrella.android.data.base.TeambrellaDataPagerLoader
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.uri

class TeammatesByRiskViewModel : TeambrellaPagerViewModel() {
    companion object {
        const val EXTRA_RANGES = "extra_ranges"
    }

    override fun getDataPagerLoader(config: Bundle?): TeambrellaDataPagerLoader =
            TeammatesByRiskDataPagerLoader(config?.uri
                    ?: throw IllegalArgumentException(), config.getParcelableArrayList(EXTRA_RANGES))
}