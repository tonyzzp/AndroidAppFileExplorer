package me.izzp.androidappfileexplorer

import android.content.Context
import android.view.View
import android.widget.Toast

/**
 * Created by zzp on 2017-08-02.
 */

internal fun View?.show() {
    this?.visibility = View.VISIBLE
}

internal fun View?.hide() {
    this?.visibility = View.INVISIBLE
}

internal fun View?.gone() {
    this?.visibility = View.GONE
}

internal fun Context.toastShort(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}