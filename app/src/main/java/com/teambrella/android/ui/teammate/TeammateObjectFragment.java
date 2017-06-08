package com.teambrella.android.ui.teammate;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.squareup.picasso.Picasso;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaServer;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataFragment;
import com.teambrella.android.ui.image.ImageViewerActivity;
import com.teambrella.android.ui.widget.AmountWidget;

import java.util.ArrayList;

import io.reactivex.Notification;
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
        view.findViewById(R.id.see_claims).setOnClickListener(v -> Toast.makeText(getContext(), "Not implemented yet", Toast.LENGTH_SHORT).show());
        return view;
    }


    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonObject data = notification.getValue().get(TeambrellaModel.ATTR_DATA).getAsJsonObject();
            JsonObject objectData = data.get(TeambrellaModel.ATTR_DATA_ONE_OBJECT).getAsJsonObject();
            JsonObject objectBasic = data.get(TeambrellaModel.ATTR_DATA_ONE_BASIC).getAsJsonObject();
            if (objectData != null) {
                JsonArray photos = objectData.get(TeambrellaModel.ATTR_DATA_SMALL_PHOTOS).getAsJsonArray();
                Resources resources = getContext().getResources();
                if (photos != null && photos.size() > 0) {
                    Picasso.with(getContext())
                            .load(TeambrellaServer.AUTHORITY + photos.get(0).getAsString())
                            .resize(resources.getDimensionPixelSize(R.dimen.teammate_object_picture_width)
                                    , resources.getDimensionPixelSize(R.dimen.teammate_object_picture_height))
                            .centerCrop()
                            .transform(new MaskTransformation(getContext(), R.drawable.teammate_object_mask))
                            .into(mObjectPicture);

                    final ArrayList<String> uris = new ArrayList<>();
                    for (int i = 0; i < photos.size(); i++) {
                        uris.add(TeambrellaServer.AUTHORITY + photos.get(i).getAsString());
                    }

                    mObjectPicture.setOnClickListener(v -> v.getContext().startActivity(ImageViewerActivity.getLaunchIntent(v.getContext(), uris),
                            ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(getActivity(), mObjectPicture, TeambrellaServer.AUTHORITY + photos.get(0).getAsString()).toBundle()));

                }
                mObjectModel.setText(objectData.get(TeambrellaModel.ATTR_DATA_MODEL).getAsString());
                mLimit.setAmount(Math.round(objectData.get(TeambrellaModel.ATTR_DATA_CLAIM_LIMIT).getAsFloat()));
            }

            if (objectBasic != null) {
                mNet.setAmount(Math.round(objectBasic.get(TeambrellaModel.ATTR_DATA_TOTALLY_PAID_AMOUNT).getAsFloat()));
                mRisk.setText(getString(R.string.risk_format_string, objectBasic.get(TeambrellaModel.ATTR_DATA_RISK).getAsFloat() + 0.05f));
            }


        }
    }
}
