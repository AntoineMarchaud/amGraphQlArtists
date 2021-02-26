package com.amarchaud.amgraphqlartist.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amarchaud.amgraphqlartist.R
import com.amarchaud.amgraphqlartist.adapter.ArtistsAdapter
import com.amarchaud.amgraphqlartist.databinding.FragmentArtistsBinding
import com.amarchaud.amgraphqlartist.interfaces.IArtistClickListener
import com.amarchaud.amgraphqlartist.model.entity.ArtistEntity
import com.amarchaud.amgraphqlartist.viewmodel.ArtistsViewModel
import com.amarchaud.estats.model.database.AppDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ArtistsFragment : Fragment(), IArtistClickListener {

    companion object {
        const val DEBOUNCE_DELAY = 500L // in milli
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

    private var mJobDebounce: Job? = null
    private var mStoredQuery: String? = null
    private var searchView: SearchView? = null // will be setted in onPrepareOptionsMenu

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        setHasOptionsMenu(true)

        artistsRecyclerAdapter.myDao = myDao

        _binding = FragmentArtistsBinding.inflate(inflater)
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        _binding?.let {
            outState.putString(SAVED_SEARCH, mStoredQuery)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.artistViewModel = viewModel
        binding.lifecycleOwner = this

        with(binding) {

            toggleEmptyState(true)

            savedInstanceState?.let {
                val query = it.getString(SAVED_SEARCH)
                query?.let { queryString ->
                    viewModel.setSearchQuery(queryString)
                }
            }

            mainSwipeRefresh.setOnRefreshListener {
                if (viewModel.currentArtistSearched.isBlank()) {
                    mainSwipeRefresh.isRefreshing = false
                    return@setOnRefreshListener
                }
                viewModel.onRefresh()
            }

            artistsRecyclerView.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            artistsRecyclerView.adapter = artistsRecyclerAdapter

            artistsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)) {
                        // bottom of the list !
                        // call next artists
                        viewModel.onRefresh(true)
                    }
                }
            })

            viewModel.listOfArtistsLiveData.observe(viewLifecycleOwner, {

                mainSwipeRefresh.isRefreshing = false

                if (it.isEmpty()) {
                    //set empty state
                    toggleEmptyState(true)
                } else {
                    toggleEmptyState(false)
                    artistsRecyclerAdapter.setArtist(it)
                }
            })
        }
    }

    private fun toggleEmptyState(state: Boolean) {
        with(binding) {
            if (state) {
                mainSwipeRefresh.visibility = View.GONE
                mainLocationsEmptyGlyph.visibility = View.VISIBLE

            } else {
                mainSwipeRefresh.visibility = View.VISIBLE
                mainLocationsEmptyGlyph.visibility = View.GONE
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // *** Menu management
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu) // ajout de la loupe
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_location_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        menu.findItem(R.id.menu_item_location_search)?.let {
            (it.actionView as? SearchView).let { searchView ->
                searchView?.let { view ->

                    this.searchView = searchView
                    // to make sure it occupies the entire screen width as possible.
                    view.maxWidth = Integer.MAX_VALUE

                    // prepare a debounce to not call api all the time
                    setupQueryInputWatcher(view)
                }
            }
        }
    }


    private fun launchDebounce(query: String) = GlobalScope.launch {
        delay(DEBOUNCE_DELAY)
        viewModel.setSearchQuery(query)
    }


    @SuppressLint("CheckResult")
    private fun setupQueryInputWatcher(searchView: SearchView) {

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                if (mStoredQuery != query) {
                    mJobDebounce?.cancel()
                    mJobDebounce = launchDebounce(query)
                }
                mStoredQuery = query
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                if (mStoredQuery != query) {
                    mJobDebounce?.cancel()
                    mJobDebounce = launchDebounce(query)
                }
                mStoredQuery = query
                return true
            }
        })
    }

    override fun onArtistClicked(artistEntity: ArtistEntity) {
        findNavController().navigate(
            ArtistsFragmentDirections.actionArtistsFragmentToArtistDetailFragment(
                artistEntity
            )
        )
    }

    override fun onBookmarkClicked(artistEntity: ArtistEntity) {
        GlobalScope.launch {
            if (myDao.getOneBookmark(artistEntity.id) == null) {
                myDao.insert(artistEntity)
            } else {
                myDao.delete(artistEntity)
            }
        }
    }
}