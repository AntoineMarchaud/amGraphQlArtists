package com.amarchaud.amgraphqlartist.bindingadapter

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.amarchaud.amgraphqlartist.R
import com.bumptech.glide.Glide

object ImageViewBindingAdapterLoad {
    @JvmStatic
    @BindingAdapter("onImageLoadFromUrl")
    fun setOnImageLoadFromUrl(view: ImageView, url: String?) {
        url?.let {
            try {
                Glide.with(view.context)
                    .load(Uri.parse(url))
                    .error(R.drawable.ic_unknown)
                    .into(view)
            } catch (e: IllegalArgumentException) {
                view.setImageResource(R.drawable.ic_unknown)
            }
        }
    }

    @JvmStatic
    @BindingAdapter("onImageLoadFromUri")
    fun setOnImageLoadFromStr(view: ImageView, uri: Uri?) {
        uri?.let {
            try {
                Glide.with(view.context)
                    .load(uri)
                    .error(R.drawable.ic_unknown)
                    .into(view)
            } catch (e: IllegalArgumentException) {
                view.setImageResource(R.drawable.ic_unknown)
            }
        }
    }
}