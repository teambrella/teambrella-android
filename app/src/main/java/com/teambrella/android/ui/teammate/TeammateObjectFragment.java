package com.teambrella.android.ui.teammate;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.image.ImageViewerActivity;
import com.teambrella.android.ui.team.claims.ClaimsActivity;
import com.teambrella.android.util.AmountCurrencyUtil;

import java.util.ArrayList;

import io.reactivex.Notification;
import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Teammate Object Fragment
 */
public class TeammateObjectFragment extends ADataFragment<ITeammateActivity> {

    private ImageView mObjectPicture;
    private TextView mObjectModel;
    private TextView mLimit;
    private TextView mNet;
    private TextView mRisk;
    private TextView mSeeClaims;
    private TextView mCoverageType;
    private TextView mObjectTitle;

    private int mTeammateId;
    private int mTeamId;
    private int mClaimId;
    private int mClaimCount;
    private String mModel;
    private String mCurrency;
    private int mGender;


    public static TeammateObjectFragment getInstance(String dataTag) {
        TeammateObjectFragment fragment = new TeammateObjectFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_DATA_FRAGMENT_TAG, dataTag);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teammate_object, container, false);
        mObjectModel = view.findViewById(R.id.model);
        mObjectPicture = view.findViewById(R.id.object_picture);
        mLimit = view.findViewById(R.id.limit);
        mNet = view.findViewById(R.id.net);
        mRisk = view.findViewById(R.id.risk);
        mSeeClaims = view.findViewById(R.id.see_claims);
        mCoverageType = view.findViewById(R.id.coverage_type);
        mObjectTitle = view.findViewById(R.id.object_title);
        return view;
    }


    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {

            Observable<JsonWrapper> responseObservable = Observable.just(notification.getValue())
                    .map(JsonWrapper::new);

            Observable<JsonWrapper> dataObservable =
                    responseObservable.map(response -> response.getObject(TeambrellaModel.ATTR_DATA))
                            .doOnNext(data -> mTeammateId = data.getInt(TeambrellaModel.ATTR_DATA_ID, mTeammateId));

            Observable<JsonWrapper> basicObservable =
                    dataObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC));

            basicObservable.doOnNext(jsonWrapper -> mGender = jsonWrapper.getInt(TeambrellaModel.ATTR_DATA_GENDER, mGender))
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();

            Observable<JsonWrapper> objectObservable =
                    dataObservable.map(item -> item.getObject(TeambrellaModel.ATTR_DATA_ONE_OBJECT));

            dataObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_TEAM))
                    .doOnNext(jsonWrapper -> mCoverageType.setText(TeambrellaModel.getInsuranceTypeName(jsonWrapper.getInt(TeambrellaModel.ATTR_DATA_COVERAGE_TYPE))))
                    .doOnNext(jsonWrapper -> mCurrency = jsonWrapper.getString(TeambrellaModel.ATTR_DATA_CURRENCY, mCurrency))
                    .doOnNext(jsonWrapper -> {
                        if (mDataHost.getTeamId() == 2021) {
                            mObjectTitle.setText(mDataHost.isItMe() ? TeambrellaModel.getMyObjectNamer(TeambrellaModel.InsuranceType.CAT)
                                    : TeambrellaModel.getObjectNameWithOwner(TeambrellaModel.InsuranceType.CAT, mGender));
                        } else if (mDataHost.getTeamId() == 2020) {
                            mObjectTitle.setText(mDataHost.isItMe() ? TeambrellaModel.getMyObjectNamer(TeambrellaModel.InsuranceType.DOG)
                                    : TeambrellaModel.getObjectNameWithOwner(TeambrellaModel.InsuranceType.DOG, mGender));
                        } else {
                            mObjectTitle.setText(mDataHost.isItMe() ? TeambrellaModel.getMyObjectNamer(jsonWrapper.getInt(TeambrellaModel.ATTR_DATA_COVERAGE_TYPE))
                                    : TeambrellaModel.getObjectNameWithOwner(jsonWrapper.getInt(TeambrellaModel.ATTR_DATA_COVERAGE_TYPE), mGender));
                        }
                    })
                    .onErrorReturnItem(new JsonWrapper(null))
                    .blockingFirst();


            objectObservable.doOnNext(objectData -> mObjectModel.setText(objectData.getString(TeambrellaModel.ATTR_DATA_MODEL)))
                    .doOnNext(objectData -> AmountCurrencyUtil.setAmount(mLimit, Math.round(objectData.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_LIMIT)), mCurrency))
                    .doOnNext(objectData -> mClaimId = objectData.getInt(TeambrellaModel.ATTR_DATA_ONE_CLAIM_ID, mClaimId))
                    .doOnNext(objectData -> mModel = objectData.getString(TeambrellaModel.ATTR_DATA_MODEL, mModel))
                    .doOnNext(objectData -> mClaimCount = objectData.getInt(TeambrellaModel.ATTR_DATA_CLAIM_COUNT, mClaimCount))
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();

            ArrayList<String> photos = objectObservable.flatMap(objectData -> Observable.fromIterable(objectData.getJsonArray(TeambrellaModel.ATTR_DATA_SMALL_PHOTOS)))
                    .map(jsonElement -> TeambrellaServer.BASE_URL + jsonElement.getAsString())
                    .toList(ArrayList::new)
                    .onErrorReturn(throwable -> new ArrayList<>()).blockingGet();

            if (photos != null && photos.size() > 0) {
                Context context = getContext();
                Resources resources = context.getResources();
                TeambrellaImageLoader.getInstance(context).getPicasso()
                        .load(photos.get(0))
                        .resize(resources.getDimensionPixelSize(R.dimen.teammate_object_picture_width)
                                , resources.getDimensionPixelSize(R.dimen.teammate_object_picture_height))
                        .centerCrop()
                        .transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask))
                        .into(mObjectPicture);
                mObjectPicture.setOnClickListener(v -> v.getContext().startActivity(ImageViewerActivity.getLaunchIntent(context, photos, 0)));
            }

            basicObservable.doOnNext(basic -> AmountCurrencyUtil.setAmount(mNet, Math.round(basic.getFloat(TeambrellaModel.ATTR_DATA_TOTALLY_PAID_AMOUNT)), mCurrency))
                    .doOnNext(basic -> mRisk.setText(getString(R.string.risk_format_string, basic.getFloat(TeambrellaModel.ATTR_DATA_RISK) + 0.05f)))
                    .doOnNext(basic -> mTeamId = basic.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID, mTeamId))
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();

            mSeeClaims.setEnabled(mClaimCount > 0);
            mSeeClaims.setText(getContext().getString(R.string.see_claims_format_string, mClaimCount));

            mSeeClaims.setOnClickListener(v -> {
                if (mClaimId > 0) {
                    ClaimActivity.start(getContext(), mClaimId, mModel, mTeamId);
                } else {
                    ClaimsActivity.start(getContext(), mTeamId, mTeammateId, mCurrency);
                }
            });
        }

    }
}
