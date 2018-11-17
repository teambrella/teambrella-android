package com.teambrella.android.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Window;

import com.teambrella.android.R;

/**
 * Progress Fragment
 */
public class ProgressDialogFragment extends DialogFragment {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setCancelable(false);
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    ProgressDialog dialog = new ProgressDialog(getActivity(), getTheme());
    dialog.setMessage(getString(R.string.please_wait));
    dialog.setIndeterminate(true);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    return dialog;
  }
}
