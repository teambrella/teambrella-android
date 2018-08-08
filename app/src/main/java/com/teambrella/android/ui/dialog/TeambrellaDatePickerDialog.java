package com.teambrella.android.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Date Picker Dialog
 */
public class TeambrellaDatePickerDialog extends DialogFragment {

    private static final String EXTRA_YEAR = "year";
    private static final String EXTRA_MONTH = "month";
    private static final String EXTRA_DAY = "day";


    public static TeambrellaDatePickerDialog getInstance(int year, int month, int day) {
        TeambrellaDatePickerDialog fragment = new TeambrellaDatePickerDialog();
        Bundle args = new Bundle();
        args.putInt(EXTRA_YEAR, year);
        args.putInt(EXTRA_MONTH, month);
        args.putInt(EXTRA_DAY, day);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        final Bundle args = getArguments();
        int year = args.getInt(EXTRA_YEAR, c.get(Calendar.YEAR));
        int month = args.getInt(EXTRA_MONTH, c.get(Calendar.MONTH));
        int day = args.getInt(EXTRA_DAY, c.get(Calendar.DAY_OF_MONTH));
        DatePickerDialog dialog = new DatePickerDialog(getActivity(), (DatePickerDialog.OnDateSetListener) getActivity(), year, month, day);
        dialog.getDatePicker().setMaxDate(new Date().getTime());
        return dialog;
    }
}
