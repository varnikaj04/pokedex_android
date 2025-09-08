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
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.data.remote.RetrofitInstance.pokemonRepository
import com.varnika_jain.pokedex.databinding.FragmentHomeBinding
import com.varnika_jain.pokedex.utils.activityViewModelFactory
import com.varnika_jain.pokedex.utils.collectFlow

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModelFactory {
        HomeViewModel(pokemonRepository)
    }

    //    private var pokemonList = ArrayList<Pokemon>()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: PokemonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        adapter =
            PokemonAdapter(
                requireContext(),
            ) { pokemon, imageView ->
                val bundle =
                    Bundle().apply {
                        putInt("pokemonId", pokemon.id)
                    }

                val extras =
                    FragmentNavigatorExtras(
                        imageView to "pokemon_image_${pokemon.id}",
                    )

                findNavController().navigate(
                    R.id.action_homeFragment_to_detailFragment,
                    bundle,
                    null,
                    extras,
                )
            }
        binding.recyclerView.adapter = adapter
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        binding.recyclerView.doOnPreDraw {
            startPostponedEnterTransition()
        }

        collectFlow(viewModel.filteredPokemon) {
            adapter.submitList(ArrayList(it))
        }

        collectFlow(viewModel.pokemonState) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("TAG", "onViewCreated: Loading... ")
                }

                is Result.Success -> {
                    binding.progressPokemon.visibility = View.GONE
                    /*pokemonList = result.data
                    adapter.submitList(pokemonList)*/
                }

                is Result.Error -> {
                    binding.progressPokemon.visibility = View.GONE
                    Log.d("TAG", "onViewCreated: Error... ")
                }
            }
        }
        if (viewModel.pokemonState.value !is Result.Success) {
            viewModel.fetchPokemonList()
        }

        val searchItem = binding.toolBar.menu.findItem(R.id.searchPokemon)
        val searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Search Pokémon"

        if (viewModel.searchQuery.value.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(viewModel.searchQuery.value, false)
        }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = true

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.setSearchQuery(newText.orEmpty())
                    return true
                }
            },
        )
    }
}
