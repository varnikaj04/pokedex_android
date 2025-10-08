package com.varnika_jain.pokedex.utils

import com.varnika_jain.pokedex.data.remote.NetworkLogger
import com.varnika_jain.pokedex.data.remote.Resource
import retrofit2.Response

suspend inline fun <T> safeApiCall(
    endpoint: String,
    crossinline apiCall: suspend () -> Response<T>
): Resource<T> {
    return try {
        NetworkLogger.logLoading(endpoint)
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                NetworkLogger.logSuccess(endpoint)
                Resource.Success(it)
            } ?: run {
                NetworkLogger.logError(endpoint, "Empty body", response.code())
                Resource.Error("Empty response body")
            }
        } else {
            val errorMsg = response.errorBody()?.string() ?: "Unknown error"
            NetworkLogger.logError(endpoint, errorMsg, response.code())
            Resource.Error(errorMsg)
        }
    } catch (e: Exception) {
        NetworkLogger.logException(endpoint, e)
        Resource.Error(e.localizedMessage ?: "Unexpected error")
    }
}