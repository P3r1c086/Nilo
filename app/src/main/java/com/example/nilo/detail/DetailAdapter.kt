package com.example.nilo.detail

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.nilo.R
import com.google.firebase.storage.StorageReference

class DetailAdapter(private val imgList: MutableList<StorageReference>, private val context: Context) :
    PagerAdapter() {

    override fun getCount(): Int = imgList.size

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imgProduct = ImageView(context)
        GlideApp.with(context)
            .load(imgList[position])
            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_access_time)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(imgProduct)
        //agregar el imageView a la vista
        container.addView(imgProduct, 0)
        return imgProduct
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as ImageView)
    }
}