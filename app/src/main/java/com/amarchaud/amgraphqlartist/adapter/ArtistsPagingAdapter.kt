package com.amarchaud.amgraphqlartist.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.databinding.ItemArtistBinding
import com.amarchaud.amgraphqlartist.interfaces.IArtistClickListener
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.bumptech.glide.Glide

class ArtistsPagingAdapter(private val onClickListener: IArtistClickListener) :
    PagingDataAdapter<ArtistApp, ArtistsPagingAdapter.ArtistViewHolder>(ArtistsComparator) {

    inner class ArtistViewHolder(var binding: ItemArtistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindArtist(item: ArtistApp) = with(binding) {

            artistName.text = item.name
            artistDisambiguation.text = item.disambiguation

            if (item.photoUrl.isNullOrEmpty()) {
                artistImage.setImageResource(R.drawable.ic_unknown)
            } else {
                try {
                    Glide.with(itemView)
                        .load(Uri.parse(item.photoUrl))
                        .error(R.drawable.ic_unknown)
                        .into(artistImage)
                } catch (e: IllegalArgumentException) {
                    artistImage.setImageResource(R.drawable.ic_unknown)
                }
            }

            //set the initial state of the favorites icon by checking if its a favorite in the database
            setupFavoriteIndicator(this, item, onClickListener)

            // display details
            itemView.setOnClickListener {
                onClickListener.onArtistClicked(item)
            }
        }
    }


    override fun onBindViewHolder(holder: ArtistViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bindArtist(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        return ArtistViewHolder(
            ItemArtistBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }


    //set the initial state of the favorites icon by checking if its a favorite in the database
    private fun setupFavoriteIndicator(
        binding: ItemArtistBinding,
        artistApp: ArtistApp,
        clickListener: IArtistClickListener
    ) {

        with(binding) {

            artistBookmark.isChecked = artistApp.isFavorite //set default

            //handle the status changes for favorites when the user clicks the star
            artistBookmark.setOnClickListener {
                artistApp.id.let {
                    artistApp.isFavorite = !artistApp.isFavorite
                    artistBookmark.isChecked = artistApp.isFavorite //set new value
                    clickListener.onBookmarkClicked(artistApp)
                }
            }
        }

    }

    object ArtistsComparator : DiffUtil.ItemCallback<ArtistApp>() {
        override fun areItemsTheSame(oldItem: ArtistApp, newItem: ArtistApp): Boolean {
            return oldItem.areItemsSame(newItem)
        }

        override fun areContentsTheSame(
            oldItem: ArtistApp,
            newItem: ArtistApp
        ): Boolean {
            return oldItem.areContentsSame(newItem)
        }
    }
}

