package com.teambrella.android.ui.chat;



import android.Manifest;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.teambrella.android.R;
import com.teambrella.android.api.TeambrellaException;
import com.teambrella.android.api.TeambrellaModel;
import com.teambrella.android.api.model.json.JsonWrapper;
import com.teambrella.android.api.server.TeambrellaUris;
import com.teambrella.android.dagger.Dependencies;
import com.teambrella.android.data.base.TeambrellaRequestFragment;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.TeambrellaBroadcastManager;
import com.teambrella.android.ui.base.dagger.ATeambrellaDaggerActivity;
import com.teambrella.android.ui.claim.ClaimActivity;
import com.teambrella.android.ui.claim.ReportClaimActivity;
import com.teambrella.android.ui.photos.PhotoAdapter;
import com.teambrella.android.util.ConnectivityUtils;
import com.teambrella.android.util.ImagePicker;

import java.util.Calendar;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class TakePhotosDialogFragment extends BottomSheetDialogFragment {

    private static final String DATA_REQUEST_FRAGMENT_TAG = "data_request";

    @Inject
    @Named(Dependencies.IMAGE_LOADER)
    TeambrellaImageLoader mTeambrellaImageLoader;

    private ChatActivity mChatActivity;
    private PhotoAdapter mPhotoAdapter;
    private Snackbar mSnackBar;
    private View topView;
    private ImagePicker mImagePicker;

    private Disposable mDisposable;

    public static TakePhotosDialogFragment getInstance() {
        return new TakePhotosDialogFragment();
    }

    public static void TakePhotos(ChatActivity fromChatActivity) {
//        if (ContextCompat.checkSelfPermission(fromChatActivity, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(fromChatActivity, new String[]{Manifest.permission.CAMERA},
//                    MY_CAMERA_PERMISSION_CODE);
//        } else {
//            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(cameraIntent, CAMERA_REQUEST);
//        }
    }


//    public static void TakePhotos(IChatActivity fromChatActivity) {
//        if (ContextCompat.checkSelfPermission(fromChatActivity, Manifest.permission.CAMERA)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(fromChatActivity, new String[]{Manifest.permission.CAMERA},
//                    MY_CAMERA_PERMISSION_CODE);
//        } else {
//            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(cameraIntent, CAMERA_REQUEST);
//        }
//    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mChatActivity = (ChatActivity) context;
        mChatActivity.getComponent().inject(this);
    }

    protected TeambrellaImageLoader getTeambrellaImageLoader() {
        return mTeambrellaImageLoader;
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


        mChatActivity.getComponent().inject(this);

        mImagePicker = new ImagePicker(mChatActivity);

        topView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_take_photos, null, false);
        topView.findViewById(R.id.close).setOnClickListener(v -> dismiss());

        RecyclerView mPhotos = topView.findViewById(R.id.photos);
        mPhotoAdapter = new PhotoAdapter(getContext());
        mChatActivity.getComponent().inject(mPhotoAdapter);
        mPhotos.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mPhotos.setAdapter(mPhotoAdapter);

        new ItemTouchHelper(new ItemTouchCallback()).attachToRecyclerView(mPhotos);

//        GlideApp.with(this).load(getTeambrellaImageLoader().getImageUrl(intent.getStringExtra(EXTRA_IMAGE_URI)))
//                .apply(new RequestOptions().transforms(new CenterCrop()
//                        , new RoundedCorners(getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp)))).into((ImageView) findViewById(R.id.object_icon));


        FragmentManager fragmentManager = mChatActivity.getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG) == null) {
            fragmentManager.beginTransaction().add(new TeambrellaRequestFragment(), DATA_REQUEST_FRAGMENT_TAG).commit();
        }


//        LinearLayout list = view.findViewById(R.id.list);
//        LayoutInflater inflater = LayoutInflater.from(getContext());

//        IChatActivity.MuteStatus muteStatus = mChatActivity != null ? mChatActivity.getMuteStatus() : IChatActivity.MuteStatus.DEFAULT;
//        View unmute = inflater.inflate(R.layout.list_item_chat_notification_option, list, false);
//        initListItem(unmute, VIEW_TYPE_UNMUTE, muteStatus == IChatActivity.MuteStatus.UMMUTED);
//        list.addView(unmute);
//        unmute.setOnClickListener(v -> {
//            if (mChatActivity != null) {
//                mChatActivity.setChatMuted(false);
//                dismiss();
//            }
//        });
//        View mute = inflater.inflate(R.layout.list_item_chat_notification_option, list, false);
//        initListItem(mute, VIEW_TYPE_MUTE, muteStatus == IChatActivity.MuteStatus.MUTED || muteStatus == IChatActivity.MuteStatus.DEFAULT);
//        list.addView(mute);
//        mute.setOnClickListener(v -> {
//            if (mChatActivity != null) {
//                mChatActivity.setChatMuted(true);
//                dismiss();
//            }
//        });

        topView.findViewById(R.id.add_photos).setOnClickListener(this::onClick);
        topView.findViewById(R.id.submit_photos).setOnClickListener(this::onClick);

        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(topView);
        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mChatActivity = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) mChatActivity.getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            mDisposable = fragment.getObservable().subscribe(this::onRequestResult);
            fragment.start();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) mChatActivity.getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.stop();
        }
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        mDisposable = null;
    }

    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_photos:
                mImagePicker.startPickingInFragment(getString(R.string.choose), this);
                break;
            case R.id.submit_photos:
                //mSubmitClaimDone.setEnabled(!submitClaim());
                break;
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Observable<ImagePicker.ImageDescriptor> observable = mImagePicker.onActivityResult(requestCode, resultCode, data);
        if (observable != null) {
            observable.subscribe(this::onImagePickerResult, this::onImagePickerError);
        }
    }

    private void onImagePickerResult(ImagePicker.ImageDescriptor descriptor) {
        String path = descriptor.file.getAbsolutePath();
        mPhotoAdapter.addPhoto(path);
        request(TeambrellaUris.getNewFileUri(path));
    }

    private void onImagePickerError(Throwable throwable) {

    }

    private void onRequestResult(Notification<JsonObject> response) {

        if (response.isOnNext()) {
            String requestUriString = Observable.just(response.getValue()).map(JsonWrapper::new)
                    .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_STATUS))
                    .map(jsonWrapper -> jsonWrapper.getString(TeambrellaModel.ATTR_STATUS_URI))
                    .blockingFirst(null);
            if (requestUriString != null) {
                Uri requestUri = Uri.parse(requestUriString);
                switch (TeambrellaUris.sUriMatcher.match(requestUri)) {
                    case TeambrellaUris.NEW_FILE:
                        String filePath = requestUri.getQueryParameter(TeambrellaUris.KEY_URI);
                        String imageUri = Observable.just(response.getValue()).map(JsonWrapper::new)
                                .map(jsonWrapper -> jsonWrapper.getJsonArray(TeambrellaModel.ATTR_DATA))
                                .map(jsonElements -> jsonElements.get(0).getAsString()).blockingFirst(null);
                        if (filePath != null && imageUri != null) {
                            mPhotoAdapter.updatePhoto(filePath, imageUri);
                        }
                        break;
//                    case TeambrellaUris.NEW_CLAIM:
//                        int claimId = Observable.just(response.getValue()).map(JsonWrapper::new)
//                                .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
//                                .map(jsonWrapper -> jsonWrapper.getInt(TeambrellaModel.ATTR_DATA_ID)).blockingFirst();
//                        startActivity(ClaimActivity.Companion.getLaunchIntent(this, claimId, getIntent().getStringExtra(EXTRA_NAME), mTeamId));
//                        new TeambrellaBroadcastManager(this).notifyClaimSubmitted();
//                        finish();
//                        break;
                }
            }
        } else if (response.isOnError()) {
            TeambrellaException exception = (TeambrellaException) response.getError();
            final Uri uri = exception.getUri();
            switch (TeambrellaUris.sUriMatcher.match(uri)) {
                case TeambrellaUris.NEW_FILE:
                    String filePath = uri.getQueryParameter(TeambrellaUris.KEY_URI);
                    if (filePath != null) {
                        mPhotoAdapter.removePhoto(filePath);
                    }
                    break;
//                case TeambrellaUris.NEW_CLAIM:
//                    FragmentManager fragmentManager = getSupportFragmentManager();
//                    fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(PLEASE_WAIT_DIALOG_FRAGMENT_TAG)).commit();
//                    break;
            }

            showSnackBar(ConnectivityUtils.isNetworkAvailable(getContext())
                    ? R.string.something_went_wrong_error : R.string.no_internet_connection);

        }
//        invalidateOptionsMenu();
    }


    public void request(Uri uri) {
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) mChatActivity.getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.request(uri);
        }
    }


    private void initListItem(View itemView, int viewType, boolean checked) {
        ImageView icon = itemView.findViewById(R.id.icon);
        TextView title = itemView.findViewById(R.id.title);
        TextView subtitle = itemView.findViewById(R.id.subtitle);
        View isSelected = itemView.findViewById(R.id.isSelected);

//        switch (viewType) {
//            case VIEW_TYPE_UNMUTE:
//                icon.setImageResource(R.drawable.ic_icon_bell_green);
//                title.setText(R.string.notification_option_unmuted_title);
//                subtitle.setText(R.string.notification_option_unmuted_description);
//                isSelected.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
//                break;
//            case VIEW_TYPE_MUTE:
//                icon.setImageResource(R.drawable.ic_icon_bell_muted_red);
//                title.setText(R.string.notification_option_muted_title);
//                subtitle.setText(R.string.notification_option_muted_description);
//                isSelected.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
//                break;
//        }
    }


    private void showSnackBar(@StringRes int text) {
        if (mSnackBar == null) {
            mSnackBar = Snackbar.make(topView.findViewById(R.id.container), text, Snackbar.LENGTH_LONG);

            mSnackBar.addCallback(new Snackbar.Callback() {
                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);
                }

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    mSnackBar = null;
                }
            });
            mSnackBar.show();
        }
    }

    private class ItemTouchCallback extends ItemTouchHelper.SimpleCallback {

        ItemTouchCallback() {
            super(ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT, 0);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mPhotoAdapter.exchangeItems(viewHolder, target);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            // nothing to do
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_DRAG:
                    viewHolder.itemView.setAlpha(0.5f);
                    break;
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1f);
        }
    }
}
