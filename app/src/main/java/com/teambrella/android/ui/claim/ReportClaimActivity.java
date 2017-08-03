package com.teambrella.android.ui.claim;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambrella.android.R;
import com.teambrella.android.image.TeambrellaImageLoader;
import com.teambrella.android.ui.dialog.TeambrellaDatePickerDialog;
import com.teambrella.android.ui.photos.PhotoAdapter;
import com.teambrella.android.util.ImagePicker;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import io.reactivex.Observable;
import jp.wasabeef.picasso.transformations.MaskTransformation;


/**
 * Activity to report a claim
 */
public class ReportClaimActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String EXTRA_IMAGE_URI = "image_uri";
    private static final String EXTRA_NAME = "object_name";

    private static final String LOG_TAG = ReportClaimActivity.class.getSimpleName();

    public static final String DATE_PICKER_FRAGMENT_TAG = "date_picker";


    private static SimpleDateFormat mDateFormat = new SimpleDateFormat("d LLLL yyyy", new Locale("es", "ES"));


    private Calendar mCalendar;
    private TextView mIncidentDate;
    private RecyclerView mPhotos;
    private PhotoAdapter mPhotoAdapter;
    private ImagePicker mImagePicker;


    public static void start(Context context, String objectImageUri, String objectName) {
        context.startActivity(new Intent(context, ReportClaimActivity.class).putExtra(EXTRA_IMAGE_URI, objectImageUri)
                .putExtra(EXTRA_NAME, objectName));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_claim);

        setTitle(R.string.report_claim);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        mImagePicker = new ImagePicker(this);

        findViewById(R.id.add_photos).setOnClickListener(this::onClick);
        findViewById(R.id.incident_date).setOnClickListener(this::onClick);

        mIncidentDate = findViewById(R.id.incident_date);
        mPhotos = findViewById(R.id.photos);
        mPhotoAdapter = new PhotoAdapter(this);
        mPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mPhotos.setAdapter(mPhotoAdapter);


        final Intent intent = getIntent();
        ((TextView) findViewById(R.id.object_title)).setText(intent.getStringExtra(EXTRA_NAME));
        TeambrellaImageLoader.getInstance(this).getPicasso().load(intent.getStringExtra(EXTRA_IMAGE_URI))
                .resize(getResources().getDimensionPixelSize(R.dimen.image_size_40), getResources().getDimensionPixelSize(R.dimen.image_size_40)).centerCrop()
                .transform(new MaskTransformation(this, R.drawable.teammate_object_mask)).into((ImageView) findViewById(R.id.object_icon));

        mCalendar = Calendar.getInstance();

        mIncidentDate.setText(mDateFormat.format(new Date()));

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
        Observable<File> observable = mImagePicker.onActivityResult(requestCode, resultCode, data);
        if (observable != null) {
            observable.subscribe(file -> mPhotoAdapter.addPhoto(file.getAbsolutePath())
                    , throwable -> Log.e(LOG_TAG, throwable.getMessage()));
        }
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        mIncidentDate.setText(mDateFormat.format(mCalendar.getTime()));
    }
}
