package com.teambrella.android.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.data.MainDataFragment;
import com.teambrella.android.ui.home.HomeFragment;
import com.teambrella.android.ui.profile.ProfileFragment;
import com.teambrella.android.ui.proxies.ProxiesFragment;
import com.teambrella.android.ui.team.TeamFragment;

import java.lang.reflect.Field;

import io.reactivex.Notification;
import io.reactivex.Observable;


/**
 * Main Activity
 */
public class MainActivity extends AppCompatActivity implements IMainDataHost {

    private static final String HOME_TAG = "home";
    private static final String TEAM_TAG = "team";
    private static final String PROXIES_TAG = "proxies";
    private static final String PROFILE_TAG = "profile";
    private static final String DATA_PROVIDER_TAG = "data_provider";


    private int mSelectedItemId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.bottom_bar);
        BottomNavigationViewHelper.removeShiftMode(navigationView);
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.container, new HomeFragment(), HOME_TAG)
                .add(new MainDataFragment(), DATA_PROVIDER_TAG).commit();

        mSelectedItemId = R.id.bottom_navigation_home;
        navigationView.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }


    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentByTag(getTagById(mSelectedItemId));
        String newFragmentTag = getTagById(item.getItemId());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.detach(currentFragment);
        Fragment newFragment = fragmentManager.findFragmentByTag(newFragmentTag);

        if (newFragment != null) {
            transaction.attach(newFragment);
        } else {
            transaction.add(R.id.container, createFragmentByTag(newFragmentTag), newFragmentTag);
        }
        transaction.commit();
        mSelectedItemId = item.getItemId();
        return true;
    }


    @NonNull
    private String getTagById(int id) {
        switch (id) {
            case R.id.bottom_navigation_home:
                return HOME_TAG;
            case R.id.bottom_navigation_team:
                return TEAM_TAG;
            case R.id.bottom_navigation_proxies:
                return PROXIES_TAG;
            case R.id.bottom_navigation_me:
                return PROFILE_TAG;
            default:
                throw new RuntimeException("unknown item id");
        }
    }


    @NonNull
    private Fragment createFragmentByTag(String tag) {
        switch (tag) {
            case HOME_TAG:
                return new HomeFragment();
            case TEAM_TAG:
                return new TeamFragment();
            case PROFILE_TAG:
                return new ProfileFragment();
            case PROXIES_TAG:
                return new ProxiesFragment();
            default:
                throw new RuntimeException("unknown tag " + tag);
        }
    }


    @Override
    public void requestTeamList(int teamID, int offset, int limit) {
        MainDataFragment dataFragment = (MainDataFragment) getSupportFragmentManager().findFragmentByTag(DATA_PROVIDER_TAG);
        dataFragment.requestTeamList(teamID, offset, limit);
    }

    @Override
    public Observable<Notification<JsonObject>> getTeamListObservable() {
        MainDataFragment dataFragment = (MainDataFragment) getSupportFragmentManager().findFragmentByTag(DATA_PROVIDER_TAG);
        return dataFragment.getTeamListObservable();
    }
}

class BottomNavigationViewHelper {

    static void removeShiftMode(BottomNavigationView view) {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
        try {
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShiftingMode(false);
                // set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.e("ERROR NO SUCH FIELD", "Unable to get shift mode field");
        } catch (IllegalAccessException e) {
            Log.e("ERROR ILLEGAL ALG", "Unable to change value of shift mode");
        }
    }
}
