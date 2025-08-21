package com.varnika_jain.pokedex.ui.details

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.google.android.material.textview.MaterialTextView
import com.varnika_jain.pokedex.R
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.data.remote.RetrofitInstance.pokemonRepository
import com.varnika_jain.pokedex.databinding.FragmentDetailsBinding
import com.varnika_jain.pokedex.utils.ImageLoadState
import com.varnika_jain.pokedex.utils.buildImageUrl
import com.varnika_jain.pokedex.utils.collectFlow
import com.varnika_jain.pokedex.utils.loadImage
import com.varnika_jain.pokedex.utils.viewModelFactory
import java.util.ArrayList


class DetailFragment : Fragment() {
    private val viewModel: DetailViewModel by viewModelFactory {
        DetailViewModel(pokemonRepository)
    }
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var pokemonDetails: PokemonDetails
    private lateinit var adapter: PokeDetailsAdapter
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(layoutInflater)

        binding.backBtn.setOnClickListener { findNavController().navigateUp() }
        adapter = PokeDetailsAdapter(
            requireContext(), arrayListOf()
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchPokemonDetails(args.pokemonId)

        collectFlow(viewModel.pokemonState) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("TAG", "onViewCreated: Loading... ")
                }

                is Result.Success -> {
                    pokemonDetails = result.data
                    setupPokemonImage()
                    setupPokemonDetails()
                    Log.d("TAG", "onViewCreated: Success... Result ${result.data.name} ")
                }

                is Result.Error -> {
                    Log.d("TAG", "onViewCreated: Error... ")
                }
            }
        }

    }

    private fun setupPokemonImage() {
        binding.ivPokemonImg.loadImage(
            imageUrl = args.pokemonId.buildImageUrl(),
            allowCaching = true,
            imageLoadListener = { state ->
                when (state) {
                    is ImageLoadState.Loading -> {
                        Log.d("TAG", "onViewCreated: Image is loading....")
                    }

                    is ImageLoadState.Success -> {

                        val drawable = binding.ivPokemonImg.drawable
                        if (drawable is BitmapDrawable) {
                            Palette.from(drawable.bitmap).generate { palette ->
                                val swatch =
                                    palette?.dominantSwatch ?: palette?.vibrantSwatch
                                swatch?.let {
                                    val bgColor =
                                        ColorUtils.setAlphaComponent(it.rgb, (0.7f * 255).toInt())
                                    binding.ivPokemonImg.setBackgroundColor(bgColor)
                                    tintPowerTypes(bgColor)
                                }
                            }
                        }

                    }

                    is ImageLoadState.Error -> {
                        Log.e(
                            "ImageView.loadImage", "Image load failed", state.throwable
                        )
                    }
                }
            })
    }

    private fun setupPokemonDetails() {
        binding.tvNamePokemon.text =
            pokemonDetails.name?.replaceFirstChar { c -> c.uppercaseChar() }
        binding.tvWeightValue.text =
            String.format(getString(R.string.str_pokemon_weight), pokemonDetails.weight)
        binding.tvHeightValue.text =
            String.format(getString(R.string.str_pokemon_height), pokemonDetails.height)
        setPokemonTypes()
        setPokemonStats()
    }

    private fun setPokemonStats() {
        val pokemonStats = pokemonDetails.stats
        val statList = ArrayList<PokemonDetails.Stats>(pokemonStats)
        binding.rvStats.adapter = adapter
        if (!pokemonStats.isNullOrEmpty()){
            adapter.submitStatsList(statList)
        }

    }

    private fun setPokemonTypes() {
        val inflater = LayoutInflater.from(requireContext())
        val parentLayout = view?.findViewById<LinearLayout>(R.id.layoutPowerTypes)
        val pokemonTypes = pokemonDetails.types

        parentLayout?.removeAllViews()
        pokemonTypes?.forEach { types ->
            val textView =
                inflater.inflate(R.layout.item_power_type, parentLayout, false) as MaterialTextView
            textView.text = types?.type?.name
            textView.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.primary)
            parentLayout?.addView(textView)
        }
    }

    private fun tintPowerTypes(color: Int) {
        val parentLayout = binding.layoutPowerTypes
        for (i in 0 until parentLayout.childCount) {
            val child = parentLayout.getChildAt(i)
            if (child is MaterialTextView) {
                child.backgroundTintList = ColorStateList.valueOf(color)
            }
        }
    }
}