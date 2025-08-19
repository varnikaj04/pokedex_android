package com.varnika_jain.pokedex.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _pokemonState = MutableStateFlow<Result<PokemonDetails>>(Result.Loading)

    val pokemonState: StateFlow<Result<PokemonDetails>> = _pokemonState

    fun fetchPokemonDetails(id: Int) {
        viewModelScope.launch {
            repository.getPokemonDetails(id)
                .collect { result ->
                    _pokemonState.value = result
                }
        }
    }
}