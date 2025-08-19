package com.varnika_jain.pokedex.utils

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import coil.request.CachePolicy
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation


fun Int.buildImageUrl(): String {
    return "https://unpkg.com/pokeapi-sprites@2.0.2/sprites/pokemon/other/dream-world/$this.svg"
}

fun ImageView.loadImage(
    imageUrl: String = "",
    @DrawableRes image: Int? = null,
    @DrawableRes placeHolder: Int? = null,
    cornerRadius: Float = 0F,
    loadCircleCrop: Boolean = false,
    shouldCrossFade: Boolean = false,
    crossFadeDurationMillis: Int = 0,
    allowCaching: Boolean = false,
    imageLoadListener: ((state: ImageLoadState) -> Unit)? = null,
) {
    val context = this.context
    val isSvg = imageUrl.endsWith(".svg", ignoreCase = true)

    val imageLoader = if (isSvg) {
        ImageLoader.Builder(context).components { add(SvgDecoder.Factory()) }.build()
    } else {
        ImageLoader.Builder(context).build()
    }

    val data = imageUrl.ifEmpty { image }

    this.load(data, imageLoader) {
        diskCachePolicy(if (allowCaching) CachePolicy.ENABLED else CachePolicy.DISABLED)
        if (placeHolder != null) placeholder(placeHolder)
        if (cornerRadius > 0 && !loadCircleCrop) {
            transformations(RoundedCornersTransformation(cornerRadius))
        }
        if (loadCircleCrop) {
            transformations(CircleCropTransformation())
        }
        if (shouldCrossFade) {
            crossfade(true)
            crossfade(crossFadeDurationMillis)
        }

        listener(onStart = {
            imageLoadListener?.invoke(ImageLoadState.Loading)
        }, onSuccess = { _, _ ->
            imageLoadListener?.invoke(ImageLoadState.Success)
        }, onError = { _, throwable ->
            imageLoadListener?.invoke(ImageLoadState.Error(throwable.throwable))
        })
    }
}

sealed class ImageLoadState {
    object Loading : ImageLoadState()
    object Success : ImageLoadState()
    data class Error(val throwable: Throwable?) : ImageLoadState()
}


inline fun <reified VM : ViewModel> Fragment.viewModelFactory(
    crossinline provider: () -> VM
): Lazy<VM> {
    return lazy {
        ViewModelProvider(this, GenericViewModelFactory { provider() })[VM::class.java]
    }
}
