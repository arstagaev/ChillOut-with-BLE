package com.arstagaev.chilloutble.utils

import android.content.Context
import android.util.Log
import android.widget.Toast

fun toastShow(msg : String, ctx : Context){
    try {
        Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("TOAST ERROR","${e.message}")
    }
}