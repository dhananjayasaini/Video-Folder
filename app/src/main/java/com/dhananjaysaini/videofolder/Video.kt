package com.dhananjaysaini.videofolder

import android.net.Uri
import java.time.Duration

data class Video(val id: String, var title: String, val duration: Long,
                 val folderName: String, val size: String, var path: String, var uri: Uri)
