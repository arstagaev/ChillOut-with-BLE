package com.arstagaev.chilloutble.ble.control_module

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast

// need f
class LogCustom {

    companion object {
        fun d(msg: String) {
            println("D: ${msg}")
        }
        fun w(msg: String) {
            println("W: ${msg}")
        }
        fun e(msg: String) {
            println("ERROR: ${msg}")
        }
        fun i(msg: String) {
            println("I: ${msg}")
        }
    }


}



fun log(msg : String){
    Log.i("for unit test:","$msg")

}

fun toastShow(msg: String, ctx : Context){
    try{
        Toast.makeText(ctx,msg, Toast.LENGTH_SHORT).show()
    }catch (exc :Exception) {
        Log.e("eee","eee fun toastShow( ${exc.message}")

    }

}





enum class ServiceState {
    STARTED,
    STOPPED,
}

private const val name = "RECORD_SERVICE_KEY"
private const val key =  "RECORD_SERVICE_STATE"

fun setServiceState(context: Context, state: ServiceState) {
    val sharedPrefs = getPreferences(context)
    sharedPrefs.edit().let {
        it.putString(key, state.name)
        it.apply()
    }
}

fun getServiceState(context: Context): ServiceState {
    val sharedPrefs = getPreferences(context)
    val value = sharedPrefs.getString(key, ServiceState.STOPPED.name)
    return ServiceState.valueOf(value!!)
}

private fun getPreferences(context: Context): SharedPreferences {

    return context.getSharedPreferences(name, 0)
}


class Log {
    val ASSERT = 7
    val DEBUG = 3
    val ERROR = 6
    val INFO = 4
    val VERBOSE = 2
    val WARN = 5


    fun v(tag: String?, msg: String): Int {
        throw RuntimeException("Stub!")
    }

    fun v(tag: String?, msg: String?, tr: Throwable?): Int {
        throw RuntimeException("Stub!")
    }

    fun d(tag: String?, msg: String): Int {
        throw RuntimeException("Stub!")
    }

    fun d(tag: String?, msg: String?, tr: Throwable?): Int {
        throw RuntimeException("Stub!")
    }

    fun i(tag: String?, msg: String): Int {
        throw RuntimeException("Stub!")
    }

    fun i(tag: String?, msg: String?, tr: Throwable?): Int {
        throw RuntimeException("Stub!")
    }

    fun w(tag: String?, msg: String): Int {
        throw RuntimeException("Stub!")
    }

    fun w(tag: String?, msg: String?, tr: Throwable?): Int {
        throw RuntimeException("Stub!")
    }

    external fun isLoggable(var0: String?, var1: Int): Boolean
}
