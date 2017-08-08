package com.teambrella.android.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.claim.ReportClaimActivity;

import io.reactivex.Notification;

/**
 * Coverage and Wallet fragment.
 */
public class HomeCoverageAndWalletFragment extends ADataFragment<IMainDataHost> {


    private ImageView mObjectPicture;
    private TextView mObjectModel;
    private TextView mCoverage;
    private TextView mSubmitClaim;
    private TextView mCoverageType;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_coverage_and_wallet, container, false);
        mObjectModel = view.findViewById(R.id.model);
        mObjectPicture = view.findViewById(R.id.object_picture);
        mCoverage = view.findViewById(R.id.coverage);
        mSubmitClaim = view.findViewById(R.id.submit_claim);
        mCoverageType = view.findViewById(R.id.coverage_type);
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            final String objectName = data.getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME);
            mObjectModel.setText(objectName);
            final String objectImageUri = TeambrellaModel.getImage(TeambrellaServer.BASE_URL, data.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO);
            picasso.load(objectImageUri).into(mObjectPicture);
            mCoverage.setText(Html.fromHtml(getString(R.string.coverage_format_string, Math.round((data.getFloat(TeambrellaModel.ATTR_DATA_COVERAGE) + 0.005) * 100))));
            mSubmitClaim.setOnClickListener(v -> ReportClaimActivity.start(getContext(), objectImageUri, objectName, mDataHost.getTeamId()));
            mCoverageType.setText(TeambrellaModel.getInsuranceTypeName(mDataHost.getTeamType()));

        }
    }
}