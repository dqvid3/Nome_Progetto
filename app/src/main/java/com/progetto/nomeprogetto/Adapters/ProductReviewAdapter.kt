package com.progetto.nomeprogetto.Adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.progetto.nomeprogetto.Objects.ProductReview
import com.progetto.nomeprogetto.databinding.ReviewViewDesignBinding

class ProductReviewAdapter(private var reviewList: List<ProductReview>) : RecyclerView.Adapter<ProductReviewAdapter.ViewHolder>() {

    class ViewHolder(binding: ReviewViewDesignBinding) : RecyclerView.ViewHolder(binding.root) {
        val ratingBar = binding.ratingBar
        val reviewUsername = binding.reviewUsername
        val reviewComment = binding.reviewComment
        val reviewDate = binding.reviewDate
    }
//
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ReviewViewDesignBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = reviewList[position]

        holder.ratingBar.rating = review.rating.toFloat()
        holder.reviewUsername.text = review.username
        holder.reviewComment.text = review.comment
        holder.reviewDate.text = review.reviewDate.toString()
    }
}