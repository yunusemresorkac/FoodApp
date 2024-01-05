package com.app.orderfood.adapter.seller

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.orderfood.R

class GalleryAdapter(private val imageList: MutableList<Bitmap>) :
    RecyclerView.Adapter<GalleryAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val txtDelete: ImageView = itemView.findViewById(R.id.deleteBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageBitmap = imageList[position]

        holder.imageView.setImageBitmap(imageBitmap)
        holder.txtDelete.setOnClickListener {
            imageList.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}