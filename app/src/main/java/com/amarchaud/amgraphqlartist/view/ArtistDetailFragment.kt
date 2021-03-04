package com.amarchaud.amgraphqlartist.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.adapter.AlbumsAdapter
import com.amarchaud.amgraphqlartist.adapter.ArtistsAdapter
import com.amarchaud.amgraphqlartist.databinding.FragmentArtistDetailBinding
import com.amarchaud.amgraphqlartist.interfaces.IArtistClickListener
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.amarchaud.amgraphqlartist.viewmodel.ArtistDetailViewModel
import com.amarchaud.amgraphqlartist.viewmodel.data.ArtistToDeleteViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArtistDetailFragment : Fragment(), IArtistClickListener {

    companion object {
        const val TAG = "BookMarksFragment"
    }

    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    // give Id to ViewModel by injection
    private val viewModel: ArtistDetailViewModel by viewModels()

    // arguments given by NavigationGraph
    val args: ArtistDetailFragmentArgs by navArgs()

    // recycler view
    private var albumsRecyclerAdapter = AlbumsAdapter()
    private var relationShipsRecyclerAdapter = ArtistsAdapter(this)

    // special viewModel
    private val artistToDeleteViewModel: ArtistToDeleteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArtistDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.artistDetailViewModel = viewModel
        binding.lifecycleOwner = this


        // give param to viewmodel
        viewModel.artistApp = args.artist

        with(binding) {

            commonDetails.artistBookmark.isVisible = false

            //colorize the favorites based on if this location has been favorited by the user
            detailsIsFavorite.visibility = View.INVISIBLE

            // init Favorite image
            lifecycleScope.launch {

                requireActivity().runOnUiThread {

                    commonDetails.artistName.text = args.artist.name
                    commonDetails.artistDisambiguation.text = args.artist.disambiguation

                    args.artist.photoUrl?.let {
                        try {
                            Glide.with(requireContext())
                                .load(Uri.parse(it))
                                .error(R.drawable.ic_unknown)
                                .into(commonDetails.artistImage)
                        } catch (e: IllegalArgumentException) {
                            commonDetails.artistImage.setImageResource(R.drawable.ic_unknown)
                        }
                    }

                    detailsIsFavorite.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (args.artist.isFavorite)
                                R.drawable.star_circle
                            else
                                R.drawable.star_circle_disabled
                        )
                    )
                    detailsIsFavorite.visibility = View.VISIBLE
                }
            }

            detailsIsFavorite.setOnClickListener {
                viewModel.onBookmarkClicked()

                requireActivity().runOnUiThread {
                    detailsIsFavorite.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (viewModel.artistApp.isFavorite)
                                R.drawable.star_circle
                            else
                                R.drawable.star_circle_disabled
                        )
                    )
                    detailsIsFavorite.visibility = View.VISIBLE
                }
            }

            /*
            viewModel.isArtistInBookMarkedDb.observe(viewLifecycleOwner, {
                requireActivity().runOnUiThread {
                    detailsIsFavorite.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            if (it) R.drawable.star_circle else R.drawable.star_circle_disabled
                        )
                    )
                    detailsIsFavorite.visibility = View.VISIBLE
                }
            })*/

            commonDetails.artistBookmark.visibility = View.INVISIBLE
            // **************** Recycler View management

            albumsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
            //LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            albumsRecyclerView.adapter = albumsRecyclerAdapter

            viewModel.albumsLiveData.observe(viewLifecycleOwner, { l ->
                l?.let {
                    albumsRecyclerAdapter.albums = l
                    albumsRecyclerAdapter.notifyDataSetChanged()
                }
            })


            artistsRecyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            artistsRecyclerView.adapter = relationShipsRecyclerAdapter

            viewModel.artistsRelationshipsLiveData.observe(viewLifecycleOwner, {
                relationShipsRecyclerAdapter.setArtist(it)
            })

        }
        viewModel.onSearch()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

        if (viewModel.artistApp.isFavorite) {
            artistToDeleteViewModel.artistToDeleteLiveData.postValue(
                null
            )
        } else {
            artistToDeleteViewModel.artistToDeleteLiveData.postValue(
                ArtistToDeleteViewModel.ArtistToDelete(viewModel.artistApp)
            )
        }
    }


    /**
     * Management relationship here
     */
    override fun onArtistClicked(artistApp: ArtistApp) {

    }

    override fun onBookmarkClicked(artistApp: ArtistApp) {

    }
}