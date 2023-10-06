package com.dhananjaysaini.videofolder

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dhananjaysaini.videofolder.MainActivity.Companion.videoList
import com.dhananjaysaini.videofolder.databinding.RenameFieldBinding
import com.dhananjaysaini.videofolder.databinding.VideoFeaturesBinding
import com.dhananjaysaini.videofolder.databinding.VideoViewBinding
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class VideoAdapter(private val context: Context, private var videoList: ArrayList<Video> )
    : RecyclerView.Adapter<VideoAdapter.MyHolder> (){

    private var newPosition = 0
    private lateinit var dialogRF: androidx.appcompat.app.AlertDialog

    class MyHolder (binding: VideoViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.videoName
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = videoList[position].title

        holder.root.setOnLongClickListener{

            val customDialogVF = LayoutInflater.from(context).inflate(R.layout.video_features, holder.root, false)
            val bindingMF = VideoFeaturesBinding.bind(customDialogVF)
            val dialog = MaterialAlertDialogBuilder(context).setView(customDialogVF)
                .create()
            dialog.show()

            bindingMF.rename.setOnClickListener{
                requestPermissionP()
                dialog.dismiss()
                val customDialogRF = LayoutInflater.from(context).inflate(R.layout.rename_field, holder.root, false)
                val bindingRF = RenameFieldBinding.bind(customDialogRF)
                val dialog = MaterialAlertDialogBuilder(context).setView(customDialogRF)
                    .setCancelable(false)
                    .setPositiveButton("Rename"){self, _->
                        val currentFile = File(videoList[position].path)
                        val newName = bindingRF.renameField.text
                        if(newName !=null && currentFile.exists() && newName.toString().isNotEmpty()){
                           val newFile = File(currentFile.parentFile, newName.toString()+ "."+currentFile.extension)
                            if (currentFile.renameTo(newFile)){
                                MediaScannerConnection.scanFile(context, arrayOf(newFile.toString()), arrayOf("video/*"), null)
                                MainActivity.videoList[position].title = newName.toString()
                                MainActivity.videoList[position].path = newFile.path
                                MainActivity.videoList[position].uri = Uri.fromFile(newFile)
                                notifyItemChanged(position)

                            }
                            else {
                                Toast.makeText(context, "Permission Denied!!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        self.dismiss()
                    }
                    .setNegativeButton("Cancel"){self, _->
                        self.dismiss()
                    }
                    .create()
                dialog.show()
                bindingRF.renameField.text =SpannableStringBuilder(videoList[position].title)
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(
                    MaterialColors.getColor(context, R.attr.themeColor,Color.MAGENTA )
                )
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(
                    MaterialColors.getColor(context, R.attr.themeColor,Color.MAGENTA )
                )
            }

            bindingMF.share.setOnClickListener{
                dialog.dismiss()
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "video/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoList[position].path))
                ContextCompat.startActivity(context, Intent.createChooser(shareIntent, "Share The Video"), null)
            }

            bindingMF.delete.setOnClickListener{
                requestPermissionP()
                dialog.dismiss()
                val dialogDF = MaterialAlertDialogBuilder(context)
                    .setTitle("Delete Video?")
                    .setMessage(videoList[position].title)
                    .setPositiveButton("Yes"){self, _->
                        val file = File(videoList[position].path)
                        if (file.exists() && file.delete()){
                            MediaScannerConnection.scanFile(context, arrayOf(file.path), arrayOf("video/*"), null)
                            MainActivity.videoList.removeAt(position)
                            notifyDataSetChanged()
                        }
                        else{
                            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        }
                        self.dismiss()
                    }
                    .setNegativeButton("No"){self, _->
                        self.dismiss()
                    }
                    .create()
                dialogDF.show()
                dialogDF.getButton(AlertDialog.BUTTON_POSITIVE).setBackgroundColor(
                    MaterialColors.getColor(context, R.attr.themeColor,Color.MAGENTA )
                )
                dialogDF.getButton(AlertDialog.BUTTON_NEGATIVE).setBackgroundColor(
                    MaterialColors.getColor(context, R.attr.themeColor,Color.MAGENTA )
                )
            }
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
       return videoList.size
    }

    private fun requestPermissionP(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if(!Environment.isExternalStorageManager()){
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse("package:${context.applicationContext.packageName}")
                    ContextCompat.startActivity(context, intent, null)
                }
            }

    }
}

