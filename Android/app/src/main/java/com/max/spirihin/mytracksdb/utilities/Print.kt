package com.max.spirihin.mytracksdb.utilities

object Print {

    private const val LOG_TAG = "MyTracksDB"

    fun Log(text: String) {
        android.util.Log.d(LOG_TAG, text)
    }

    fun LogWarning(text: String) {
        android.util.Log.w(LOG_TAG, text)
    }

    fun LogError(text: String) {
        android.util.Log.e(LOG_TAG, text)
    }
}