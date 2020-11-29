package com.max.spirihin.mytracksdb.core

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

data class TrackPoint(val time: Date, val latitude: Double, val longitude: Double, val accuracy: Double, val steps: Int, val heartrate: Int)