package com.varnika_jain.pokedex.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.varnika_jain.pokedex.R
import com.varnika_jain.pokedex.data.remote.ApiResponse
import com.varnika_jain.pokedex.data.remote.RetrofitInstance.pokemonRepository
import com.varnika_jain.pokedex.databinding.FragmentHomeBinding
import com.varnika_jain.pokedex.utils.activityViewModelFactory
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by activityViewModelFactory {
        HomeViewModel(pokemonRepository)
    }

    //    private var pokemonList = ArrayList<Pokemon>()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: PokemonAdapter
    private var isLoading = false

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
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        viewModel.getPokemonList(initial = true)

        binding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as GridLayoutManager
                    layoutManager.spanSizeLookup =
                        object : GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int =
                                when (adapter.getItemViewType(position)) {
                                    PokemonAdapter.TYPE_LOADING -> layoutManager.spanCount // Footer spans both columns
                                    else -> 1
                                }
                        }
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
        lifecycleScope.launchWhenStarted {
            viewModel.pokemonFlow.collect { response ->
                when (response) {
                    is ApiResponse.Success -> {
                        val pokemonResponse = response.data
                        binding.progressPokemon.visibility = View.GONE
                        if (pokemonResponse != null) {
                            launch {
                                adapter.showLoadingFooter(false)
                                adapter.submitPokemonList(pokemonResponse.results, append = true)
                            }
                        }
                        Log.e(
                            "TAG",
                            "onViewCreated: pokemonListSize is : ${pokemonResponse?.results?.size}",
                        )
                    }

                    is ApiResponse.Error -> {
                        binding.progressPokemon.visibility = View.GONE
                        launch {
                            adapter.showLoadingFooter(false)
                        }
                        Log.e("TAG", "onViewCreated: error : ${response.message}")
                    }

                    is ApiResponse.Loading -> {
                        binding.progressPokemon.visibility = View.VISIBLE
                        if (adapter.itemCount > 0) {
                            adapter.showLoadingFooter(true)
                        }
                        Log.e("TAG", "onViewCreated: Loading...")
                    }
                }
            }
        }
        // Load first page
        /*if (viewModel.pokemonState.value !is Result.Success) {
            viewModel.fetchNextPage()
        }

        collectFlow(viewModel.filteredPokemon) {
            adapter.submitList(ArrayList(it))
        }

        collectFlow(viewModel.pokemonState) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("TAG", "onViewCreated: Loading... ")
                    if (adapter.itemCount == 0) {
                        binding.progressPokemon.visibility = View.VISIBLE
                    } else {
                        adapter.showLoadingFooter(true)
                    }
                    isLoading = true
                }

                is Result.Success -> {
                    binding.progressPokemon.visibility = View.GONE
                    adapter.showLoadingFooter(false)
                    isLoading = false
                }

                is Result.Error -> {
                    binding.progressPokemon.visibility = View.GONE
                    Log.d("TAG", "onViewCreated: Error... ")
                    adapter.showLoadingFooter(false)
                    isLoading = false
                }
            }
        }*/
        /*if (viewModel.pokemonState.value !is Result.Success) {
            viewModel.fetchPokemonList(10)
        }*/

        /*val searchItem = binding.toolBar.menu.findItem(R.id.searchPokemon)
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
        )*/
    }
}
