package com.teambrella.android.util;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import com.teambrella.android.util.log.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Image Picker
 */
public class ImagePicker {

    private static final String LOG_TAG = ImagePicker.class.getSimpleName();


    private static final int IMAGE_PICKER_REQUEST_CODE = 101;
    private static final String EXTENSION = ".jpg";
    private static final SimpleDateFormat CAMERA_FILE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);


    private final AppCompatActivity mContext;
    private Uri mCameraFileUri;


    public ImagePicker(AppCompatActivity context) {
        mContext = context;
    }


    public void startPicking() {
        mCameraFileUri = getTempFileUri();
        mContext.startActivityForResult(getImagePickerIntent(mCameraFileUri), IMAGE_PICKER_REQUEST_CODE);
    }


    public Observable<File> onActivityResult(int requestCode, int resultCode, Intent data) {
        Observable<File> result = null;
        switch (requestCode) {
            case IMAGE_PICKER_REQUEST_CODE:
                File cameraFile = mCameraFileUri != null ? new File(mCameraFileUri.getPath()) : null;
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    Uri uri = data != null ? data.getData() : null;
                    result = cameraFile != null && uri == null ?
                            Observable.just(cameraFile)
                            : uri != null ? Observable.just(uri).map(this::createFile)
                            : null;

                    if (uri != null) {
                        if (cameraFile != null) {
                            //noinspection ResultOfMethodCallIgnored
                            cameraFile.delete();
                        }
                    }

                    if (result != null) {
                        result = result.map(this::scaleImageIfNeeded);
                    }

                    if (result != null) {
                        result = result.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread());
                    }
                } else {
                    if (cameraFile != null) {
                        //noinspection ResultOfMethodCallIgnored
                        cameraFile.delete();
                    }
                }
                break;
        }

        return result;
    }


    /**
     * Get image picker intent
     */
    private Intent getImagePickerIntent(Uri cameraFile) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        // Gallery intent
        Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        addIntentToList(intentList, pickIntent);

        // Camera intent
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFile);
        addIntentToList(intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1), "Choose");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private void addIntentToList(List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;

            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
    }


    /**
     * Get image file
     */
    private Uri getTempFileUri() {

        // Create an image file name
        String timeStamp = CAMERA_FILE_FORMAT.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, EXTENSION, storageDir);
            return Uri.fromFile(image);
        } catch (IOException ex) {
            Log.e(LOG_TAG, ex.toString());
            return null;
        }
    }


    private File createFile(Uri uri) throws IOException {

        ContentResolver contentResolver = mContext.getContentResolver();
        String mimeType = contentResolver.getType(uri);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        String name = null;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null
                && cursor.moveToFirst()) {
            name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }


        File file = null;

        if (name != null && extension != null) {
            file = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES), name);
            copy(contentResolver.openInputStream(uri), file);
        }

        if (cursor != null) {
            cursor.close();
        }
        return file;
    }


    private File scaleImageIfNeeded(File srcFile) throws IOException {

        int maxDimen = 1200;

        Bitmap bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
        float scaleFactor = ((float) Math.max(bitmap.getHeight(), bitmap.getWidth())) / maxDimen;
        Bitmap newBitmap;
        if (scaleFactor > 1.0f) {
            scaleFactor = 1 / scaleFactor;
            newBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scaleFactor), (int) (bitmap.getHeight() * scaleFactor), true);
            bitmap.recycle();
        } else {
            newBitmap = bitmap;
        }
        File newFile = compress(newBitmap);
        //noinspection ResultOfMethodCallIgnored
        srcFile.delete();
        return newFile;
    }

    private File compress(Bitmap bitmap) throws IOException {
        String timeStamp = CAMERA_FILE_FORMAT.format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_out";
        File storageDir = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File outFile = new File(storageDir, imageFileName + ".jpg");
        OutputStream outStream = null;
        try {
            outStream = new FileOutputStream(new File(storageDir, imageFileName + ".jpg"));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outStream != null) {
                outStream.flush();
                outStream.close();
            }
        }

        return outFile;
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
