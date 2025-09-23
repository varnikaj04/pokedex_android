package com.varnika_jain.pokedex.ui.details

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.databinding.ItemStatsBarBinding

class PokeDetailsAdapter(
    private val context: Context,
    private var statsList: ArrayList<PokemonDetails.Stats?>,
) : RecyclerView.Adapter<PokeDetailsAdapter.DetailViewHolder>() {
    private var progressTint: Int? = null
    private var textTint: Int? = null

    class DetailViewHolder(
        val binding: ItemStatsBarBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DetailViewHolder =
        DetailViewHolder(
            ItemStatsBarBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false,
            ),
        )

    override fun onBindViewHolder(
        holder: DetailViewHolder,
        position: Int,
    ) {
        with(holder) {
            binding.apply {
                tvStatLabel.text =
                    statsList[position]?.stat?.name?.replaceFirstChar { it.uppercaseChar() }
                statsList[position]?.base_stat?.let { progressStats.setProgress(it, true) }
                tvStatValue.text = statsList[position]?.base_stat.toString()

                progressTint?.let { color ->
                    progressStats.setIndicatorColor(color)
                }
                textTint?.let {
                    tvStatValue.setTextColor(textTint!!)
                }
            }
        }
    }

    override fun getItemCount() = statsList.size

    fun submitStatsList(
        list: ArrayList<PokemonDetails.Stats?>,
        bgColor: Int? = null,
        textColor: Int? = null,
    ) {
        statsList.clear()
        statsList.addAll(list)
        bgColor?.let { progressTint = it }
        textColor?.let { textTint = it }
        notifyDataSetChanged()
    }
}
