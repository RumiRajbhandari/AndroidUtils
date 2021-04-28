package com.evolve.rosiautils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Created by Nabin on 4/25/2021.
 */

class PictureManager2(private val host: Any) {
    var context: Context = when (host) {
        is Activity -> host
        is Fragment -> host.context!!
        else -> throw Exception("Host either can be Activity or Fragment")
    }

    private lateinit var currentPhotoPath: String
    private var imagePathListener: ((String) -> Unit?)? = null

    fun dispatchTakePictureIntent(
            fileName: String = "",
            imagePathListener: (imagePath: String) -> Unit
    ) {

        this.imagePathListener = imagePathListener
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(context.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(fileName)
                } catch (error: IOException) {
                    // Error occurred while creating the File
                    error.printStackTrace()
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            context,
                            "${BuildConfig.LIBRARY_PACKAGE_NAME}.provider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    when (host) {
                        is Activity -> host.startActivityForResult(takePictureIntent, FROM_CAMERA)
                        is Fragment -> host.startActivityForResult(takePictureIntent, FROM_CAMERA)
                    }
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(fileName: String): File {
        // Create an image file name
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                createFileName(fileName), /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    /**
     * create file name for the given monitoring of the given project.
     *
     * @return String obtained after concatenation of timestamp with projectId and serverId */
    private fun createFileName(fileName: String = ""): String {
        val timeStamp = Date().time
        return "$fileName$timeStamp"
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != AppCompatActivity.RESULT_CANCELED) {
            if (requestCode == FROM_CAMERA) {
                val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
                bitmap.fixRotation(Uri.parse(currentPhotoPath))
                bitmap.recycle()
                if (imagePathListener != null) {
                    imagePathListener!!(currentPhotoPath)
                } else {
                    throw Exception("Image Listener is null")
                }
            }
        }
    }

    private fun Bitmap.fixRotation(uri: Uri): Bitmap? {

        val ei = ExifInterface(uri.path!!)

        val orientation: Int = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(270f)
            ExifInterface.ORIENTATION_NORMAL -> this
            else -> this
        }
    }

    private fun Bitmap.rotateImage(angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
                this, 0, 0, width, height,
                matrix, true
        )
    }

    fun hasPermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
        ) {

            when (host) {
                is Activity -> {
                    ActivityCompat.requestPermissions(
                            host,
                            arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            PICK_FROM_CAMERA
                    )
                    println("has permission from activity")
                }
                is Fragment -> {
                    println(" has permission from fragment")
                    host.requestPermissions(
                            arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ),
                            PICK_FROM_CAMERA
                    )
                }
                else -> println("inside else of has permission")
            }

            false
        } else
            true
    }

    fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ): Boolean {
        return (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
    }

    companion object {
        private const val FROM_GALLERY = 700
        private const val FROM_CAMERA = 800
        private const val EXTRA_ABSOLUTE_FILE_PATH = "absolute-path"
        private const val TAG = "PictureManager"
        private const val PICK_FROM_CAMERA = 100

        fun deleteImageFromDirectory(imagePath: String) {
            try {
                val file = File(imagePath)
                file.delete()
            } catch (error: Exception) {
                error.printStackTrace()
            }
        }

        fun setImage(imageView: ImageView, imagePath: String) {
            Glide.with(imageView.context).load(imagePath).into(imageView)
        }
    }
}
