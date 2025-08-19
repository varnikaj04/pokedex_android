package com.varnika_jain.pokedex.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.varnika_jain.pokedex.R
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.data.remote.RetrofitInstance.pokemonRepository
import com.varnika_jain.pokedex.databinding.FragmentHomeBinding
import com.varnika_jain.pokedex.utils.viewModelFactory
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModelFactory {
        HomeViewModel(pokemonRepository)
    }
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: PokemonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        adapter = PokemonAdapter(
            requireContext(), arrayListOf()
        ) { pokemon ->
            val bundle = Bundle()
            bundle.putInt("pokemonId", pokemon.id)
            findNavController().navigate(R.id.action_homeFragment_to_detailFragment, bundle)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchPokemonList(10)
        binding.recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pokemonState.collect { result ->
                    when (result) {
                        is Result.Loading -> {
                            Log.d("TAG", "onViewCreated: Loading... ")
                        }

                        is Result.Success -> {
                            adapter.submitList(ArrayList(result.data))
                            Log.d("TAG", "onViewCreated: Success... ")
                        }

                        is Result.Error -> {
                            Log.d("TAG", "onViewCreated: Error... ")
                        }
                    }
                }
            }
        }
    }

}