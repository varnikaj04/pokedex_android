package com.varnika_jain.pokedex.repository

import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.data.remote.PokemonService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.varnika_jain.pokedex.data.remote.Result


class PokemonRepository(
    private val pokemonService: PokemonService
) {

    suspend fun getPokemonList(limit: Int): Flow<Result<List<Pokemon>>> = flow{
        try {
            emit(Result.Loading)
            val response = pokemonService.getPokemonList(limit)
            emit(Result.Success(response.results))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Unknown error"))
        }
    }

    suspend fun getPokemonDetails(id: Int): Flow<Result<PokemonDetails>> = flow {
        try {
            emit(Result.Loading)
            val response = pokemonService.getPokemonDetails(id)
            emit(Result.Success(response))
        } catch (e: Exception) {
            emit(Result.Error(e.localizedMessage ?: "Unknown error"))
        }
    }
}