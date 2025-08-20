package com.varnika_jain.pokedex.ui.details

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
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


class DetailFragment : Fragment() {
    private val viewModel: DetailViewModel by viewModelFactory {
        DetailViewModel(pokemonRepository)
    }
    private lateinit var binding: FragmentDetailsBinding
    private lateinit var pokemonDetails: PokemonDetails
    private val args: DetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val inflater = LayoutInflater.from(requireContext())

        viewModel.fetchPokemonDetails(args.pokemonId)

        collectFlow(viewModel.pokemonState) { result ->
            when (result) {
                is Result.Loading -> {
                    Log.d("TAG", "onViewCreated: Loading... ")
                }

                is Result.Success -> {
                    pokemonDetails = result.data
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
                                                binding.ivPokemonImg.setBackgroundColor(
                                                    ColorUtils.setAlphaComponent(
                                                        it.rgb, (0.7f * 255).toInt()
                                                    )
                                                )
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
                    Log.d("TAG", "onViewCreated: Success... Result ${result.data.name} ")
                }

                is Result.Error -> {
                    Log.d("TAG", "onViewCreated: Error... ")
                }
            }
        }

        val parentLayout = view.findViewById<LinearLayout>(R.id.layoutPowerTypes)
        val powerTypes = listOf("Water", "Electric")

        parentLayout.removeAllViews()
        for (type in powerTypes) {
            val textView =
                inflater.inflate(R.layout.item_power_type, parentLayout, false) as MaterialTextView
            textView.text = type
            parentLayout.addView(textView)
        }
    }
}