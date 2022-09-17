package com.arstagaev.flowble.gentelman_kit

import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlin.reflect.KClass

fun logAction(msg: String) = Log.i("~~action",msg ?:"empty")

fun logWarning(msg: String) = Log.w("~~warning",msg ?:"empty")

fun logError(msg: String) = Log.w("~~ERROR",msg ?:"empty")


//fun Class<*>.transformName() {
//    val clazz: Class<*> = this
//    clazz.name.hashCode()
//}

