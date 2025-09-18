package com.varnika_jain.pokedex.data.remote

import com.varnika_jain.pokedex.utils.buildImageUrl

data class Pokemon(
    val name: String,
    val url: String,
) {
    val id: Int
        get() = url.trimEnd('/').substringAfterLast('/').toInt()
    val imageUrl: String
        get() = id.buildImageUrl()
}

data class PokemonResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: ArrayList<Pokemon>,
)
