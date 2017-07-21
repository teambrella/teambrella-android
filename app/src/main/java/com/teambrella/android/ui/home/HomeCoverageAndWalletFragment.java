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
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.base.ADataFragment;

import io.reactivex.Notification;

/**
 * Coverage and Wallet fragment.
 */
public class HomeCoverageAndWalletFragment extends ADataFragment<IDataHost> {


    private ImageView mObjectPicture;
    private TextView mObjectModel;
    private TextView mCoverage;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_coverage_and_wallet, container, false);

        mObjectModel = (TextView) view.findViewById(R.id.model);
        mObjectPicture = (ImageView) view.findViewById(R.id.object_picture);
        mCoverage = (TextView) view.findViewById(R.id.coverage);

        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            mObjectModel.setText(data.getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME));
            picasso.load(TeambrellaModel.getImage(TeambrellaServer.BASE_URL, data.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO)).into(mObjectPicture);
            mCoverage.setText(Html.fromHtml(getString(R.string.coverage_format_string, Math.round((data.getFloat(TeambrellaModel.ATTR_DATA_COVERAGE) + 0.005) * 100))));
        }
    }
}
