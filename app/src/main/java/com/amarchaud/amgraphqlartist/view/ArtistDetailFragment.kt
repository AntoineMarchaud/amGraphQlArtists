package com.amarchaud.amgraphqlartist.view

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.adapter.AlbumsAdapter
import com.amarchaud.amgraphqlartist.adapter.ArtistsAdapter
import com.amarchaud.amgraphqlartist.databinding.FragmentArtistDetailBinding
import com.amarchaud.amgraphqlartist.viewmodel.ArtistDetailViewModel
import com.amarchaud.amgraphqlartist.viewmodel.data.ArtistToDeleteViewModel
import com.amarchaud.estats.model.database.AppDao
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ArtistDetailFragment : Fragment() {


    companion object {
        const val TAG = "BookMarksFragment"
    }

    private var _binding: FragmentArtistDetailBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var myDao: AppDao

    @Inject
    lateinit var viewModelFactory: ArtistDetailViewModel.AssistedFactory


    // give Id to ViewModel by injection
    private val viewModel: ArtistDetailViewModel by viewModels {
        ArtistDetailViewModel.provideFactory(viewModelFactory, args.artist)
    }

    // arguments given by NavigationGraph
    val args: ArtistDetailFragmentArgs by navArgs()

    // recycler view
    private var albumsRecyclerAdapter = AlbumsAdapter(this)
    private var artistsRecyclerAdapter = ArtistsAdapter(this)

    // special viewModel
    private val artistToDeleteViewModel: ArtistToDeleteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        artistsRecyclerAdapter.myDao = myDao

        _binding = FragmentArtistDetailBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.artistDetailViewModel = viewModel
        binding.lifecycleOwner = this

        with(binding) {

            commonDetails.artistDetails.isEnabled = false
            commonDetails.artistDetails.visibility = View.INVISIBLE
            commonDetails.isBookMarked.setImageResource(R.drawable.yellow_star)

            commonDetails.isBookMarked.setOnClickListener {
                viewModel.onBookMarkedClick()
            }
            viewModel.isArtistInDatabase.observe(viewLifecycleOwner, {
                commonDetails.isBookMarked.setImageResource(if (it) R.drawable.yellow_star else R.drawable.white_star)
            })

            viewModel.nameLiveData.observe(viewLifecycleOwner, {
                commonDetails.artistName.text = it
            })
            viewModel.disambiguationLiveData.observe(viewLifecycleOwner, {
                commonDetails.artistDisambiguation.text = it
            })
            viewModel.photoUrlLiveData.observe(viewLifecycleOwner, {

                try {
                    Glide.with(requireContext())
                        .load(Uri.parse(it))
                        .error(R.drawable.unknown)
                        .into(commonDetails.artistImage)
                } catch (e: IllegalArgumentException) {
                    commonDetails.artistImage.setImageResource(R.drawable.unknown)
                }
            })
            // **************** Recycler View management

            albumsRecyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            albumsRecyclerView.adapter = albumsRecyclerAdapter

            viewModel.albumsLiveData.observe(viewLifecycleOwner, {
                albumsRecyclerAdapter.albums = it
                albumsRecyclerAdapter.notifyDataSetChanged()
            })


            artistsRecyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            artistsRecyclerView.adapter = artistsRecyclerAdapter

            viewModel.artistsRelationshipsLiveData.observe(viewLifecycleOwner, {
                artistsRecyclerAdapter.artists = it.toMutableList()
                artistsRecyclerAdapter.notifyDataSetChanged()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null

        // send event to listener (in our case : previous Fragment)
        if(viewModel.isArtistInDatabase.value != true) {
            artistToDeleteViewModel.artistToDeleteLiveData.postValue(ArtistToDeleteViewModel.ArtistToDelete(args.artist))
        } else {
            artistToDeleteViewModel.artistToDeleteLiveData.postValue(null)
        }
    }
}