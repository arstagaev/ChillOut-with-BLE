package com.arstagaev.flowble.gentelman_kit

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlin.reflect.KClass

fun logInfo(msg: String) = Log.i("~~info",msg ?:"empty")

fun logAction(msg: String) = Log.i("~~action",msg ?:"empty")

fun logWarning(msg: String) = Log.w("~~warning",msg ?:"empty")

fun logError(msg: String) = Log.w("~~ERROR",msg ?:"empty")
