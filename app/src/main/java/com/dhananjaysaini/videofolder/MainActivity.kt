package com.dhananjaysaini.videofolder

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.dhananjaysaini.videofolder.databinding.ActivityMainBinding
import java.io.File


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    companion object {
        lateinit var videoList: ArrayList<Video>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (requestRuntimePermission()) {
            videoList = getAllVideo()
            setFragment(VideosFragment())
        }

    }

    private fun setFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFL, fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }

//FOR REQUEST PERMISSION

     private fun requestRuntimePermission(): Boolean{
        if(ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 14)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 14) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            else
                ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), 14)
        }
    }



    @SuppressLint("Range")
    private fun getAllVideo(): ArrayList<Video> {
        val tempList = ArrayList<Video>()
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.DURATION
        )
        val cursor = this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
            null, null, MediaStore.Video.Media.DATE_ADDED)
        if (cursor != null)
            if (cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()

                    try {
                        val file = File(pathC)
                        val uriC = Uri.fromFile(file)
                        val video = Video(
                            title = titleC, id = idC, folderName = folderC, size = sizeC,
                            path = pathC, duration = durationC, uri = uriC
                        )
                        if (file.exists())
                            tempList.add(video)
                    } catch (e: Exception) {
                    }

                } while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }

    }
