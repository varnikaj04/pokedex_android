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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varnika_jain.pokedex.R
import com.varnika_jain.pokedex.data.remote.ApiResponse
import com.varnika_jain.pokedex.data.remote.RetrofitInstance.pokemonRepository
import com.varnika_jain.pokedex.databinding.FragmentHomeBinding
import com.varnika_jain.pokedex.utils.activityViewModelFactory
import com.varnika_jain.pokedex.utils.collectFlow

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModelFactory {
        HomeViewModel(pokemonRepository)
    }
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: PokemonAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        adapter =
            PokemonAdapter { pokemon, imageView ->
                val bundle =
                    Bundle().apply {
                        putInt("pokemonId", pokemon?.id ?: 0)
                    }

                val extras =
                    FragmentNavigatorExtras(
                        imageView to "pokemon_image_${pokemon?.id}",
                    )

                findNavController().navigate(
                    R.id.action_homeFragment_to_detailFragment,
                    bundle,
                    null,
                    extras,
                )
            }
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

        binding.recyclerView.adapter = adapter
        val layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.layoutManager = layoutManager

        layoutManager.spanSizeLookup =
            object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int =
                    when (adapter.getItemViewType(position)) {
                        PokemonAdapter.TYPE_LOADING -> layoutManager.spanCount
                        else -> 1
                    }
            }

        if (viewModel.pokemonList.isEmpty()) {
            viewModel.getPokemonList(initial = true)
        } else {
            adapter.submitPokemonList(viewModel.pokemonList)
            binding.progressPokemon.visibility = View.GONE
        }

        binding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!viewModel.hasNextPage()) return

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        adapter.showLoadingFooter(true)
                        viewModel.getPokemonList(initial = false)
                    }
                }
            },
        )
        collectFlow(viewModel.pokemonFlow) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val pokemonResponse = response.data
                    binding.progressPokemon.visibility = View.GONE
                    if (pokemonResponse != null) {
                        adapter.showLoadingFooter(false)
                        adapter.submitPokemonList(pokemonResponse.results)
                    }
                }

                is ApiResponse.Error -> {
                    binding.progressPokemon.visibility = View.GONE
                    adapter.showLoadingFooter(false)
                    Log.e("TAG", "onViewCreated: error : ${response.message}")
                }

                is ApiResponse.Loading -> {
                    if (adapter.itemCount == 0) {
                        binding.progressPokemon.visibility = View.VISIBLE
                    } else {
                        adapter.showLoadingFooter(true)
                    }
                    Log.e("TAG", "onViewCreated: Loading...")
                }
            }
        }

        collectFlow(viewModel.filteredPokemon) { list ->
            binding.progressPokemon.visibility = View.GONE
            adapter.showLoadingFooter(false)
            adapter.submitPokemonList(ArrayList(list))
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
