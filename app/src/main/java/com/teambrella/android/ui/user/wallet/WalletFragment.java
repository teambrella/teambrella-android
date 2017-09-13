package com.teambrella.android.ui.user.wallet;

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
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.util.AmountCurrencyUtil;

import java.util.Locale;

import io.reactivex.Notification;

/**
 * Wallet Fragment
 */
public class WalletFragment extends ADataProgressFragment<IMainDataHost> {


    private TextView mCryptoBalanceView;
    private TextView mBalanceView;
    private TextView mCurrencyView;
    private TextView mReservedValueView;
    private TextView mAvailableValueView;
    private ImageView mQRCodeView;


    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        mCryptoBalanceView = view.findViewById(R.id.crypto_balance);
        mBalanceView = view.findViewById(R.id.balance);
        mCurrencyView = view.findViewById(R.id.currency);
        mReservedValueView = view.findViewById(R.id.reserved_value);
        mAvailableValueView = view.findViewById(R.id.available_value);
        mQRCodeView = view.findViewById(R.id.qr_code);
        if (savedInstanceState == null) {
            mDataHost.load(mTags[0]);
            setContentShown(false);
        }
        return view;
    }

    @Override
    protected void onDataUpdated(Notification<JsonObject> notification) {
        if (notification.isOnNext()) {
            JsonWrapper data = new JsonWrapper(notification.getValue()).getObject(TeambrellaModel.ATTR_DATA);
            float cryptoBalance = data.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_BALANCE);
            float reservedValue = data.getFloat(TeambrellaModel.ATTR_DATA_CRYPTO_RESERVED);
            float availableValue = cryptoBalance - reservedValue;
            int stringId = cryptoBalance > 1 ? R.string.ethereum : R.string.milli_ethereum;
            String cryptoCurrency = getContext().getString(stringId);
            switch (stringId) {
                case R.string.ethereum:
                    mCryptoBalanceView.setText(String.format(Locale.US, "%.2f", cryptoBalance));
                    break;
                case R.string.milli_ethereum:
                    mCryptoBalanceView.setText(String.format(Locale.US, "%d", Math.round(cryptoBalance * 1000)));
                    break;

            }
            mCurrencyView.setText(cryptoCurrency);
            AmountCurrencyUtil.setCryptoAmount(mReservedValueView, reservedValue);
            AmountCurrencyUtil.setCryptoAmount(mAvailableValueView, availableValue);


            mBalanceView.setText(getContext().getString(R.string.amount_format_string
                    , AmountCurrencyUtil.getCurrencySign(mDataHost.getCurrency())
                    , Math.round(cryptoBalance * data.getFloat(TeambrellaModel.ATTR_DATA_CURRENCY_RATE))));

            
            //mQRCodeView.setImageBitmap(QRCodeUtils.createBitmap("qdsdasdasd"));

        } else {

        }
        setContentShown(true);
    }
}
