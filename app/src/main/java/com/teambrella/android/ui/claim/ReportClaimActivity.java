package com.teambrella.android.ui.claim;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.teambrella.android.R;
import com.teambrella.android.ui.dialog.TeambrellaDatePickerDialog;
import com.teambrella.android.util.ImagePicker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
        findViewById(R.id.date).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.upload_file).setOnClickListener(v -> startActivityForResult(ImagePicker.getImagePickerIntent(this), 4));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(LOG_TAG, data.getData().toString());
        Observable.just(data.getData())
                .map(this::createFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(file -> Log.e(LOG_TAG, file));
    }

    private String createFile(Uri uri) throws IOException {
        String mimeType = getContentResolver().getType(uri);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        String name = null;
        Cursor cursor =
                getContentResolver().query(uri, null, null, null, null);

        if (cursor != null
                && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }


        if (name != null) {
            
        }


        if (cursor != null) {
            cursor.close();
        }
        return extension;
    }


    private void copy(File source, File destination) throws IOException {

        FileChannel in = new FileInputStream(source).getChannel();
        FileChannel out = new FileOutputStream(destination).getChannel();

        try {
            in.transferTo(0, in.size(), out);
        } finally {
            if (in != null)
                in.close();
            out.close();
        }
    }
}
