package com.teambrella.android.ui.claim

import android.os.Bundle
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.uri

class ClaimsViewModel : TeambrellaPagerViewModel() {
    override fun getDataPagerLoader(config: Bundle?) = ClaimsDataPagerLoader(config?.uri)
}