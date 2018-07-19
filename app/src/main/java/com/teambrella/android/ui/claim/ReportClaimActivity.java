package com.teambrella.android.ui.claim;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.teambrella.android.data.base.TeambrellaRequestFragment;
import com.teambrella.android.image.glide.GlideApp;
import com.teambrella.android.ui.base.ITeambrellaDaggerActivity;
import com.teambrella.android.ui.base.TeambrellaBroadcastManager;
import com.teambrella.android.ui.base.TeambrellaDaggerActivity;
import com.teambrella.android.ui.dialog.ProgressDialogFragment;
import com.teambrella.android.ui.dialog.TeambrellaDatePickerDialog;
import com.teambrella.android.ui.photos.PhotoAdapter;
import com.teambrella.android.util.AmountCurrencyUtil;
import com.teambrella.android.util.ConnectivityUtils;
import com.teambrella.android.util.ImagePicker;
import com.teambrella.android.util.TeambrellaDateUtils;

import java.util.Calendar;
import java.util.regex.Pattern;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;


/**
 * Activity to report a claim
 */
public class ReportClaimActivity extends TeambrellaDaggerActivity implements DatePickerDialog.OnDateSetListener, ITeambrellaDaggerActivity {

    private static final String EXTRA_IMAGE_URI = "image_uri";
    private static final String EXTRA_NAME = "object_name";
    private static final String EXTRA_TEAM_ID = "teamId";
    private static final String EXTRA_CURRENCY = "currency";
    private static final String EXTRA_LOCAION = "location";

    private static final String LOG_TAG = ReportClaimActivity.class.getSimpleName();

    private static final String DATE_PICKER_FRAGMENT_TAG = "date_picker";
    private static final String DATA_REQUEST_FRAGMENT_TAG = "data_request";
    private static final String PLEASE_WAIT_DIALOG_FRAGMENT_TAG = "please_wait";

    private Calendar mCalendar;
    private TextView mIncidentDateView;
    private EditText mExpensesView;
    private TextView mCoverageView;
    private TextView mDescriptionView;
    private TextView mAddressView;
    private TextView mClaimAmount;
    private PhotoAdapter mPhotoAdapter;
    private ImagePicker mImagePicker;


    private Disposable mDisposable;


    private float mLimitValue;
    private float mCoverageValue = -1f;
    private float mExpensesValue = -1f;
    private int mTeamId;
    private String mCurrency;
    private Snackbar mSnackBar;

    public static void start(Context context, String objectImageUri, String objectName, int teamId, String currency, String location) {
        context.startActivity(getLaunchIntent(context, objectImageUri, objectName, teamId, currency, location));
    }

    public static Intent getLaunchIntent(Context context, String objectImageUri, String objectName, int teamId, String currency, String location) {
        return new Intent(context, ReportClaimActivity.class).putExtra(EXTRA_IMAGE_URI, objectImageUri)
                .putExtra(EXTRA_NAME, objectName)
                .putExtra(EXTRA_TEAM_ID, teamId)
                .putExtra(EXTRA_CURRENCY, currency)
                .putExtra(EXTRA_LOCAION, location);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_report_claim);

        setTitle(R.string.report_claim);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getComponent().inject(this);


        mImagePicker = new ImagePicker(this);


        findViewById(R.id.add_photos).setOnClickListener(this::onClick);
        findViewById(R.id.incident_date).setOnClickListener(this::onClick);


        mIncidentDateView = findViewById(R.id.incident_date);
        mCoverageView = findViewById(R.id.coverage);
        mClaimAmount = findViewById(R.id.claim_amount);


        TextView locationView = findViewById(R.id.location);
        locationView.setText(getIntent().getStringExtra(EXTRA_LOCAION));


        RecyclerView mPhotos = findViewById(R.id.photos);
        mExpensesView = findViewById(R.id.expenses);
        mPhotoAdapter = new PhotoAdapter(this);
        getComponent().inject(mPhotoAdapter);
        mPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mPhotos.setAdapter(mPhotoAdapter);


        new ItemTouchHelper(new ItemTouchCallback()).attachToRecyclerView(mPhotos);

        final Intent intent = getIntent();
        mCurrency = intent.getStringExtra(EXTRA_CURRENCY);
        mTeamId = intent.getIntExtra(EXTRA_TEAM_ID, -1);
        ((TextView) findViewById(R.id.object_title)).setText(intent.getStringExtra(EXTRA_NAME));
        GlideApp.with(this).load(getImageLoader().getImageUrl(intent.getStringExtra(EXTRA_IMAGE_URI)))
                .apply(new RequestOptions().transforms(new CenterCrop()
                        , new RoundedCorners(getResources().getDimensionPixelOffset(R.dimen.rounded_corners_4dp)))).into((ImageView) findViewById(R.id.object_icon));

        mCalendar = Calendar.getInstance();

        FragmentManager fragmentManager = getSupportFragmentManager();

        if (fragmentManager.findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG) == null) {
            fragmentManager.beginTransaction().add(new TeambrellaRequestFragment(), DATA_REQUEST_FRAGMENT_TAG).commit();
        }

        AmountCurrencyUtil.setAmount(mClaimAmount, 0f, mCurrency);


        mExpensesView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (!TextUtils.isEmpty(charSequence)) {
                    mExpensesValue = Float.parseFloat(charSequence.toString());
                    if (mExpensesValue <= mLimitValue) {
                        if (mCoverageValue > 0) {
                            AmountCurrencyUtil.setAmount(mClaimAmount, mCoverageValue * mExpensesValue, mCurrency);
                        }
                    } else {
                        mExpensesView.setError(getString(R.string.big_expenses_error, AmountCurrencyUtil.getCurrencySign(mCurrency), mLimitValue));
                    }
                } else {
                    mExpensesValue = 0f;
                    AmountCurrencyUtil.setAmount(mClaimAmount, mCoverageValue * mExpensesValue, mCurrency);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        AmountCurrencyUtil.setAmount(findViewById(R.id.deductible), 0f, mCurrency);
        mDescriptionView = findViewById(R.id.description);
        mAddressView = findViewById(R.id.address);
    }

    /**
     * Show Date Picker
     */
    private void showDatePicker(int year, int month, int day) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(DATE_PICKER_FRAGMENT_TAG) == null) {
            TeambrellaDatePickerDialog.getInstance(year, month, day).show(fragmentManager, DATE_PICKER_FRAGMENT_TAG);
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            mDisposable = fragment.getObservable().subscribe(this::onRequestResult);
            fragment.start();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.stop();
        }
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }

        mDisposable = null;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.submit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.submit:
                item.setEnabled(!submitClaim());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_photos:
                mImagePicker.startPicking();
                break;
            case R.id.incident_date:
                showDatePicker(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
                break;
        }
    }


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    case TeambrellaUris.GET_COVERAGE_FOR_DATE:
                        Observable.just(response.getValue()).map(JsonWrapper::new)
                                .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                                .doOnNext(jsonWrapper -> mLimitValue = jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_LIMIT_ANOUNT))
                                .doOnNext(jsonWrapper -> mCoverageValue = jsonWrapper.getFloat(TeambrellaModel.ATTR_DATA_COVERAGE))
                                .doOnNext(jsonWrapper -> mExpensesView.setHint(Integer.toString(Math.round(mLimitValue))))
                                .doOnNext(jsonWrapper -> mCoverageView.setText(Html.fromHtml(getString(R.string.coverage_format_string, Math.round(mCoverageValue * 100)))))
                                .blockingFirst(new JsonWrapper(null));
                        break;
                    case TeambrellaUris.NEW_CLAIM:
                        int claimId = Observable.just(response.getValue()).map(JsonWrapper::new)
                                .map(jsonWrapper -> jsonWrapper.getObject(TeambrellaModel.ATTR_DATA))
                                .map(jsonWrapper -> jsonWrapper.getInt(TeambrellaModel.ATTR_DATA_ID)).blockingFirst();
                        startActivity(ClaimActivity.Companion.getLaunchIntent(this, claimId, getIntent().getStringExtra(EXTRA_NAME), mTeamId));
                        new TeambrellaBroadcastManager(this).notifyClaimSubmitted();
                        finish();
                        break;
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
                case TeambrellaUris.GET_COVERAGE_FOR_DATE:
                    mIncidentDateView.setText(null);
                    break;
                case TeambrellaUris.NEW_CLAIM:
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().remove(fragmentManager.findFragmentByTag(PLEASE_WAIT_DIALOG_FRAGMENT_TAG)).commit();
                    break;
            }

            showSnackBar(ConnectivityUtils.isNetworkAvailable(this)
                    ? R.string.something_went_wrong_error : R.string.no_internet_connection);

        }
        invalidateOptionsMenu();
    }


    public void request(Uri uri) {
        TeambrellaRequestFragment fragment = (TeambrellaRequestFragment) getSupportFragmentManager().findFragmentByTag(DATA_REQUEST_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.request(uri);
        }
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        mIncidentDateView.setText(TeambrellaDateUtils.getDatePresentation(this, TeambrellaDateUtils.TEAMBRELLA_UI_DATE, mCalendar.getTime().getTime()));
        mIncidentDateView.setError(null);
        request(TeambrellaUris.getCoverageForDate(mTeamId, mCalendar.getTime()));
    }


    private boolean submitClaim() {


        hideKeyboard();

        if (mCoverageValue <= 0) {
            mCoverageView.requestFocus();
            mCoverageView.setError(getString(R.string.no_coverage_for_the_date));
            return false;
        }

        if (TextUtils.isEmpty(mIncidentDateView.getText())) {
            mIncidentDateView.setFocusable(true);
            mIncidentDateView.setFocusableInTouchMode(true);
            mIncidentDateView.requestFocus();
            mIncidentDateView.setError(getString(R.string.no_incident_date_error));
            return false;
        }

        if (mExpensesValue <= 0) {
            mExpensesView.setError(getString(R.string.no_expenses_provided_error));
            return false;
        }

        if (mExpensesValue > mLimitValue) {
            return false;
        }

        if (TextUtils.isEmpty(mDescriptionView.getText())) {
            mDescriptionView.setError(getString(R.string.no_description_provided_error));
            return false;
        }

        if (TextUtils.isEmpty(mAddressView.getText())) {
            mAddressView.setError(getString(R.string.no_address_provided));
            return false;
        }

        if (!checkEthereumAddress(mAddressView.getText().toString())) {
            mAddressView.setError(getString(R.string.invalid_ethereum_address_error));
            return false;
        }


        request(TeambrellaUris.getNewClaimUri(mTeamId, mCalendar.getTime(),
                mExpensesValue, mDescriptionView.getText().toString(),
                mPhotoAdapter.getImages(),
                mAddressView.getText().toString()));


        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(PLEASE_WAIT_DIALOG_FRAGMENT_TAG) == null) {
            new ProgressDialogFragment().show(getSupportFragmentManager(), PLEASE_WAIT_DIALOG_FRAGMENT_TAG);
        }

        return true;
    }

    private boolean checkEthereumAddress(String address) {
        return Pattern.matches("^0x[a-fA-F0-9]{40}$", address);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showSnackBar(@StringRes int text) {
        if (mSnackBar == null) {
            mSnackBar = Snackbar.make(mAddressView, text, Snackbar.LENGTH_LONG);

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
