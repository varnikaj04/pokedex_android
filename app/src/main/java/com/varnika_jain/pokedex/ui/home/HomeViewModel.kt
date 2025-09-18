package com.varnika_jain.pokedex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varnika_jain.pokedex.data.remote.ApiResponse
import com.varnika_jain.pokedex.data.remote.PokemonResponse
import com.varnika_jain.pokedex.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: PokemonRepository,
) : ViewModel() {
    /*private val _pokemonState = MutableStateFlow<Result<ArrayList<Pokemon>>>(Result.Loading)
    val pokemonState: StateFlow<Result<ArrayList<Pokemon>>> = _pokemonState*/

    /*private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery*/

    private val _pokemonFlow = MutableStateFlow<ApiResponse<PokemonResponse>>(ApiResponse.Loading())
    val pokemonFlow: StateFlow<ApiResponse<PokemonResponse>> = _pokemonFlow

    private var offset = 0
    private val limit = 20
    private var nextUrl: String? = null
    private var isLoading = false

    fun getPokemonList(initial: Boolean = false) {
        if (isLoading) return
        isLoading = true
        if (initial) offset = 0

        /*viewModelScope.launch {
            repository.getPokemonList(limit, offset).collect { response ->
                if (response is ApiResponse.Success) {
                    nextUrl = response.data?.next
                    offset += limit
                }
                _pokemonFlow.value = response
                isLoading = false
            }
        }*/
        viewModelScope.launch {
            repository.getPokemonList(limit, offset).collect { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        nextUrl = response.data?.next
                        offset += limit
                        isLoading = false
                    }
                    is ApiResponse.Error -> {
                        isLoading = false
                    }
                    is ApiResponse.Loading -> { /* don't reset here */ }
                }
                _pokemonFlow.value = response
            }
        }
    }

    fun hasNextPage(): Boolean = nextUrl != null

    /*fun fetchNextPage() {
        if (isLoadingMore || !hasMoreData) return

        isLoadingMore = true
        _pokemonState.value = Result.Loading

        viewModelScope.launch {
            repository.getPokemonList(pageSize * (currentPage + 1)).collect { result ->
                when (result) {
                    is Result.Success -> {
                        val newData = result.data
                        if (newData.size == allPokemon.size) {
                            hasMoreData = false
                        } else {
                            allPokemon.clear()
                            allPokemon.addAll(newData)
                            currentPage++
                        }
                        _pokemonState.value = Result.Success(ArrayList(allPokemon))
                    }
                    is Result.Error -> {
                        _pokemonState.value = result
                    }
                    else -> {}
                }
                isLoadingMore = false
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
    }*/
}
