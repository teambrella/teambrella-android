package com.teambrella.android.ui.team.teammates

import android.os.Bundle
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.uri

class TeammatesViewModel : TeambrellaPagerViewModel() {
    override fun getDataPagerLoader(config: Bundle?) = TeammatesDataPagerLoader(config?.uri)
}