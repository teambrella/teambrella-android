package com.teambrella.android.ui.withdraw

import android.os.Bundle
import com.teambrella.android.data.base.TeambrellaDataPagerLoader
import com.teambrella.android.ui.base.TeambrellaPagerViewModel
import com.teambrella.android.ui.base.property
import com.teambrella.android.ui.base.uri

class WithdrawalViewModel : TeambrellaPagerViewModel() {

    override fun getDataPagerLoader(config: Bundle?)
            : TeambrellaDataPagerLoader = WithdrawalsDataPagerLoader(config?.uri, config?.property)

}