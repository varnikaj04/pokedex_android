package com.varnika_jain.pokedex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varnika_jain.pokedex.data.remote.ApiResponse
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.data.remote.PokemonResponse
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
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _pokemonFlow = MutableStateFlow<ApiResponse<PokemonResponse>>(ApiResponse.Loading())
    val pokemonFlow: StateFlow<ApiResponse<PokemonResponse>> = _pokemonFlow

    private var offset = 0
    private val limit = 20
    private var nextUrl: String? = null
    private var isLoading = false

    private val _pokemonList = mutableListOf<Pokemon>()
    val pokemonList: List<Pokemon> get() = _pokemonList

    fun getPokemonList(initial: Boolean = false) {
        if (searchQuery.value.isNotBlank()) return
        if (isLoading) return

        if (initial && _pokemonList.isNotEmpty()) {
            _pokemonFlow.value =
                ApiResponse.Success(
                    PokemonResponse(
                        count = _pokemonList.size,
                        next = nextUrl,
                        previous = null,
                        results = ArrayList(_pokemonList),
                    ),
                )
            return
        }

        isLoading = true
        if (initial) offset = 0

        viewModelScope.launch {
            repository.getPokemonList(limit, offset).collect { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        val newItems = response.data?.results ?: emptyList()
                        if (initial) _pokemonList.clear()
                        _pokemonList.addAll(newItems)

                        nextUrl = response.data?.next
                        offset += limit
                        isLoading = false

                        _pokemonFlow.value =
                            ApiResponse.Success(
                                PokemonResponse(
                                    count = _pokemonList.size,
                                    next = nextUrl,
                                    previous = null,
                                    results = ArrayList(_pokemonList),
                                ),
                            )
                    }

                    is ApiResponse.Error -> {
                        isLoading = false
                        _pokemonFlow.value = response
                    }

                    is ApiResponse.Loading -> {
                        _pokemonFlow.value = response
                    }
                }
            }
        }
    }

    fun hasNextPage(): Boolean = searchQuery.value.isBlank() && nextUrl != null

    val filteredPokemon: StateFlow<List<Pokemon>> =
        combine(_searchQuery, _pokemonFlow) { query, response ->
            val list = (response as? ApiResponse.Success)?.data?.results ?: emptyList()
            if (query.isBlank()) {
                list
            } else {
                _pokemonList.filter { it.name.startsWith(query, ignoreCase = true) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
