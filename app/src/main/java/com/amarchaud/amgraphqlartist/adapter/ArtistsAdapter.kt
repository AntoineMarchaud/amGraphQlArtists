package com.amarchaud.amgraphqlartist.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.databinding.ItemArtistBinding
import com.amarchaud.amgraphqlartist.interfaces.IArtistClickListener
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.bumptech.glide.Glide


class ArtistsAdapter(private val onClickListener: IArtistClickListener) :
    RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder>() {
    private var artists: List<ArtistApp> = ArrayList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArtistsAdapter.ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistsAdapter.ArtistViewHolder, position: Int) {

        with(artists[position]) {

            holder.binding.artistName.text = name
            holder.binding.artistDisambiguation.text = disambiguation

            if (photoUrl.isNullOrEmpty()) {
                holder.binding.artistImage.setImageResource(R.drawable.ic_unknown)
            } else {
                try {
                    Glide.with(holder.itemView)
                        .load(Uri.parse(photoUrl))
                        .error(R.drawable.ic_unknown)
                        .into(holder.binding.artistImage)
                } catch (e: IllegalArgumentException) {
                    holder.binding.artistImage.setImageResource(R.drawable.ic_unknown)
                }
            }

            //set the initial state of the favorites icon by checking if its a favorite in the database
            setupFavoriteIndicator(holder.binding, this, onClickListener)

            // display details
            holder.itemView.setOnClickListener {
                onClickListener.onArtistClicked(this)
            }
        }
    }

    override fun getItemCount() = artists.size

    inner class ArtistViewHolder(var binding: ItemArtistBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun setArtistWithoutRefresh(newArtists: List<ArtistApp>) {
        artists = newArtists
    }

    fun setArtist(newArtists: List<ArtistApp>) {
        if (newArtists.isNullOrEmpty()) {
            artists = newArtists
            notifyDataSetChanged()
            return
        }

        val diffResult = DiffUtil.calculateDiff(DiffCallback(this.artists, newArtists), true)

        artists = newArtists

        // This will notify the adapter of what is new data, and will animate/update it for you ("this" being the adapter)
        diffResult.dispatchUpdatesTo(this)
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

    open class DiffCallback(
        private val oldList: List<ArtistApp>,
        private val newList: List<ArtistApp>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].areItemsSame(newList[newItemPosition])
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].areContentsSame(newList[newItemPosition])
        }
    }
}