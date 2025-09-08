package com.varnika_jain.pokedex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PokemonRepository,
) : ViewModel() {
    private val _pokemonState = MutableStateFlow<Result<ArrayList<Pokemon>>>(Result.Loading)
    val pokemonState: StateFlow<Result<ArrayList<Pokemon>>> = _pokemonState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun fetchPokemonList() {
        viewModelScope.launch {
            repository.getPokemonList().collect { result ->
                _pokemonState.value = result
            }
        }
    }

    val filteredPokemon: StateFlow<List<Pokemon>> =
        pokemonState
            .combine(_searchQuery) { result, query ->
                val list = (result as? Result.Success)?.data ?: emptyList()
                if (query.isBlank()) {
                    list
                } else {
                    list.filter { it.name.contains(query, ignoreCase = true) }
                }
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
