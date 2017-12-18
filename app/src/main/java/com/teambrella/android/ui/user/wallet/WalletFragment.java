package com.teambrella.android.ui.user.wallet;

import android.annotation.SuppressLint;
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
import com.teambrella.android.backup.WalletBackupManager;
import com.teambrella.android.ui.CosignersActivity;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.QRCodeActivity;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;
import com.teambrella.android.ui.withdraw.WithdrawActivity;
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.QRCodeUtils;

import java.util.Locale;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Wallet Fragment
 */
public class WalletFragment extends ADataProgressFragment<IMainDataHost> implements WalletBackupManager.IWalletBackupListener {


    private TextView mCryptoBalanceView;
    private TextView mBalanceView;
    private TextView mCurrencyView;
    private TextView mReservedValueView;
    private TextView mAvailableValueView;
    private TextView mMaxCoverageCryptoValue;
    private TextView mUninterruptedCoverageCryptoValue;
    private TextView mMaxCoverageCurrencyValue;
    private TextView mUninterruptedCoverageCurrencyValue;
    private View mCosignersView;
    private TeambrellaAvatarsWidgets mCosignersAvatar;
    private TextView mCosignersCountView;
    private TextView mBackupWalletButton;


    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        mCryptoBalanceView = view.findViewById(R.id.crypto_balance);
        mBalanceView = view.findViewById(R.id.balance);
        mCurrencyView = view.findViewById(R.id.currency);
        mReservedValueView = view.findViewById(R.id.reserved_value);
        mAvailableValueView = view.findViewById(R.id.available_value);
        ImageView QRCodeView = view.findViewById(R.id.qr_code);
        mMaxCoverageCryptoValue = view.findViewById(R.id.for_max_coverage_crypto_value);
        mMaxCoverageCurrencyValue = view.findViewById(R.id.for_max_coverage_currency_value);
        mUninterruptedCoverageCryptoValue = view.findViewById(R.id.for_uninterrupted_coverage_crypto_value);
        mUninterruptedCoverageCurrencyValue = view.findViewById(R.id.for_uninterrupted_coverage_currency_value);
        TextView fundWalletButton = view.findViewById(R.id.fund_wallet);
        mCosignersView = view.findViewById(R.id.cosigners);
        mCosignersAvatar = view.findViewById(R.id.cosigners_avatar);
        mCosignersCountView = view.findViewById(R.id.cosigners_count);

        if (savedInstanceState == null) {
            mDataHost.load(mTags[0]);
            setContentShown(false);
        }

        mCurrencyView.setText(getString(R.string.milli_ethereum));
        AmountCurrencyUtil.setCryptoAmount(mReservedValueView, 0);
        AmountCurrencyUtil.setCryptoAmount(mAvailableValueView, 0);

        mBalanceView.setText(getContext().getString(R.string.amount_format_string
                , AmountCurrencyUtil.getCurrencySign(mDataHost.getCurrency())
                , 0));


        String fundAddress = mDataHost.getFundAddress();

        if (fundAddress != null) {
            Observable.just(fundAddress).map(QRCodeUtils::createBitmap)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(QRCodeView::setImageBitmap, throwable -> {
                    });
            fundWalletButton.setEnabled(true);
            QRCodeView.setVisibility(View.VISIBLE);
            QRCodeView.setOnClickListener(v -> QRCodeActivity.startQRCode(getContext(), fundAddress));
            fundWalletButton.setOnClickListener(v -> QRCodeActivity.startQRCode(getContext(), fundAddress));
        } else {
            fundWalletButton.setEnabled(false);
            QRCodeView.setVisibility(View.INVISIBLE);
        }


        AmountCurrencyUtil.setCryptoAmount(mMaxCoverageCryptoValue, 0);
        mMaxCoverageCurrencyValue.setText(getContext().getString(R.string.amount_format_string
                , AmountCurrencyUtil.getCurrencySign(mDataHost.getCurrency())
                , 0));


        AmountCurrencyUtil.setCryptoAmount(mUninterruptedCoverageCryptoValue, 0);
        mUninterruptedCoverageCurrencyValue.setText(getContext().getString(R.string.amount_format_string
                , AmountCurrencyUtil.getCurrencySign(mDataHost.getCurrency())
                , 0));

        mCryptoBalanceView.setText(String.format(Locale.US, "%d", 0));

        view.findViewById(R.id.withdraw).setOnClickListener(v -> WithdrawActivity.start(getContext(), mDataHost.getTeamId()));

        mBackupWalletButton = view.findViewById(R.id.backup_wallet);

        mDataHost.addWalletBackupListener(this);
        mDataHost.backUpWallet(false);

        return view;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDataHost.removeWalletBackupListener(this);
    }

    @Override
    public void onWalletSaved() {
        mBackupWalletButton.setText(R.string.your_wallet_is_backed_up);
    }

    @Override
    public void onWalletSaveError(int code) {
        if (code == RESOLUTION_REQUIRED) {
            mBackupWalletButton.setText(R.string.backup_wallet);
            mBackupWalletButton.setOnClickListener(v -> mDataHost.backUpWallet(true));
        }
    }

    @Override
    public void onWalletReadError(int code) {

    }

    @Override
    public void onWalletRead(String key) {

    }

    @SuppressLint("SetTextI18n")
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

            float forMaxCoverage = Math.abs(data.getFloat(TeambrellaModel.ATTR_DATA_NEED_CRYPTO));
            AmountCurrencyUtil.setCryptoAmount(mMaxCoverageCryptoValue, forMaxCoverage);
            mMaxCoverageCurrencyValue.setText(getContext().getString(R.string.amount_format_string
                    , AmountCurrencyUtil.getCurrencySign(mDataHost.getCurrency())
                    , Math.round(forMaxCoverage * data.getFloat(TeambrellaModel.ATTR_DATA_CURRENCY_RATE))));


            float forUninterruptedCoverage = Math.abs(data.getFloat(TeambrellaModel.ATTR_DATA_RECOMMENDED_CRYPTO));
            AmountCurrencyUtil.setCryptoAmount(mUninterruptedCoverageCryptoValue, forUninterruptedCoverage);
            mUninterruptedCoverageCurrencyValue.setText(getContext().getString(R.string.amount_format_string
                    , AmountCurrencyUtil.getCurrencySign(mDataHost.getCurrency())
                    , Math.round(forUninterruptedCoverage * data.getFloat(TeambrellaModel.ATTR_DATA_CURRENCY_RATE))));

            Observable.just(data).flatMap(jsonWrapper -> Observable.fromIterable(jsonWrapper.getJsonArray(TeambrellaModel.ATTR_DATA_COSIGNERS)))
                    .map(jsonElement -> TeambrellaServer.BASE_URL + jsonElement.getAsJsonObject().get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString())
                    .toList()
                    .subscribe((uris) -> {
                        mCosignersAvatar.setAvatars(uris);
                        mCosignersCountView.setText(Integer.toString(uris.size()));
                    }, e -> {
                    });

            mCosignersView.setOnClickListener(view -> CosignersActivity.start(getContext(), data.getJsonArray(TeambrellaModel.ATTR_DATA_COSIGNERS).toString()
                    , mDataHost.getTeamId()));


        }
        setContentShown(true);
    }
}
