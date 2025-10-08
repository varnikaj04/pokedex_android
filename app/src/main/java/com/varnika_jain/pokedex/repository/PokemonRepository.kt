package com.varnika_jain.pokedex.repository

import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.data.remote.PokemonResponse
import com.varnika_jain.pokedex.data.remote.PokemonService
import com.varnika_jain.pokedex.data.remote.Resource
import com.varnika_jain.pokedex.utils.safeApiCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class PokemonRepository(
    private val pokemonService: PokemonService,
) {
    fun getPokemonList(limit: Int, offset: Int): Flow<Resource<PokemonResponse>> =
        flow {
            emit(Resource.Loading)
            emit(safeApiCall("getPokemonList") { pokemonService.getPokemonList(limit, offset) })
        }.flowOn(Dispatchers.IO)

    fun getPokemonDetails(id: Int): Flow<Resource<PokemonDetails>> =
        flow {
            emit(Resource.Loading)
            emit(safeApiCall("getPokemonDetails") { pokemonService.getPokemonDetails(id) })
        }.flowOn(Dispatchers.IO)
}
