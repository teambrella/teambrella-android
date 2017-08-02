package com.teambrella.android.ui.claim;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;

import com.teambrella.android.R;
import com.teambrella.android.ui.dialog.TeambrellaDatePickerDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * Activity to report a claim
 */
public class ReportClaimActivity extends AppCompatActivity {

    private static final String LOG_TAG = ReportClaimActivity.class.getSimpleName();

    public static final String DATE_PICKER_FRAGMENT_TAG = "date_picker";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_claim);
//        findViewById(R.id.date).setOnClickListener(v -> showDatePicker());
//        findViewById(R.id.upload_file).setOnClickListener(v -> startActivityForResult(ImagePicker.getImagePickerIntent(this), 4));
        setTitle(R.string.report_claim);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(LOG_TAG, data.getAction() != null ? data.getAction() : "null");
        Uri uri = data.getData();
        if (uri != null) {
            Observable.just(data.getData())
                    .map(this::createFile)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(file -> Log.e(LOG_TAG, file.getAbsolutePath()));
        }
    }

    private File createFile(Uri uri) throws IOException {

        String mimeType = getContentResolver().getType(uri);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        String name = null;

        Cursor cursor =
                getContentResolver().query(uri, null, null, null, null);

        if (cursor != null
                && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }


        File file = null;

        if (name != null && extension != null) {
            file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), name);
            copy(getContentResolver().openInputStream(uri), file);
        }

        if (cursor != null) {
            cursor.close();
        }
        return file;
    }

    private void copy(InputStream in, File file) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }

        } finally {
            if (out != null) {
                out.close();
            }
            in.close();
        }

    }
}
