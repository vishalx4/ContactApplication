package com.example.mangoapps.models

import android.graphics.Bitmap
import com.example.mangoapps.helper.CallLogType

data class CallLogs(
    val name: String,
    val number: String,
    val callType: CallLogType,
    var image: Bitmap?,
    val date: Long,
)
