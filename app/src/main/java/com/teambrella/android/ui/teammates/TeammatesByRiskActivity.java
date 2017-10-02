package com.teambrella.android.ui.teammates;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.teambrella.android.data.base.TeambrellaDataFragment;
import com.teambrella.android.data.base.TeambrellaDataPagerFragment;
import com.teambrella.android.ui.base.ADataHostActivity;

/**
 * All Teammates sorted by Risk
 */
public class TeammatesByRiskActivity extends ADataHostActivity {

    private static final String TEAMMATES_DATA_TAG = "teammate_data_tag";
    private static final String TEAMMATES_UI_TAG = "teammates_ui_tag";

    public static void start(Context context) {
        context.startActivity(new Intent(context, TeammatesByRiskActivity.class));
    }

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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
