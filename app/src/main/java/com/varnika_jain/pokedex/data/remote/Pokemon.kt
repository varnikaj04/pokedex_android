package com.varnika_jain.pokedex.data.remote

data class Pokemon(
    val name: String,
    val url: String
){
    val id: Int
        get() = url.trimEnd('/').substringAfterLast('/').toInt()
}

data class PokemonResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: ArrayList<Pokemon>
)

