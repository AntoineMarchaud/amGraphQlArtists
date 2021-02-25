package com.amarchaud.amgraphqlartist.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.adapter.ArtistsAdapter
import com.amarchaud.amgraphqlartist.databinding.FragmentBookmarksBinding
import com.amarchaud.amgraphqlartist.interfaces.IArtistClickListener
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.amgraphqlartist.viewmodel.BookmarksViewModel
import com.amarchaud.amgraphqlartist.viewmodel.data.ArtistToDeleteViewModel
import com.amarchaud.estats.model.database.AppDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BookmarksFragment : Fragment(), IArtistClickListener {

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
                artistsRecyclerAdapter.setArtist(it)
            })

            artistToDeleteViewModel.artistToDeleteLiveData.observe(viewLifecycleOwner, {
                if (it != null) {
                   viewModel.refresh()
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onArtistClicked(artistEntity: ArtistEntity) {
        findNavController().navigate(
            BookmarksFragmentDirections.actionBookmarksFragmentToArtistDetailFragment(
                artistEntity
            )
        )
    }

    override fun onBookmarkClicked(artistEntity: ArtistEntity) {

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.DeleteEntryTitle)
            .setMessage(R.string.DeleteEntryMessage)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                lifecycleScope.launch {
                    val pos = myDao.getAllBookmarks().indexOf(artistEntity)
                    if (pos >= 0) {
                        myDao.deleteOneBookmark(artistEntity.id)
                        artistsRecyclerAdapter.setArtistWithoutRefresh(myDao.getAllBookmarks())

                        requireActivity().runOnUiThread {
                            artistsRecyclerAdapter.notifyItemRemoved(pos)
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }


}