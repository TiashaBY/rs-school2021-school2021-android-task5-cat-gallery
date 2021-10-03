package com.rsschool.catsapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.rsschool.catsapp.R
import com.rsschool.catsapp.databinding.CatItemBinding
import com.rsschool.catsapp.model.Cat

class CatsGalleryAdapter(private val listener: OnImageClickListener) :
    PagingDataAdapter<Cat, CatsGalleryAdapter.CatImageViewHolder>(IMAGE_COMPARATOR) {

    companion object {
        private val IMAGE_COMPARATOR = object : DiffUtil.ItemCallback<Cat>() {
            override fun areItemsTheSame(oldItem: Cat, newItem: Cat): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Cat, newItem: Cat): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(holder: CatImageViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.bind(currentItem)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatImageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = CatItemBinding.inflate(layoutInflater, parent, false)
        return CatImageViewHolder(binding)
    }

    inner class CatImageViewHolder(private val binding: CatItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != -1) {
                    getItem(position)?.let {
                        listener.onItemClick(it)
                    }
                }
            }
        }

        fun bind(cat: Cat) {
            binding.apply {
                Glide.with(itemView).load(cat.url).centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .error(R.drawable.ic_outline_sentiment_very_dissatisfied_24)
                    .into(imageCard)
            }
        }
    }

    interface OnImageClickListener {
        fun onItemClick(image: Cat)
    }
}
