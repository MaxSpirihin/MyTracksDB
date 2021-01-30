package com.max.spirihin.mytracksdb.utilities

import android.view.View
import android.widget.LinearLayout

object UIUtils {

    fun addToLinearLayout(
            linearLayout: LinearLayout,
            view: View,
            marginLeft: Int,
            marginTop: Int,
            marginRight: Int,
            marginBottom: Int) {
        val lp = LinearLayout.LayoutParams(
                if (linearLayout.orientation == LinearLayout.HORIZONTAL) 0 else LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        )
        lp.setMargins(marginLeft, marginTop, marginRight, marginBottom)
        view.layoutParams = lp
        linearLayout.addView(view)
    }
}