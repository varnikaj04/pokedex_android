package com.varnika_jain.pokedex.data.remote

data class PokemonDetails(
    val base_experience: Int?,
    val height: Int?,
    val id: Int?,
    val is_default: Boolean?,
    val name: String?,
    val order: Int?,
    val stats: List<Stats?>?,
    val types: List<Types?>?,
    val weight: Int?
) {
    data class Stats(
        val base_stat: Int?,
        val effort: Int?,
        val stat: Stat?
    ) {
        data class Stat(
            val name: String?,
            val url: String?
        )
    }

    data class Types(
        val slot: Int?,
        val type: Type?
    ) {
        data class Type(
            val name: String?,
            val url: String?
        )
    }
}