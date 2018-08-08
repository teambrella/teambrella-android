package com.teambrella.android.ui.user.wallet;

import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.teambrella.android.R;
import com.teambrella.android.ui.IMainDataHost;

/**
 * Wallet backup info fragment
 */
public class WalletBackupInfoFragment extends BottomSheetDialogFragment {

    private IMainDataHost mMainDataHost;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainDataHost = (IMainDataHost) context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext(), R.style.InfoDialog) {
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                Window window = getWindow();
                if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                }
            }
        };
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_wallet_backup_info, null, false);
        view.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        view.findViewById(R.id.save_password).setOnClickListener(v -> {
            mMainDataHost.backUpWallet(true);
            dismiss();
        });
        view.findViewById(R.id.later).setOnClickListener(v -> dismiss());
        dialog.setContentView(view);
        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainDataHost = null;
    }
}
