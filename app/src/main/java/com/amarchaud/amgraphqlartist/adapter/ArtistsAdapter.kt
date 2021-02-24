package com.amarchaud.amgraphqlartist.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.databinding.ItemArtistBinding
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.amgraphqlartist.view.ArtistsFragment
import com.amarchaud.amgraphqlartist.view.ArtistsFragmentDirections
import com.amarchaud.amgraphqlartist.view.BookmarksFragment
import com.amarchaud.amgraphqlartist.view.BookmarksFragmentDirections
import com.amarchaud.estats.model.database.AppDao
import com.bumptech.glide.Glide
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ArtistsAdapter(private val fragment: Fragment) :
    RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder>() {

    var artists: MutableList<ArtistEntity> = mutableListOf()
    var myDao: AppDao? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArtistsAdapter.ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArtistsAdapter.ArtistViewHolder, position: Int) {

        //val positionInList: Int = position % artists.size

        with(artists[position]) {

            holder.binding.artistName.text = name
            holder.binding.artistDisambiguation.text = disambiguation

            photoUrl?.let { url ->

                try {
                    Glide.with(fragment.requireContext())
                        .load(Uri.parse(url))
                        .error(R.drawable.unknown)
                        .into(holder.binding.artistImage)
                } catch (e: IllegalArgumentException) {
                    holder.binding.artistImage.setImageResource(R.drawable.unknown)
                }
            }

            if (myDao == null) {
                holder.binding.isBookMarked.visibility = View.INVISIBLE
                holder.binding.isBookMarked.isEnabled = false
            } else {
                GlobalScope.launch {
                    val r = if (myDao!!.getOneBookmark(this@with.id) != null) {
                        R.drawable.yellow_star
                    } else {
                        R.drawable.white_star
                    }

                    fragment.requireActivity().runOnUiThread {
                        holder.binding.isBookMarked.setImageResource(r)
                    }
                }
            }


            // manage Dao
            holder.binding.isBookMarked.setOnClickListener {

                if (myDao == null)
                    return@setOnClickListener

                when (fragment) {
                    is ArtistsFragment -> {
                        GlobalScope.launch {
                            if (myDao!!.getOneBookmark(this@with.id) == null) {
                                myDao!!.insert(this@with)

                                fragment.requireActivity().runOnUiThread {
                                    holder.binding.isBookMarked.setImageResource(R.drawable.yellow_star)
                                }

                            } else {
                                myDao!!.delete(this@with)

                                fragment.requireActivity().runOnUiThread {
                                    holder.binding.isBookMarked.setImageResource(R.drawable.white_star)
                                }
                            }
                        }
                    }
                    is BookmarksFragment -> {
                        // display a warning popup !
                        AlertDialog.Builder(fragment.requireContext())
                            .setTitle(R.string.DeleteEntryTitle)
                            .setMessage(R.string.DeleteEntryMessage)
                            .setPositiveButton(android.R.string.ok) { dialog, which ->

                                artists.removeAt(position)
                                notifyItemRemoved(position)

                                GlobalScope.launch {
                                    myDao!!.delete(this@with)
                                }
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
                    }
                    else -> {
                        // do nothing
                    }
                }


            }

            // display details
            holder.binding.artistDetails.setOnClickListener {
                when (fragment) {
                    is ArtistsFragment -> {
                        fragment.findNavController().navigate(
                            ArtistsFragmentDirections.actionArtistsFragmentToArtistDetailFragment(
                                artists[position]
                            )
                        )
                    }
                    is BookmarksFragment -> {
                        fragment.findNavController().navigate(
                            BookmarksFragmentDirections.actionBookmarksFragmentToArtistDetailFragment(
                                artists[position]
                            )
                        )
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    override fun getItemCount() = artists.size

    inner class ArtistViewHolder(var binding: ItemArtistBinding) :
        RecyclerView.ViewHolder(binding.root)
}