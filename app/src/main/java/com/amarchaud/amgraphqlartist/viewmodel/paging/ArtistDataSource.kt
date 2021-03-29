package com.amarchaud.amgraphqlartist.viewmodel.paging


import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.amarchaud.amgraphqlartist.ArtistsQuery
import com.amarchaud.amgraphqlartist.model.app.ArtistApp
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await


class ArtistDataSource(private val artistName: String?, private val apolloClient: ApolloClient) :
    PagingSource<String, ArtistApp>() {

    private var listAfter = mutableListOf<String>()

    override fun getRefreshKey(state: PagingState<String, ArtistApp>): String? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestItemToPosition(anchorPosition)?.cursor
        }
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, ArtistApp> {
        return try {

            if (artistName.isNullOrEmpty()) {
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = null,
                    nextKey = null
                )
            } else {
                val after: String? = params.key
                Log.d("ArtistDataSource", "load artist after : $after")

                val response = apolloClient.query(ArtistsQuery(artistName, after ?: "")).await()

                val listArtists = mutableListOf<ArtistApp>()
                response.data?.search?.artists?.edges?.forEach { edge ->
                    edge?.let { listArtists.add(ArtistApp(it)) }
                }

                if(listAfter.size > 0) {
                    // if there is something at the end
                    val index = listAfter.indexOf(after)
                    if (index > -1 && index != (listAfter.size - 1))
                        listAfter = listAfter.subList(0, index)
                }

                val previous = if (listAfter.size > 0) {
                    listAfter[listAfter.size - 1]
                } else {
                    null
                }


                after?.let { listAfter.add(it) }

                LoadResult.Page(
                    data = listArtists,
                    prevKey = previous,
                    nextKey = listArtists.last().cursor
                )
            }


        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}