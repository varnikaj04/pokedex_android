package com.varnika_jain.pokedex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.data.remote.PokemonResponse
import com.varnika_jain.pokedex.data.remote.Resource
import com.varnika_jain.pokedex.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeViewModel(
    private val repository: PokemonRepository,
) : ViewModel() {
    private val _pokemonFlow = MutableStateFlow<Resource<PokemonResponse>>(Resource.Loading)
    val pokemonFlow: StateFlow<Resource<PokemonResponse>> = _pokemonFlow

    private val _pokemonList = mutableListOf<Pokemon>()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filteredPokemon = MutableStateFlow<List<Pokemon>>(emptyList())
    val filteredPokemon: StateFlow<List<Pokemon>> = _filteredPokemon

    private var offset = 0
    private val limit = 20
    private var nextUrl: String? = null
    private var isLoading = false

    init {
        loadInitialPokemon()
        observeSearchQuery()
    }

    fun loadInitialPokemon() {
        if (isLoading) return
        offset = 0
        _pokemonList.clear()
        fetchPokemon()
    }

    fun fetchNextPage() {
        if (isLoading || nextUrl == null || _searchQuery.value.isNotBlank()) return
        fetchPokemon(append = true)
    }


    private fun fetchPokemon(append: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            repository.getPokemonList(limit, offset).collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        if (!append) _pokemonFlow.value = result
                    }

                    is Resource.Success -> {
                        val newItems = result.data.results
                        if (!append) _pokemonList.clear()
                        _pokemonList.addAll(newItems)

                        nextUrl = result.data.next
                        offset += limit

                        _pokemonFlow.value = Resource.Success(
                            result.data.copy(results = ArrayList(_pokemonList))
                        )

                        updateFilteredList()
                        isLoading = false
                    }

                    is Resource.Error -> {
                        _pokemonFlow.value = result
                        isLoading = false
                    }
                }
            }
        }
    }

    fun hasNextPage(): Boolean = nextUrl != null && _searchQuery.value.isBlank()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest {
                    updateFilteredList()
                }
        }
    }

    private fun updateFilteredList() {
        val query = _searchQuery.value.trim()
        _filteredPokemon.value = if (query.isBlank()) {
            _pokemonList
        } else {
            _pokemonList.filter { it.name.startsWith(query, ignoreCase = true) }
        }
    }
}
