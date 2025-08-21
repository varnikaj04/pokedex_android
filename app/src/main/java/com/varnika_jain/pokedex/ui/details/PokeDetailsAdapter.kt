package com.varnika_jain.pokedex.ui.details

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.varnika_jain.pokedex.data.remote.PokemonDetails
import com.varnika_jain.pokedex.databinding.ItemStatsBarBinding

class PokeDetailsAdapter(
    private val context: Context,
    private var statsList: ArrayList<PokemonDetails.Stats>,
) : RecyclerView.Adapter<PokeDetailsAdapter.DetailViewHolder>() {
    class DetailViewHolder(val binding: ItemStatsBarBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailViewHolder {
        return DetailViewHolder(
            ItemStatsBarBinding.inflate(
                LayoutInflater.from(context), parent, false
            )
        )
    }

    override fun onBindViewHolder(
        holder: DetailViewHolder,
        position: Int
    ) {
        with(holder) {
            binding.apply {
                tvStatLabel.text = statsList[position].stat?.name
                statsList[position].baseStat?.let { progressStats.setProgress(it, true) }
                tvStatValue.text = statsList[position].baseStat.toString()
            }
        }
    }

    override fun getItemCount() = statsList.size

    fun submitStatsList(list: ArrayList<PokemonDetails.Stats>) {
        statsList.clear()
        statsList.addAll(list)
        notifyDataSetChanged()
    }

}