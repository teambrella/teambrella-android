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
 * Home Fragment
 */
public class HomeFragment extends ADataFragment<IDataHost> {


    private TextView mHeader;
    private ImageView mObjectPicture;
    private TextView mObjectModel;
    private TextView mCoverage;
    private TextView mWallet;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mDataHost.load(mTags[0]);
        mHeader = (TextView) view.findViewById(R.id.home_header);
        mObjectModel = (TextView) view.findViewById(R.id.model);
        mObjectPicture = (ImageView) view.findViewById(R.id.object_picture);
        mCoverage = (TextView) view.findViewById(R.id.coverage);
        return view;
    }


    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        Picasso picasso = TeambrellaImageLoader.getInstance(getContext()).getPicasso();

        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            mHeader.setText(data.getString(TeambrellaModel.ATTR_DATA_NAME));
            mObjectModel.setText(data.getString(TeambrellaModel.ATTR_DATA_OBJECT_NAME));
            picasso.load(TeambrellaModel.getImage(TeambrellaServer.AUTHORITY, data.getObject(), TeambrellaModel.ATTR_DATA_SMALL_PHOTO)).into(mObjectPicture);
            mCoverage.setText(Html.fromHtml(getString(R.string.coverage_format_string, Math.round((data.getFloat(TeambrellaModel.ATTR_DATA_COVERAGE) + 0.005) * 100))));
        }
    }
}
