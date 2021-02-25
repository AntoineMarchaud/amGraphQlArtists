package com.amarchaud.amgraphqlartist.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.databinding.ItemAlbumBinding
import com.amarchaud.amgraphqlartist.fragment.ArtistDetailsFragment
import com.bumptech.glide.Glide

class AlbumsAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder>() {

    var albums: List<ArtistDetailsFragment.Node> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlbumsAdapter.AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumsAdapter.AlbumViewHolder, position: Int) {

        with(albums[position]) {

            holder.binding.albumName.text = title()
            holder.binding.ratingBar.rating = ratingBar(this)

            coverArtArchive()?.front()?.let {
                try {
                    Glide.with(fragment.requireContext())
                        .load(Uri.parse(it.toString()))
                        .error(R.drawable.unknown)
                        .into(holder.binding.albumImage)
                } catch (e: IllegalArgumentException) {
                    holder.binding.albumImage.setImageResource(R.drawable.unknown)
                }
            }

        }
    }

    override fun getItemCount(): Int = albums.size

    inner class AlbumViewHolder(var binding: ItemAlbumBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun ratingBar(artistDetailsFragment: ArtistDetailsFragment.Node): Float {
        return artistDetailsFragment.rating()?.value()?.div(2)?.toFloat() ?: 0f
    }

}