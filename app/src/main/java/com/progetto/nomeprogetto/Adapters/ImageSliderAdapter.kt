package com.progetto.nomeprogetto.Adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.progetto.nomeprogetto.R

class ImageSliderAdapter(private val context: Context, private var images: List<Bitmap>) : PagerAdapter() {

    fun updateImages(newImages: List<Bitmap>) {
        images = newImages
        notifyDataSetChanged()
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_image_slider, container, false)
        val imageView = view.findViewById<ImageView>(R.id.imageView_slider_image)
        imageView.setImageBitmap(images[position])
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }
}
