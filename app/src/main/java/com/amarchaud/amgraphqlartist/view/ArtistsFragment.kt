package com.amarchaud.amgraphqlartist.view

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.amarchaud.amgraphqlartist.adapter.ArtistsAdapter
import com.amarchaud.amgraphqlartist.databinding.FragmentArtistsBinding
import com.amarchaud.amgraphqlartist.extensions.hideKeyboard
import com.amarchaud.amgraphqlartist.viewmodel.ArtistsViewModel
import com.amarchaud.estats.model.database.AppDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ArtistsFragment : Fragment() {

    companion object {
        const val TAG = "ArtistFragment"
        const val SAVED_SEARCH = "SAVED_SEARCH"
    }

    private var _binding: FragmentArtistsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArtistsViewModel by viewModels()

    @Inject
    lateinit var myDao: AppDao

    // recycler view
    private var artistsRecyclerAdapter = ArtistsAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        artistsRecyclerAdapter.myDao = myDao

        _binding = FragmentArtistsBinding.inflate(inflater)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        _binding?.let {
            outState.putString(SAVED_SEARCH, binding.currentArtist.text.toString())
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.artistViewModel = viewModel
        binding.lifecycleOwner = this

        with(binding) {

            if (savedInstanceState != null) {
                savedInstanceState.getString(SAVED_SEARCH)?.let {
                    currentArtist.text = SpannableStringBuilder(it)
                }
            }

            currentArtist.setOnEditorActionListener { v, actionId, event ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_SEARCH -> {
                        viewModel.onSearch()
                        hideKeyboard()
                        true
                    }
                    else -> false
                }
            }

            searchButton.setOnClickListener {
                viewModel.onSearch()
                hideKeyboard()
            }

            artistsRecyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            artistsRecyclerView.adapter = artistsRecyclerAdapter

            viewModel.listOfArtistsLiveData.observe(viewLifecycleOwner, {
                artistsRecyclerAdapter.artists = it.toMutableList()
                artistsRecyclerAdapter.notifyDataSetChanged()
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}