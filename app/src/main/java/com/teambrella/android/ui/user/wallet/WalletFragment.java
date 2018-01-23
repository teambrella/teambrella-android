package com.teambrella.android.ui.user.wallet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.backup.WalletBackupManager;
import com.teambrella.android.ui.CosignersActivity;
import com.teambrella.android.ui.IMainDataHost;
import com.teambrella.android.ui.QRCodeActivity;
import com.teambrella.android.ui.TeambrellaUser;
import com.teambrella.android.ui.base.ADataProgressFragment;
import com.teambrella.android.ui.wallet.WalletTransactionsActivityKt;
import com.teambrella.android.ui.widget.TeambrellaAvatarsWidgets;
import com.teambrella.android.ui.withdraw.WithdrawActivity;
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.ConnectivityUtils;
import com.teambrella.android.util.QRCodeUtils;
import com.teambrella.android.util.StatisticHelper;

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
    private View mTransactionsView;
    private TeambrellaAvatarsWidgets mCosignersAvatar;
    private TextView mCosignersCountView;
    private View mBackupWalletButton;
    private View mBackupWalletMessage;
    private boolean mShowBackupInfoOnShow;

    private TeambrellaUser mUser;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mUser = TeambrellaUser.get(context);
    }

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
        mBackupWalletMessage = view.findViewById(R.id.wallet_not_backed_up_message);
        mTransactionsView = view.findViewById(R.id.transactions);

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
        view.findViewById(R.id.transactions).setOnClickListener(v -> startActivity(WalletTransactionsActivityKt.getLaunchIntent(getContext(), mDataHost.getTeamId())));

        mBackupWalletButton = view.findViewById(R.id.backup_wallet);

        mDataHost.addWalletBackupListener(this);

        view.findViewById(R.id.wallet_not_backed_up_message).setOnClickListener(v -> mDataHost.showWalletBackupDialog());

        return view;


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDataHost.removeWalletBackupListener(this);
    }

    @Override
    public void onWalletSaved(boolean force) {
        mBackupWalletButton.setVisibility(View.VISIBLE);
        mBackupWalletMessage.setVisibility(View.GONE);
        if (force) {
            Toast.makeText(getContext(), R.string.your_wallet_is_backed_up, Toast.LENGTH_SHORT).show();
            StatisticHelper.onWalletSaved(getContext(), mUser.getUserId());
        }
    }

    @Override
    public void onWalletSaveError(int code, boolean force) {
        if (code == RESOLUTION_REQUIRED) {
            mBackupWalletMessage.setVisibility(View.VISIBLE);
            mBackupWalletMessage.setOnClickListener(v -> mDataHost.backUpWallet(true));
            showWalletBackupInfo();
        } else {

            if (force && code != WalletBackupManager.IWalletBackupListener.CANCELED) {
                mDataHost.showSnackBar(ConnectivityUtils.isNetworkAvailable(getContext()) ? R.string.something_went_wrong_error
                        : R.string.no_internet_connection);
            }

            if (!force) {
                mBackupWalletMessage.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && mShowBackupInfoOnShow) {
            showWalletBackupInfo();
        }
    }

    @Override
    protected void onReload() {
        super.onReload();
        mDataHost.load(mTags[0]);
        if (!mUser.isDemoUser()) {
            mDataHost.backUpWallet(false);
        }
    }

    @Override
    public void onWalletReadError(int code, boolean force) {

    }

    @Override
    public void onWalletRead(String key, boolean force) {

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
                    .map(jsonElement -> jsonElement.getAsJsonObject().get(TeambrellaModel.ATTR_DATA_AVATAR).getAsString())
                    .toList()
                    .subscribe((uris) -> {
                        mCosignersAvatar.setAvatars(getImageLoader(), uris);
                        mCosignersCountView.setText(Integer.toString(uris.size()));
                    }, e -> {
                    });

            mCosignersView.setOnClickListener(view -> CosignersActivity.start(getContext(), data.getJsonArray(TeambrellaModel.ATTR_DATA_COSIGNERS).toString()
                    , mDataHost.getTeamId()));


            if (!mUser.isDemoUser()) {
                mDataHost.backUpWallet(false);
            }

        }
        setContentShown(true);
    }


    private void showWalletBackupInfo() {
        if (!mUser.isDemoUser()) {
            if (getUserVisibleHint()) {
                if (!mUser.isBackupInfoDialogShown()) {
                    mDataHost.showWalletBackupDialog();
                    mUser.setBackupInfodialogShown(true);
                }
                mShowBackupInfoOnShow = false;
            } else {
                mShowBackupInfoOnShow = true;
            }
        }
    }
}
