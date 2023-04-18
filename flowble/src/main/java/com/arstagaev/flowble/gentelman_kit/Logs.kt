package com.arstagaev.flowble.gentelman_kit

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlin.reflect.KClass

fun logInfo(msg: String) = Log.i("~~info",msg ?:"empty")

fun logAction(msg: String) = Log.i("~~action",msg ?:"empty")

fun logWarning(msg: String) = Log.w("~~warning",msg ?:"empty")

fun logError(msg: String) = Log.e("~~ERROR",msg ?:"empty")



// visible logs:

fun Context.toast(msg: String) = try {Toast.makeText(this,msg ?: "",Toast.LENGTH_SHORT).show() } catch (e: Exception) { logError(" error in Toast") }

