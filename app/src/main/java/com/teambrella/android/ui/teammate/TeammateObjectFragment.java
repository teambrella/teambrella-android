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
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.image.ImageViewerActivity;
import com.teambrella.android.ui.widget.AmountWidget;

import java.util.ArrayList;

import io.reactivex.Notification;
import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.MaskTransformation;

/**
 * Teammate Object Fragment
 */
public class TeammateObjectFragment extends ADataFragment<IDataHost> {

    private ImageView mObjectPicture;
    private TextView mObjectModel;
    private AmountWidget mLimit;
    private AmountWidget mNet;
    private TextView mRisk;
    private View mSeeClaims;

    private int mTeammateId;
    private int mTeamId;


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
        mObjectModel = (TextView) view.findViewById(R.id.model);
        mObjectPicture = (ImageView) view.findViewById(R.id.object_picture);
        mLimit = (AmountWidget) view.findViewById(R.id.limit);
        mNet = (AmountWidget) view.findViewById(R.id.net);
        mRisk = (TextView) view.findViewById(R.id.risk);
        mSeeClaims = view.findViewById(R.id.see_claims);
        return view;
    }


    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {

            Observable<JsonWrapper> responseObservable = Observable.just(notification.getValue())
                    .map(JsonWrapper::new);

            Observable<JsonWrapper> dataObservable =
                    responseObservable.map(item -> item.getObject(TeambrellaModel.ATTR_DATA));

            Observable<JsonWrapper> basicObservable =
                    dataObservable.map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA_ONE_BASIC));


            Observable<JsonWrapper> objectObservable =
                    dataObservable.map(item -> item.getObject(TeambrellaModel.ATTR_DATA_ONE_OBJECT));

            objectObservable.doOnNext(objectData -> mObjectModel.setText(objectData.getString(TeambrellaModel.ATTR_DATA_MODEL)))
                    .doOnNext(objectData -> mLimit.setAmount(Math.round(objectData.getFloat(TeambrellaModel.ATTR_DATA_CLAIM_LIMIT))))
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


            basicObservable.doOnNext(basic -> mNet.setAmount(Math.round(basic.getFloat(TeambrellaModel.ATTR_DATA_TOTALLY_PAID_AMOUNT))))
                    .doOnNext(basic -> mRisk.setText(getString(R.string.risk_format_string, basic.getFloat(TeambrellaModel.ATTR_DATA_RISK) + 0.05f)))
                    .onErrorReturnItem(new JsonWrapper(null)).blockingFirst();


//            if (objectBasic != null) {
//                mNet.setAmount(Math.round(objectBasic.getFloat(TeambrellaModel.ATTR_DATA_TOTALLY_PAID_AMOUNT, 0f)));
//                mRisk.setText(getString(R.string.risk_format_string, objectBasic.getFloat(TeambrellaModel.ATTR_DATA_RISK, 0f) + 0.05f));
//                mSeeClaims.setOnClickListener(v -> startActivity(ClaimsActivity.getLaunchIntent(getContext()
//                        , TeambrellaUris.getClaimsUri(objectBasic.getInt(TeambrellaModel.ATTR_DATA_TEAM_ID, 0), data.getInt(TeambrellaModel.ATTR_DATA_ID, 0)))));
//
//            }
        }
    }
}
