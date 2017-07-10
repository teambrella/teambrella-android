package com.teambrella.android.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.data.base.IDataHost;
import com.teambrella.android.ui.base.ADataFragment;

import io.reactivex.Notification;

/**
 * Home Cards Fragment.
 */
public class HomeCardsFragment extends ADataFragment<IDataHost> {


    private TextView mHeader;
    private ViewPager mCardsPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_cards, container, false);
        mHeader = (TextView) view.findViewById(R.id.home_header);
        mCardsPager = (ViewPager) view.findViewById(R.id.cards_pager);
        mCardsPager.setPageMargin(40);
        mCardsPager.setAdapter(new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return new CardsFragment();
            }

            @Override
            public int getCount() {
                return 3;
            }
        });

        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper response = new JsonWrapper(notification.getValue());
            JsonWrapper data = response.getObject(TeambrellaModel.ATTR_DATA);
            mHeader.setText(data.getString(TeambrellaModel.ATTR_DATA_NAME));
        }
    }


    public static final class CardsFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.home_claom_card, container, false);
        }
    }

}
