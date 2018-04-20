package com.teambrella.android.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.teambrella.android.R;
import com.teambrella.android.ui.widget.AkkuratBoldTypefaceSpan;
import com.teambrella.android.util.QRCodeUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * QR Code Activity
 */
public class QRCodeActivity extends AppCompatActivity {


    public static final String EXTRA_ADDRESS = "extra_address";


    public static void startQRCode(Context context, String address) {
        context.startActivity(new Intent(context, QRCodeActivity.class)
                .putExtra(EXTRA_ADDRESS, address));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(VectorDrawableCompat.create(getResources(), R.drawable.ic_arrow_back, null));
        }

        setTitle(R.string.qr_code);

        final String address = getIntent().getStringExtra(EXTRA_ADDRESS);

        Observable.just(address).map(s -> QRCodeUtils.createBitmap(s, getResources().getColor(R.color.dark)
                , getResources().getColor(R.color.activity_background)))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    final ImageView qrCodeImageView = findViewById(R.id.qr_code_image);
                    qrCodeImageView.setImageBitmap(bitmap);
                }, throwable -> {
                });

        ((TextView) findViewById(R.id.address)).setText(address);

        findViewById(R.id.copy_address).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText(getString(R.string.ethereum_address), address);
                clipboard.setPrimaryClip(clip);
            }
        });

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
    public void setTitle(CharSequence title) {
        SpannableString s = new SpannableString(title);
        s.setSpan(new AkkuratBoldTypefaceSpan(this), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        super.setTitle(s);
    }

}
