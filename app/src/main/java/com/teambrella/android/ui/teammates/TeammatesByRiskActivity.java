package com.teambrella.android.ui.teammates;

import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.ADataHostActivity;

/**
 * All Teammates sorted by Risk
 */
public class TeammatesByRiskActivity extends ADataHostActivity {

    private static final String TEAMMATES_DATA_TAG = "teammate_data_tag";

    @Override
    protected String[] getDataTags() {
        return new String[]{};
    }

    @Override
    protected String[] getPagerTags() {
        return new String[]{TEAMMATES_DATA_TAG};
    }

    @Override
    protected TeambrellaDataFragment getDataFragment(String tag) {
        return null;
    }

    @Override
    protected TeambrellaDataPagerFragment getDataPagerFragment(String tag) {
        return null;
    }
}
