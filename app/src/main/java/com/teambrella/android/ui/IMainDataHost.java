package com.teambrella.android.ui;

import com.google.gson.JsonArray;
import com.teambrella.android.data.base.IDataPager;

/**
 * Main Data Host
 */
public interface IMainDataHost {

    IDataPager<JsonArray> getTeamListPager();

}
