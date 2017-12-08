package com.teambrella.android.ui.chat;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.teambrella.android.R;

/**
 * Notifications Settings
 */
public class NotificationsSettingsDialogFragment extends BottomSheetDialogFragment {

    private static final int VIEW_TYPE_UNMUTE = 0;
    private static final int VIEW_TYPE_MUTE = 1;


    public static NotificationsSettingsDialogFragment getInstance() {
        return new NotificationsSettingsDialogFragment();
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

        final View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_notifications_setttings, null, false);
        view.findViewById(R.id.close).setOnClickListener(v -> dismiss());
        LinearLayout list = view.findViewById(R.id.list);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View unmute = inflater.inflate(R.layout.list_item_chat_notification_option, list, false);
        initListItem(unmute, VIEW_TYPE_UNMUTE);
        list.addView(unmute);
        View unmuted = inflater.inflate(R.layout.list_item_chat_notification_option, list, false);
        initListItem(unmuted, VIEW_TYPE_MUTE);
        list.addView(unmuted);
        dialog.setContentView(view);
        return dialog;
    }


    private void initListItem(View itemView, int viewType) {
        ImageView icon = itemView.findViewById(R.id.icon);
        TextView title = itemView.findViewById(R.id.title);
        TextView subtitle = itemView.findViewById(R.id.subtitle);
        View isSelected = itemView.findViewById(R.id.isSelected);
        switch (viewType) {
            case VIEW_TYPE_UNMUTE:
                icon.setImageResource(R.drawable.ic_icon_bell_green);
                title.setText(R.string.notification_option_unmuted_title);
                subtitle.setText(R.string.notification_option_unmuted_description);
                isSelected.setVisibility(View.VISIBLE);
                break;
            case VIEW_TYPE_MUTE:
                icon.setImageResource(R.drawable.ic_icon_bell_muted_red);
                title.setText(R.string.notification_option_muted_title);
                subtitle.setText(R.string.notification_option_muted_description);
                isSelected.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
