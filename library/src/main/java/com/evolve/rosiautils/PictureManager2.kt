package com.evolve.rosiautils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import java.io.*
import java.util.*
import kotlin.jvm.Throws

/**
 * Created by Nabin on 4/25/2021.
 */

class PictureManager2(private val host: Any) {
    var context: Context = when (host) {
        is Activity -> host
        is Fragment -> host.requireContext()
        else -> throw Exception("Host either can be Activity or Fragment")
    }

    private lateinit var currentPhotoPath: String
    private var imagePathListener: ((String) -> Unit?)? = null

    fun setListener(imagePathListener: ((String) -> Unit?)?) {
        this.imagePathListener = imagePathListener
    }

    fun dispatchTakePictureIntent(
        fileName: String = "",
        openFrontCamera: Boolean = false,
        imagePathListener: (imagePath: String) -> Unit
    ) {

        this.imagePathListener = imagePathListener
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (openFrontCamera && hasFrontCamera()) {
        // TODO test why facing front camera is not working? Why Why Why ????????????
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> {
                    takePictureIntent.putExtra(
                        "android.intent.extras.CAMERA_FACING",
                        CameraCharacteristics.LENS_FACING_FRONT
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    takePictureIntent.putExtra(
                        "android.intent.extras.CAMERA_FACING",
                        CameraCharacteristics.LENS_FACING_FRONT
                    )
                    takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                }
                else -> takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", 1)
            }
        }

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
                "${context.packageName}.provider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            when (host) {
                is Activity -> host.startActivityForResult(takePictureIntent, FROM_CAMERA)
                is Fragment -> host.startActivityForResult(takePictureIntent, FROM_CAMERA)
            }
        }
    }

    fun getCurrentPhotoPath() = currentPhotoPath

    fun setCurrentPhotoPath(currentPhotoPath: String) {
        this.currentPhotoPath = currentPhotoPath
    }

    private fun hasFrontCamera(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
    }

    //    TODO find how to create file in optimized way
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
                imagePathListener?.let {
                    it(currentPhotoPath)
                }.orElse {
                    throw Exception("Image Listener is null")
                }
            }
        }
    }

    /**
     * See Example:
     *  https://gist.github.com/nawinkhatiwada/1e70a214837dfc826cecf71613077581
     * */
    @Deprecated("Handle Image rotation by using glide `transform` method.")
    fun rotateImageIfRequired(imagePath: String, height: Int, width: Int) {
        val uri = Uri.parse(imagePath)
        if (getOrientation(uri) != ExifInterface.ORIENTATION_NORMAL) {
            val byteArray = streamToByteArray(FileInputStream(imagePath))
            val bitmap = decodeSampledByteArrayFromResource(byteArray, height, width)
            bitmap.fixRotation(uri)
        }
    }

    @Throws(IOException::class)
    private fun streamToByteArray(stream: InputStream): ByteArray {
        val buffer = ByteArray(1024)
        val os = ByteArrayOutputStream()
        var line = 0
        // read bytes from stream, and store them in buffer
        while (line != -1) {
            // Writes bytes from byte array (buffer) into output stream.
            os.write(buffer, 0, line)
            line = stream.read(buffer)
        }
        stream.close()
        os.flush()
        os.close()
        return os.toByteArray()
    }

    private fun decodeSampledByteArrayFromResource(
        byteArray: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
//            BitmapFactory.decodeResource(res, resId, this)
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false
            BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
//            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    fun getFileSize(imagePath: String): Int {
        val file = File(imagePath)
        return (file.length() / 1024).toString().toInt()
    }

    private fun Bitmap.fixRotation(uri: Uri): Bitmap? {
        return when (getOrientation(uri)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(270f)
            ExifInterface.ORIENTATION_NORMAL -> this
            else -> this
        }
    }

    private fun getOrientation(uri: Uri): Int {
        val ei = ExifInterface(uri.path!!)
        return ei.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
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

        fun deletePhotos(context: Context?) {
            val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            storageDir?.let {
                if (it.exists())
                    storageDir.deleteRecursively()
            }
        }

        fun setImage(imageView: ImageView, imagePath: String) {
            Glide.with(imageView.context).load(imagePath).into(imageView)
        }
    }
}