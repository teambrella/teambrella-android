package com.teambrella.android.ui.util

import android.os.Build
import android.support.annotation.DimenRes
import android.support.annotation.DrawableRes
import android.text.format.DateUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.teambrella.android.R
import com.teambrella.android.image.glide.GlideApp
import com.teambrella.android.util.TeambrellaDateUtils
import java.text.ParseException


fun ImageView.setAvatar(url: GlideUrl?) {
    url?.let {
        GlideApp.with(this).load(it)
                .apply(RequestOptions().circleCrop().placeholder(R.drawable.picture_background_circle))
                .into(this)
    }
}

fun ImageView.setImage(url: GlideUrl?, @DimenRes radius: Int) {
    url?.let {
        GlideApp.with(this).load(it)
                .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(this.context.resources.getDimensionPixelOffset(radius))))
                .into(this)
    }
}

fun ImageView.setImage(url: GlideUrl?, @DimenRes radius: Int, @DrawableRes placeholder: Int) {
    url?.let {
        GlideApp.with(this).load(it)
                .apply(RequestOptions().transforms(CenterCrop(), RoundedCorners(this.context.resources.getDimensionPixelOffset(radius))))
                .placeholder(placeholder)
                .into(this)
    }
}

fun TextView.setRightCompoundDrawable(@DrawableRes drawableRes: Int) {
    val drawable = if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        this.resources.getDrawable(drawableRes, null) else this.resources.getDrawable(drawableRes)
    this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
}

fun TextView.setUnreadCount(count: Int) {
    this.text = count.toString()
    this.visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
}

fun TextView.setAVGDifference(value: Float, avg: Float) {
    val percent = Math.round((value - avg) / avg * 100)
    when {
        percent > 0 -> this.text = this.context.resources.getString(R.string.vote_avg_difference_bigger_format_string, percent)
        percent < 0 -> this.text = this.context.resources.getString(R.string.vote_avg_difference_smaller_format_string, percent)
        else -> this.setText(R.string.vote_avg_difference_same)
    }
}

fun TextView.setRelativeTimeSpan(serverTime: String) {
    try {
        val time = TeambrellaDateUtils.getServerTime(serverTime)
        val now = System.currentTimeMillis()
        this.text = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE)
    } catch (e: ParseException) {
        e.printStackTrace()
    }
}