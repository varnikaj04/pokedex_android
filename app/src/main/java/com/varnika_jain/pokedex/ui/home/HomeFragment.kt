package com.varnika_jain.pokedex.ui.home

import android.os.Bundle
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
import com.varnika_jain.pokedex.data.remote.Resource
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
        setupRecyclerview()
        return binding.root
    }

    private fun setupRecyclerview() {
        adapter = PokemonAdapter { pokemon, imageView ->
            val bundle = Bundle().apply {
                putInt("pokemonId", pokemon?.id ?: 0)
            }

            val extras = FragmentNavigatorExtras(
                imageView to "pokemon_image_${pokemon?.id}",
            )

            findNavController().navigate(
                R.id.action_homeFragment_to_detailFragment,
                bundle,
                null,
                extras,
            )
        }

        val layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.layoutManager = layoutManager

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int =
                when (adapter.getItemViewType(position)) {
                    PokemonAdapter.TYPE_LOADING -> layoutManager.spanCount
                    else -> 1
                }
        }

        binding.recyclerView.adapter = adapter

        binding.recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int,
                ) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0) return

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // Stop if no more pages to load
                    if (!viewModel.hasNextPage()) return

                    val isAtEnd =
                        (visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                                firstVisibleItemPosition >= 0

                    if (isAtEnd) {
                        // Defer UI modifications (adapter notifications) to next frame
                        recyclerView.post {
                            // Show loader safely (avoids IllegalStateException)
                            adapter.showBottomLoader(true)

                            // Request next page from ViewModel
                            viewModel.fetchNextPage()
                        }
                    }
                }
            },
        )
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
        setupSearch()
        observeData()
    }

    private fun setupSearch() {
        val searchItem = binding.toolBar.menu.findItem(R.id.searchPokemon)
        val searchView = searchItem.actionView as SearchView

        searchView.queryHint = "Search Pokémon"

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

    private fun observeData() {

        collectFlow(viewModel.filteredPokemon) {
            adapter.submitList(it)
        }
        collectFlow(viewModel.pokemonFlow) { response ->
            when (response) {
                is Resource.Loading -> adapter.showBottomLoader(true)
                is Resource.Success -> {
                    adapter.showBottomLoader(false)
                    adapter.submitList(response.data.results)
                }

                is Resource.Error -> adapter.showBottomLoader(false)
            }
        }
    }
}
