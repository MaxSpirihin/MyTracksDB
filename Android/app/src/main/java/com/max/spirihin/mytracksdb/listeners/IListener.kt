package com.max.spirihin.mytracksdb.listeners

import android.content.Context

interface IListener {
    fun startListen(context: Context)
    fun stopListen()
}