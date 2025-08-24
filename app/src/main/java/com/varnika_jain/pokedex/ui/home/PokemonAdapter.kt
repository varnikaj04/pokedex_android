package com.varnika_jain.pokedex.ui.home

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.databinding.ListItemPokemonBinding
import com.varnika_jain.pokedex.utils.ImageLoadState
import com.varnika_jain.pokedex.utils.buildImageUrl
import com.varnika_jain.pokedex.utils.loadImage

class PokemonAdapter(
    val context: Context, private val onPokemonClick: (Pokemon, ImageView) -> Unit
) : RecyclerView.Adapter<PokemonAdapter.PokemonViewHolder>() {
    private val diffUtil = object : DiffUtil.ItemCallback<Pokemon>() {
        override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
            return oldItem == newItem
        }

    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

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
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.binding.apply {
            val pokemon = asyncListDiffer.currentList[position]
            val progressBar: ProgressBar = loadingSpinner
            imgPokemon.transitionName = "pokemon_image_${pokemon.id}"
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
                                    val swatch = palette?.dominantSwatch ?: palette?.vibrantSwatch
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

            holder.itemView.setOnClickListener { onPokemonClick(pokemon, imgPokemon) }

        }
    }

    fun submitList(pokeList: ArrayList<Pokemon>) {
        asyncListDiffer.submitList(pokeList)
    }
}