package com.teambrella.android.ui.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataProgressFragment;

import io.reactivex.Notification;

/**
 * Claim fragment
 */
public class ClaimFragment extends ADataProgressFragment<IDataHost> {

    private ImageView mClaimPicture;

    public static ClaimFragment getInstance(String tag) {
        return ADataProgressFragment.getInstance(tag, ClaimFragment.class);
    }

    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_claim, container, false);
        mClaimPicture = (ImageView) view.findViewById(R.id.claim_picture);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {

    }
}
