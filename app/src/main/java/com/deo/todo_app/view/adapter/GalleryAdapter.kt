package com.deo.todo_app.view.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.deo.todo_app.R
import com.deo.todo_app.model.Attachment
import com.deo.todo_app.model.Gallery
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import java.io.File

class GalleryAdapter(private val context: Context, private val galleryList:List<Gallery>, private val onClickListener:(Gallery) -> Unit):RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val thumbnail = itemView.findViewById<RoundedImageView>(R.id.thumbnail)
        val videoIcon = itemView.findViewById<ImageView>(R.id.videoIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_gallery,parent,false))
    }

    override fun getItemCount(): Int {
        return galleryList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gallery = galleryList[position]

        when (gallery.type) {
            "image" -> {
                Glide.with(context)
                    .load(gallery.localPath)
                    .into(holder.thumbnail)
                holder.videoIcon.visibility = View.GONE
            }
            "video" -> {
                Glide.with(context)
                    .load(gallery.localPath)
                    .into(holder.thumbnail)
                holder.videoIcon.visibility = View.VISIBLE
            }
        }

        holder.itemView.setOnClickListener {
            onClickListener(gallery)
        }

    }
}