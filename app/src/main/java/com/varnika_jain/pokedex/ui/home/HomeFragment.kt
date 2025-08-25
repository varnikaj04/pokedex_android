package com.varnika_jain.pokedex.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.varnika_jain.pokedex.R
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.data.remote.RetrofitInstance.pokemonRepository
import com.varnika_jain.pokedex.databinding.FragmentHomeBinding
import com.varnika_jain.pokedex.utils.activityViewModelFactory
import com.varnika_jain.pokedex.utils.collectFlow

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModelFactory {
        HomeViewModel(pokemonRepository)
    }
    private var pokemonList = ArrayList<Pokemon>()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: PokemonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        adapter = PokemonAdapter(
            requireContext()
        ) { pokemon, imageView ->
            val bundle = Bundle().apply {
                putInt("pokemonId", pokemon.id)
            }

            val extras = FragmentNavigatorExtras(
                imageView to "pokemon_image_${pokemon.id}" // unique transition name
            )

            findNavController().navigate(
                R.id.action_homeFragment_to_detailFragment, bundle, null, extras
            )
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        binding.recyclerView.doOnPreDraw {
            startPostponedEnterTransition()
        }

        if (viewModel.pokemonState.value !is Result.Success) {
            viewModel.fetchPokemonList(10)
        }
        binding.recyclerView.adapter = adapter

        binding.toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.searchPokemon -> true
                else -> false
            }
        }
        val searchItem = binding.toolBar.menu.findItem(R.id.searchPokemon)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Search Pokémon"

        if(viewModel.lastSearchQuery.isNotEmpty()){
            searchItem.expandActionView()
            searchView.setQuery(viewModel.lastSearchQuery, false)
            adapter.filterList(viewModel.lastSearchQuery)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.lastSearchQuery = newText.orEmpty()
                adapter.filterList(newText.orEmpty())
                return true
            }
        })

        collectFlow(viewModel.pokemonState) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("TAG", "onViewCreated: Loading... ")
                }

                is Result.Success -> {
                    pokemonList = result.data
                    adapter.submitList(pokemonList)
                }

                is Result.Error -> {
                    Log.d("TAG", "onViewCreated: Error... ")
                }
            }
        }
    }
}