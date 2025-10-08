package com.varnika_jain.pokedex.data.remote

import android.util.Log

object NetworkLogger {
    private const val TAG = "NetworkResult"

    fun logLoading(endpoint: String) {
        Log.i(TAG, "⏳ LOADING [$endpoint]")
    }

    fun logSuccess(endpoint: String, message: String? = null) {
        Log.d(TAG, "✅ SUCCESS [$endpoint] ${message ?: ""}")
    }

    fun logError(endpoint: String, error: String, code: Int? = null) {
        Log.e(TAG, "❌ ERROR [$endpoint] Code: ${code ?: "N/A"}, Message: $error")
    }

    fun logException(endpoint: String, exception: Throwable) {
        Log.e(TAG, "💥 EXCEPTION [$endpoint]: ${exception.localizedMessage}", exception)
    }
}