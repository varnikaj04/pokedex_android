package com.varnika_jain.pokedex.ui.details

import android.content.res.ColorStateList
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.google.android.material.textview.MaterialTextView
import com.varnika_jain.pokedex.R
import com.varnika_jain.pokedex.data.local.PaletteColors
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.data.remote.Result
import com.varnika_jain.pokedex.data.remote.RetrofitInstance.pokemonRepository
import com.varnika_jain.pokedex.databinding.FragmentDetailsBinding
import com.varnika_jain.pokedex.utils.ImageLoadState
import com.varnika_jain.pokedex.utils.activityViewModelFactory
import com.varnika_jain.pokedex.utils.buildImageUrl
import com.varnika_jain.pokedex.utils.collectFlow
import com.varnika_jain.pokedex.utils.loadImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailFragment : Fragment() {
    private val viewModel: DetailViewModel by activityViewModelFactory {
        DetailViewModel(pokemonRepository)
    }
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var pokemonDetails: PokemonDetails
    private lateinit var adapter: PokeDetailsAdapter
    private val args: DetailFragmentArgs by navArgs()
    private val _paletteColors = MutableStateFlow(PaletteColors(null, null))
    private val paletteColors: StateFlow<PaletteColors> = _paletteColors.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition =
            TransitionInflater
                .from(requireContext())
                .inflateTransition(android.R.transition.move)
        sharedElementReturnTransition =
            TransitionInflater
                .from(requireContext())
                .inflateTransition(android.R.transition.move)

        postponeEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        val pokemonId = args.pokemonId
        Log.d("TAG", "onCreateView: $pokemonId")
        binding.ivPokemonImg.transitionName = "pokemon_image_$pokemonId" // must match

        binding.backBtn.setOnClickListener { findNavController().navigateUp() }
        adapter =
            PokeDetailsAdapter(
                context = requireContext(),
                statsList = arrayListOf(),
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
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

        viewLifecycleOwner.lifecycleScope.launch {
            paletteColors.collectLatest { colors ->
                colors.background?.let { bg ->
                    binding.ivPokemonImg.setBackgroundColor(bg)
                    tintPowerTypes(bg)
                    setPokemonStats()
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
                        viewLifecycleOwner.lifecycleScope.launch {
                            val color = extractDominantColor(drawable)
                            if (color != null) {
                                _paletteColors.value = color
                            }
                        }
                        startPostponedEnterTransition()
                    }

                    is ImageLoadState.Error -> {
                        startPostponedEnterTransition()
                        Log.e(
                            "ImageView.loadImage",
                            "Image load failed",
                            state.throwable,
                        )
                    }
                }
            },
        )
    }

    private fun setupPokemonDetails() {
        binding.tvNamePokemon.text =
            pokemonDetails.name?.replaceFirstChar { c -> c.uppercaseChar() }
        binding.tvWeightValue.text =
            String.format(getString(R.string.str_pokemon_weight), pokemonDetails.weight)
        binding.tvHeightValue.text =
            String.format(getString(R.string.str_pokemon_height), pokemonDetails.height)
        setPokemonTypes()
    }

    private fun setPokemonStats() {
        val pokemonStats = pokemonDetails.stats
        binding.rvStats.adapter = adapter
        if (pokemonStats.isNotEmpty()) {
            adapter.submitStatsList(
                pokemonStats,
                paletteColors.value.background,
                paletteColors.value.text,
            )
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

    private suspend fun extractDominantColor(drawable: Drawable): PaletteColors? =
        withContext(Dispatchers.Default) {
            if (drawable is BitmapDrawable) {
                val palette = Palette.from(drawable.bitmap).generate()
                val swatch = palette.dominantSwatch ?: palette.vibrantSwatch
                swatch?.let {
                    PaletteColors(
                        background = ColorUtils.setAlphaComponent(it.rgb, (0.7f * 255).toInt()),
                        text = it.bodyTextColor,
                    )
                } ?: PaletteColors(null, null)
            } else {
                PaletteColors(null, null)
            }
        }
}
