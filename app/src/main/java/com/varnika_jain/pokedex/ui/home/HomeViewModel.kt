package com.varnika_jain.pokedex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    var lastSearchQuery: String = ""
    private val _pokemonState = MutableStateFlow<Result<ArrayList<Pokemon>>>(Result.Loading)
    val pokemonState: StateFlow<Result<ArrayList<Pokemon>>> = _pokemonState

    fun fetchPokemonList(limit: Int) {
        viewModelScope.launch {
            repository.getPokemonList(limit).collect { result ->
                _pokemonState.value = result
            }
        }
    }

}