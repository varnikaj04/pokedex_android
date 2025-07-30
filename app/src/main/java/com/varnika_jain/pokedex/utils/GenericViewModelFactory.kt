package com.varnika_jain.pokedex.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GenericViewModelFactory<T : ViewModel>(
    private val creator: () -> T
) : ViewModelProvider.Factory {

    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        val viewModel = creator()
        if (modelClass.isAssignableFrom(viewModel.javaClass)) {
            @Suppress("UNCHECKED_CAST")
            return viewModel as VM
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}