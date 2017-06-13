package com.teambrella.android.data;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.data.base.IDataPager;
import com.teambrella.android.data.base.TeambrellaDataPagerLoader;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLSocketFactory;

/**
 * Main Data Fragment
 */
public class MainDataFragment extends Fragment {

    private static final String EXTRA_TEAM_ID = "teamId";

    private TeambrellaDataPagerLoader mTeamListDataLoader;
    private TeambrellaDataPagerLoader mClaimListDataLoder;
    private WebSocketClient mWebSocketClient;

    public static MainDataFragment getInstance(int teamId) {
        MainDataFragment fragment = new MainDataFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TEAM_ID, teamId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle args = getArguments();
        mTeamListDataLoader = new TeambrellaDataPagerLoader(getContext(),
                TeambrellaUris.getTeamUri(args.getInt(EXTRA_TEAM_ID)),
                TeambrellaModel.ATTR_DATA_TEAMMATES);

        mClaimListDataLoder = new TeambrellaDataPagerLoader(getContext(),
                TeambrellaUris.getClaimsUri(args.getInt(EXTRA_TEAM_ID)),
                null);

        connectWebSocket();
    }

    public IDataPager<JsonArray> getTeamListPager() {
        return mTeamListDataLoader;
    }

    public IDataPager<JsonArray> getClaimsListPager() {
        return mClaimListDataLoder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebSocketClient.close();
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("wss://teambrella.com/echo2.ashx");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
                mWebSocketClient.send("test");
            }

            @Override
            public void onMessage(String s) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };

        try {
            mWebSocketClient.setSocket(SSLSocketFactory.getDefault().createSocket());
        } catch (IOException e) {
        }
        mWebSocketClient.connect();
    }

}
