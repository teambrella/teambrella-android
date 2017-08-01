package com.teambrella.android.ui.claim;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.teambrella.android.R;
import com.teambrella.android.ui.dialog.TeambrellaDatePickerDialog;

/**
 * Activity to report a claim
 */
public class ReportClaimActivity extends AppCompatActivity {

    public static final String DATE_PICKER_FRAGMENT_TAG = "date_picker";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_claim);
        findViewById(R.id.date).setOnClickListener(v -> showDatePicker());
    }

    /**
     * Show Date Picker
     */
    private void showDatePicker() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(DATE_PICKER_FRAGMENT_TAG) == null) {
            new TeambrellaDatePickerDialog().show(fragmentManager, DATE_PICKER_FRAGMENT_TAG);
        }
    }
}
