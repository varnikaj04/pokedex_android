package com.varnika_jain.pokedex.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonService {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): Response<PokemonResponse>

    @GET("pokemon/{id}")
    suspend fun getPokemonDetails(
        @Path("id") pokemonId: Int,
    ): Response<PokemonDetails>
}
