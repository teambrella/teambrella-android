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
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.claim.ReportClaimActivity;
import com.teambrella.android.util.AmountCurrencyUtil;

import io.reactivex.Notification;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Coverage and Wallet fragment.
 */
public class HomeCoverageAndWalletFragment extends ADataFragment<IMainDataHost> {


    private ImageView mObjectPicture;
    private TextView mObjectModel;
    private TextView mCoverage;
    private TextView mSubmitClaim;
    private TextView mCoverageType;
    private TextView mWalletAmount;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_coverage_and_wallet, container, false);
        mObjectModel = view.findViewById(R.id.model);
        mObjectPicture = view.findViewById(R.id.object_picture);
        mCoverage = view.findViewById(R.id.coverage);
        mSubmitClaim = view.findViewById(R.id.submit_claim);
        mCoverageType = view.findViewById(R.id.coverage_type);
        mWalletAmount = view.findViewById(R.id.wallet_amount);

        view.findViewById(R.id.coverageInfo).setOnClickListener(v -> mDataHost.showCoverage());
        view.findViewById(R.id.walletInfo).setOnClickListener(v -> mDataHost.showWallet());

        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Picasso picasso = getPicasso();
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            final String objectName = data.getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME);
            mObjectModel.setText(objectName);
            final String objectImageUri = TeambrellaModel.getImage(TeambrellaServer.BASE_URL, data.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO);
            picasso.load(objectImageUri).centerCrop().resizeDimen(R.dimen.image_size_40, R.dimen.image_size_40).transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask)).
                    into(mObjectPicture);
            mCoverage.setText(Html.fromHtml(getString(R.string.coverage_format_string, Math.round((data.getFloat(TeambrellaModel.ATTR_DATA_COVERAGE)) * 100))));
            mSubmitClaim.setVisibility(mDataHost.getTeamAccessLevel() == TeambrellaModel.TeamAccessLevel.FULL_ACCESS
                    ? View.VISIBLE : View.INVISIBLE);
            mSubmitClaim.setOnClickListener(v -> ReportClaimActivity.start(getContext(), objectImageUri, objectName, mDataHost.getTeamId(), mDataHost.getCurrency()));
            mCoverageType.setText(TeambrellaModel.getInsuranceTypeName(mDataHost.getTeamType()));
            AmountCurrencyUtil.setCryptoAmount(mWalletAmount, data.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_BALANCE));

        }
    }
}
