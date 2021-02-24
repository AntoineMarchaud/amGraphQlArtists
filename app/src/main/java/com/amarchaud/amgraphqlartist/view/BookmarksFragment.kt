package com.amarchaud.amgraphqlartist.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarchaud.amgraphqlartist.adapter.ArtistsAdapter
import com.amarchaud.amgraphqlartist.databinding.FragmentBookmarksBinding
import com.amarchaud.amgraphqlartist.viewmodel.BookmarksViewModel
import com.amarchaud.amgraphqlartist.viewmodel.data.ArtistToDeleteViewModel
import com.amarchaud.estats.model.database.AppDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BookmarksFragment : Fragment() {

    companion object {
        const val TAG = "BookMarksFragment"
    }

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarksViewModel by viewModels()

    @Inject
    lateinit var myDao: AppDao

    // recycler view
    private var artistsRecyclerAdapter = ArtistsAdapter(this)

    // special viewModel
    private val artistToDeleteViewModel: ArtistToDeleteViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        artistsRecyclerAdapter.myDao = myDao

        _binding = FragmentBookmarksBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bookmarksViewModel = viewModel
        binding.lifecycleOwner = this

        with(binding) {
            artistsRecyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            artistsRecyclerView.adapter = artistsRecyclerAdapter

            viewModel.artistsBookmarkedLiveData.observe(viewLifecycleOwner, {
                artistsRecyclerAdapter.artists = it.toMutableList()
                artistsRecyclerAdapter.notifyDataSetChanged()
            })

            artistToDeleteViewModel.artistToDeleteLiveData.observe(viewLifecycleOwner, {
                if(it != null) {
                    val posToDelete = artistsRecyclerAdapter.artists.indexOf(it.artist)
                    if (posToDelete >= 0) {
                        artistsRecyclerAdapter.artists.removeAt(posToDelete)
                        artistsRecyclerAdapter.notifyItemRemoved(posToDelete)
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}