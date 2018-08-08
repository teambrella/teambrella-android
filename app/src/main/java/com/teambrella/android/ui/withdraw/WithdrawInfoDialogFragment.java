package com.teambrella.android.ui.withdraw;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.teambrella.android.R;
import com.teambrella.android.util.AmountCurrencyUtil;

/**
 * Withdraw Info Dialog
 */
public class WithdrawInfoDialogFragment extends BottomSheetDialogFragment {

    private static final String EXTRA_AVAILABLE = "available";
    private static final String EXTRA_RESERVED = "reserved";


    /**
     * Get instance of the fragment
     *
     * @param available available amount
     * @param reserved  reserved amount
     * @return fragment
     */
    public static WithdrawInfoDialogFragment getInstance(int available, int reserved) {
        WithdrawInfoDialogFragment fragment = new WithdrawInfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_AVAILABLE, available);
        args.putInt(EXTRA_RESERVED, reserved);
        fragment.setArguments(args);
        return fragment;
    }


    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle args = getArguments();
        final Resources resources = getResources();
        int available = args != null ? args.getInt(EXTRA_AVAILABLE) : 0;
        int reserved = args != null ? args.getInt(EXTRA_RESERVED) : 0;
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

        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_withdraw_info, null, false);
        String text = reserved > 0 ? resources.getString(R.string.withdraw_info_description_reserved, available, reserved)
                : resources.getString(R.string.withdraw_info_description, available);
        view.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        TextView description = view.findViewById(R.id.description);
        AmountCurrencyUtil.setAmount(description, text, "mETH");
        dialog.setContentView(view);
        return dialog;
    }
}
