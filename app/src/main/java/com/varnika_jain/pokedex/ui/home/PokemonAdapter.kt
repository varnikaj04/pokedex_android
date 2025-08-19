package com.varnika_jain.pokedex.ui.home

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.databinding.ListItemPokemonBinding
import com.varnika_jain.pokedex.utils.ImageLoadState
import com.varnika_jain.pokedex.utils.buildImageUrl
import com.varnika_jain.pokedex.utils.loadImage

class PokemonAdapter(
    val context: Context,
    private var pokemonList: ArrayList<Pokemon>,
    private val onPokemonClick: (Pokemon) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {

    class PokemonViewHolder(val binding: ListItemPokemonBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        return PokemonViewHolder(
            ListItemPokemonBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return pokemonList.size
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        with(holder) {
            binding.apply {
                val pokemon = pokemonList[position]
                val progressBar: ProgressBar = loadingSpinner

                imgPokemon.loadImage(
                    imageUrl = pokemon.id.buildImageUrl(),
                    allowCaching = true,
                    imageLoadListener = { state ->
                        when (state) {
                            is ImageLoadState.Loading -> progressBar.visibility = View.VISIBLE
                            is ImageLoadState.Success -> {
                                progressBar.visibility = View.GONE

                                val drawable = imgPokemon.drawable
                                if (drawable is BitmapDrawable) {
                                    Palette.from(drawable.bitmap).generate { palette ->
                                        val swatch =
                                            palette?.dominantSwatch ?: palette?.vibrantSwatch
                                        swatch?.let {
                                            pokeLayout.setBackgroundColor(
                                                ColorUtils.setAlphaComponent(
                                                    it.rgb, (0.7f * 255).toInt()
                                                )
                                            )
                                            tvPokeName.setTextColor(it.bodyTextColor)
                                        }
                                    }
                                }

                            }

                            is ImageLoadState.Error -> {
                                progressBar.visibility = View.GONE
                                Log.e("ImageView.loadImage", "Image load failed", state.throwable)
                            }
                        }
                    })
                tvPokeName.text = pokemon.name.replaceFirstChar { it.uppercaseChar() }

                itemView.setOnClickListener { onPokemonClick(pokemon) }

            }
        }
    }

    fun submitList(pokeList: ArrayList<Pokemon>) {
        pokemonList.clear()
        pokemonList.addAll(pokeList)
        notifyDataSetChanged()
    }

}