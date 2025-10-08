package com.varnika_jain.pokedex.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.data.remote.Resource
import com.varnika_jain.pokedex.repository.PokemonRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: PokemonRepository
) : ViewModel() {
    private val _pokemonDetails = MutableStateFlow<Resource<PokemonDetails>>(Resource.Loading)
    val pokemonDetails: StateFlow<Resource<PokemonDetails>> = _pokemonDetails

    fun fetchPokemonDetails(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.getPokemonDetails(id).collect { result ->
                    when (result) {
                        is Resource.Loading -> {
                            _pokemonDetails.value = Resource.Loading
                        }

                        is Resource.Success -> {
                            _pokemonDetails.value = Resource.Success(result.data)
                        }

                        is Resource.Error -> {
                            _pokemonDetails.value = Resource.Error(result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                _pokemonDetails.value =
                    Resource.Error(e.localizedMessage ?: "Unknown exception occurred")
            }
        }
    }
}