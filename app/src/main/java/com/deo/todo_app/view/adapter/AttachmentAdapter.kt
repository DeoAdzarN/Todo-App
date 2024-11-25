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
import com.google.firebase.storage.FirebaseStorage
import com.makeramen.roundedimageview.RoundedImageView
import java.io.File

class AttachmentAdapter(private val context: Context, private val attachments:List<Attachment>,private val onRemove:(Attachment,Int) -> Unit, private val onClickListener:(Attachment) -> Unit):RecyclerView.Adapter<AttachmentAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val thumbnail = itemView.findViewById<RoundedImageView>(R.id.thumbnail)
        val videoIcon = itemView.findViewById<ImageView>(R.id.videoIcon)
        val remove = itemView.findViewById<ImageView>(R.id.remove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item_attachment,parent,false))
    }

    override fun getItemCount(): Int {
        return attachments.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val attachment = attachments[position]

        when (attachment.type) {
            "image" -> {
                Glide.with(context)
                    .load(attachment.path)
                    .into(holder.thumbnail)
                holder.videoIcon.visibility = View.GONE
            }
            "video" -> {
                Glide.with(context)
                    .load(attachment.path)
                    .into(holder.thumbnail)
                holder.videoIcon.visibility = View.VISIBLE
            }
        }

        holder.itemView.setOnClickListener {
            onClickListener(attachment)
        }

        holder.remove.setOnClickListener {
            onRemove(attachment,position)
        }
    }

    private fun getRealPathFromUri(uri: Uri): String {
        var path = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            val index = it.getColumnIndex(MediaStore.Images.Media.DATA)
            if (index != -1) {
                path = it.getString(index)
            }
            it.close()
        }
        return path
    }
}