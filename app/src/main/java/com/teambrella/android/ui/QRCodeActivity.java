package com.teambrella.android.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


    public static final String EXTRA_DATA = "extra_data";
    public static final String EXTRA_TYPE = "extra_type";
    public static final int QRTYPE_ADDRESS = 0;
    public static final int QRTYPE_KEY = 1;

    public static void startQRCode(Context context, String data, int qrType) {
        context.startActivity(new Intent(context, QRCodeActivity.class)
                .putExtra(EXTRA_DATA, data)
                .putExtra(EXTRA_TYPE, qrType));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_vector);
        }

        final String data = getIntent().getStringExtra(EXTRA_DATA);
        final int qrType = getIntent().getIntExtra(EXTRA_TYPE, QRTYPE_ADDRESS);

        if (qrType == QRTYPE_ADDRESS) {
            setTitle(R.string.qr_code_address_title);
            ((TextView) findViewById(R.id.data_title)).setText(R.string.qr_code_address_title);
        }
        if (qrType == QRTYPE_KEY) {
            setTitle(R.string.qr_code_key_title);
            ((TextView) findViewById(R.id.data_title)).setText(R.string.qr_code_key_title);
        }


        Observable.just(data).map(s -> QRCodeUtils.createBitmap(s, getResources().getColor(R.color.dark)
                , getResources().getColor(R.color.activity_background)))
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> {
                    final ImageView qrCodeImageView = findViewById(R.id.qr_code_image);
                    qrCodeImageView.setImageBitmap(bitmap);
                }, throwable -> {
                });

        ((TextView) findViewById(R.id.address)).setText(data);

        findViewById(R.id.copy_data).setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText(getString(R.string.ethereum_address), data);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show();
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
