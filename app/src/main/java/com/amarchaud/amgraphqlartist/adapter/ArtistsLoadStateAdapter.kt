package com.amarchaud.amgraphqlartist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.amarchaud.amgraphqlartist.databinding.ItemPagingStateBinding

class ArtistsLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<ArtistsLoadStateAdapter.PassengerLoadStateViewHolder>() {

    inner class PassengerLoadStateViewHolder(
        private val binding: ItemPagingStateBinding,
        private val retry: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState) {

            if (loadState is LoadState.Error) {
                binding.textViewError.text = loadState.error.localizedMessage
            }
            binding.progressbar.visibility =
                if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
            binding.buttonRetry.visibility =
                if (loadState is LoadState.Error) View.VISIBLE else View.GONE
            binding.textViewError.visibility =
                if (loadState is LoadState.Error) View.VISIBLE else View.GONE
            binding.buttonRetry.setOnClickListener {
                retry()
            }
        }
    }

    override fun onBindViewHolder(holder: PassengerLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ) = PassengerLoadStateViewHolder(
        ItemPagingStateBinding.inflate(LayoutInflater.from(parent.context), parent, false), retry
    )
}