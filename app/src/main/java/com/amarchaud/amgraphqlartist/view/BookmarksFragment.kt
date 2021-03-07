package com.amarchaud.amgraphqlartist.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.adapter.ArtistsAdapter
import com.amarchaud.amgraphqlartist.databinding.FragmentBookmarksBinding
import com.amarchaud.amgraphqlartist.interfaces.IArtistClickListener
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.amarchaud.amgraphqlartist.viewmodel.BookmarksViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookmarksFragment : Fragment(), IArtistClickListener {

    companion object {
        const val TAG = "BookMarksFragment"
    }

    private var _binding: FragmentBookmarksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarksViewModel by viewModels()

    // recycler view
    private var artistsRecyclerAdapter = ArtistsAdapter(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(ArtistDetailFragment.TAG) { _, bundle ->
            val result : ArtistApp? = bundle.getParcelable(ArtistDetailFragment.ARTIST_TO_DELETE)
            result?.let { viewModel.refresh() }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onArtistClicked(artistApp: ArtistApp) {
        findNavController().navigate(
            BookmarksFragmentDirections.actionBookmarksFragmentToArtistDetailFragment(
                artistApp
            )
        )
    }

    override fun onBookmarkClicked(artistApp: ArtistApp) {

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.DeleteEntryTitle)
            .setMessage(R.string.DeleteEntryMessage)
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                lifecycleScope.launch {
                    viewModel.deleteBookmark(artistApp)
                }
            }
            .setNegativeButton(android.R.string.cancel) { dialog, which ->
                viewModel.refresh()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }


}