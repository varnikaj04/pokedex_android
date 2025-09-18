package com.varnika_jain.pokedex.repository

import com.varnika_jain.pokedex.data.remote.ApiResponse
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.data.remote.PokemonResponse
import com.varnika_jain.pokedex.data.remote.PokemonService
import com.varnika_jain.pokedex.data.remote.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PokemonRepository(
    private val pokemonService: PokemonService,
) {
    fun getPokemonList(
        limit: Int,
        offset: Int,
    ): Flow<ApiResponse<PokemonResponse>> =
        flow {
            emit(ApiResponse.Loading())

            try {
                val response = pokemonService.getPokemonList(limit = limit, offset = offset)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        emit(ApiResponse.Success(body))
                    } else {
                        emit(ApiResponse.Error(message = "Empty response body"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    emit(ApiResponse.Error(message = errorMsg))
                }
            } catch (e: Exception) {
                emit(ApiResponse.Error(message = e.localizedMessage ?: "Unexpected error"))
            }
        }.flowOn(Dispatchers.IO)

    fun getPokemonDetails(id: Int): Flow<Result<PokemonDetails>> =
        flow {
            try {
                emit(Result.Loading)
                val response = pokemonService.getPokemonDetails(id)
                emit(Result.Success(response))
            } catch (e: Exception) {
                emit(Result.Error(e.localizedMessage ?: "Unknown error"))
            }
        }
}
