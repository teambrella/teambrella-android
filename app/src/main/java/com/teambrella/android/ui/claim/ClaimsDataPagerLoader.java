package com.teambrella.android.ui.claim;

import android.content.Context;
import android.net.Uri;

import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

/**
 * Claims Data Pager Loader
 */
public class ClaimsDataPagerLoader extends TeambrellaDataPagerLoader {
    
    ClaimsDataPagerLoader(Context context, Uri uri) {
        super(context, uri, null);
    }
}
