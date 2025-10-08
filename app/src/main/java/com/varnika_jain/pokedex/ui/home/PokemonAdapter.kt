package com.varnika_jain.pokedex.ui.home

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.varnika_jain.pokedex.data.remote.Pokemon
import com.varnika_jain.pokedex.databinding.ItemLoadingBinding
import com.varnika_jain.pokedex.databinding.ListItemPokemonBinding
import com.varnika_jain.pokedex.utils.ImageLoadState
import com.varnika_jain.pokedex.utils.loadImage

class PokemonAdapter(
    private val onPokemonClick: (Pokemon?, ImageView) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val TYPE_ITEM = 0
        const val TYPE_LOADING = 1
    }

    private val items = mutableListOf<Pokemon>()
    private var showLoader = false

    fun submitList(newItems: List<Pokemon>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun showBottomLoader(show: Boolean) {
        if (show == showLoader) return
        showLoader = show
        if (show) notifyItemInserted(items.size) else notifyItemRemoved(items.size)
    }

    override fun getItemViewType(position: Int): Int =
        if (position < items.size) TYPE_ITEM else TYPE_LOADING

    override fun getItemCount(): Int = items.size + if (showLoader) 1 else 0


    class PokemonViewHolder(
        val binding: ListItemPokemonBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    class LoadingViewHolder(
        val binding: ItemLoadingBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder =
        if (viewType == TYPE_ITEM) {
            val binding =
                ListItemPokemonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                )
            PokemonViewHolder(binding)
        } else {
            val binding =
                ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding)
        }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is PokemonViewHolder -> {
                with(holder) {
                    binding.apply {
                        val pokemon = items[position]
                        val progressBar: ProgressBar = loadingSpinner
                        imgPokemon.transitionName = "pokemon_image_${pokemon.id}"
                        imgPokemon.loadImage(
                            imageUrl = pokemon.imageUrl,
                            allowCaching = true,
                            imageLoadListener = { state ->
                                when (state) {
                                    is ImageLoadState.Loading ->
                                        progressBar.visibility =
                                            View.VISIBLE

                                    is ImageLoadState.Success -> {
                                        progressBar.visibility = View.GONE

                                        val drawable = imgPokemon.drawable
                                        if (drawable is BitmapDrawable) {
                                            Palette.from(drawable.bitmap).generate { palette ->
                                                val swatch =
                                                    palette?.dominantSwatch
                                                        ?: palette?.vibrantSwatch
                                                swatch?.let {
                                                    pokeLayout.setBackgroundColor(
                                                        ColorUtils.setAlphaComponent(
                                                            it.rgb,
                                                            (0.7f * 255).toInt(),
                                                        ),
                                                    )
                                                    tvPokeName.setTextColor(it.bodyTextColor)
                                                }
                                            }
                                        }
                                    }

                                    is ImageLoadState.Error -> {
                                        progressBar.visibility = View.GONE
                                        Log.e(
                                            "ImageView.loadImage",
                                            "Image load failed",
                                            state.throwable,
                                        )
                                    }
                                }
                            },
                        )
                        tvPokeName.text = pokemon.name.replaceFirstChar { it.uppercaseChar() }
                        holder.itemView.setOnClickListener { onPokemonClick(pokemon, imgPokemon) }
                    }
                }
            }

            is LoadingViewHolder -> {
                with(holder) {
                    binding.loadingFooter.isIndeterminate = true
                }
            }
        }
    }
}
