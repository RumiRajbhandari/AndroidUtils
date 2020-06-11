package com.evolve.rosiautils

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by krishna on 4/10/18.
 */

class PictureManager(private val host: Any) : PictureCallback {
    private var cameraImagePath: String? = null
    var context: Context = when (host) {
        is Activity -> host
        is androidx.fragment.app.Fragment -> host.context!!
        else -> throw Exception("Host either can be Activity or Fragment")
    }
    var imagePath: ((String) -> Unit?)? = null

    override fun startGalleryIntent(context: Context?, getSavedPath: (String) -> Unit) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        imagePath = getSavedPath
        when (context) {
            is Activity -> context.startActivityForResult(intent, FROM_GALLERY)
            is androidx.fragment.app.Fragment -> context.startActivityForResult(
                    intent,
                    FROM_GALLERY
            )
        }
    }

    override fun startCameraIntent(
            context: Context?,
            fileName: String,
            getSavedPath: (String) -> Unit
    ) {
        imagePath = getSavedPath
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(context?.packageManager!!).let {
            val imageFile = createImageFile(context, createFileName(fileName))
            if (imageFile != null) {
                val uri: Uri = if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.N)) {
                    Uri.fromFile(imageFile)
                } else {
                    println("provider is " + context.packageName)
                    FileProvider.getUriForFile(
                            context,
                            context.packageName + ".provider",
                            imageFile
                    )
                }
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                intent.putExtra(EXTRA_ABSOLUTE_FILE_PATH, imageFile.absolutePath)
                cameraImagePath = imageFile.absolutePath
            }

        }

        when (host) {
            is Activity -> host.startActivityForResult(intent, FROM_CAMERA)
            is androidx.fragment.app.Fragment -> host.startActivityForResult(intent, FROM_CAMERA)
        }
    }

    override fun getImagePathFromUri(context: Context, uri: Uri): String {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // Good job vivo
                if ("5D68-9217".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)!!
                )

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                when (type) {
                    "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path!!
        }// File
        // MediaStore (and general)

        throw Exception("Image not found exception")
    }

    private fun isDocumentUri(context: Context, uri: Uri): Boolean {
        return DocumentsContract.isDocumentUri(context, uri)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(
            context: Context?, uri: Uri?, selection: String?,
            selectionArgs: Array<String>?
    ): String {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor =
                    context?.contentResolver?.query(uri!!, projection, selection, selectionArgs, null)
            var path: String? = null
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(column)
                path = cursor.getString(columnIndex)
            }
            return path.let { path } ?: throw Exception("Image Not Found Exception")
        } finally {
            cursor?.close()
        }
    }

    /**
     * Create image file of given name in the app private folder
     *
     * @param context  {[Context]}
     * @param fileName name of file to be created
     * @return the {[File]} instance
     */
    private fun createImageFile(context: Context, fileName: String): File? {
        var image: File? = null
        // Create an image file name
        val storageDir =
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) // for private folder
        val appDirectory = getDocumentPath(context)
        if (!appDirectory.exists()) {
            if (!appDirectory.mkdir()) return image
        }
        Log.e(TAG, "storageDir: " + storageDir!!.absolutePath)
        try {
            image = File(appDirectory, "$fileName.jpg")
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    /**
     * create file name for the given monitoring of the given project.
     *
     * @return {[String]} obtained after concatenation of timestamp with projectId and serverId
     */
    private fun createFileName(fileName: String = ""): String {
        val timeStamp = Date().time
        return "$fileName$timeStamp"
    }

    private fun getDocumentPath(context: Context): File {
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File(storageDir, context.getString(R.string.app_name))
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode != Activity.RESULT_OK && (requestCode == FROM_CAMERA || requestCode == FROM_GALLERY)) {
            imagePath?.let { notNullImagePath ->
                notNullImagePath("")
            }
            return
        }

        when (requestCode) {
            FROM_GALLERY -> {
                val path = if (host is androidx.fragment.app.Fragment) {
                    getImagePathFromUri(host.context!!, data!!.data!!)
                } else {
                    getImagePathFromUri(host as Activity, data!!.data!!)
                }
                imagePath?.let {
                    imagePath!!(path)
                }

                return
            }
            FROM_CAMERA -> {
                imagePath?.let {
                    imagePath!!(cameraImagePath ?: "file is not available")
                }
            }
        }
    }

    fun hasPermission(context: Context): Boolean {
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
                    println("has permission form activity")
                }
                is androidx.fragment.app.Fragment -> {
                    println(" has permissin from fragment")
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
        return (grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED)
    }

    /**
     * Opens the image from imagePath, rotates it and overwrites the rotated image in the original path
     * Should be called after onActivityResult
     * @param imagePathName location of image to be rotated
     * */
    fun rotateImage(imagePathName: String) {
        try {
            val file = File(imagePathName)
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565
            BitmapFactory.decodeStream(FileInputStream(file.absolutePath), null, options)
            // Calculate inSampleSize
            options.inSampleSize =
                    calculateInSampleSize(options, 720, 1280) //My device pixel resolution
            // Decode bitmap with inSampleSize set
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            val rotatedBitmap = rotateBitmaps(bitmap = bitmap, imageFileLocation = imagePathName)
            val fileOutputStream = FileOutputStream(file)
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fileOutputStream)
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Rotates the bitmaps of image into vertical orientation depending on the image orientation
     * @param bitmap  {[Bitmap]}
     * @param imageFileLocation location of image to be rotated
     * @return the {[Bitmap]} of rotated image
     * */
    private fun rotateBitmaps(bitmap: Bitmap, imageFileLocation: String): Bitmap {
        val exifInterface = androidx.exifinterface.media.ExifInterface(imageFileLocation)
        val orientation = exifInterface.getAttributeInt(
                androidx.exifinterface.media.ExifInterface.TAG_ORIENTATION,
                androidx.exifinterface.media.ExifInterface.ORIENTATION_UNDEFINED
        )
        val matrix = Matrix()
        when (orientation) {
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
            androidx.exifinterface.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(
                    180f
            )
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun calculateInSampleSize(
            ourOption: BitmapFactory.Options,
            imageWidth: Int, imageHeight: Int
    ): Int {
        val height = ourOption.outHeight
        val width = ourOption.outWidth
        var inSampleSize = 1
        if (height > imageHeight || width > imageWidth) {
            inSampleSize = if (width > height) {
                (height.toFloat() / imageHeight.toFloat()).roundToInt()
            } else {
                (width.toFloat() / imageWidth.toFloat()).roundToInt()
            }
        }
        return inSampleSize
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
    }
}

interface PictureCallback {
    fun startGalleryIntent(context: Context?, getSavedPath: (String) -> Unit)
    fun startCameraIntent(context: Context?, fileName: String = "", getSavedPath: (String) -> Unit)

    @Throws(Exception::class)
    fun getImagePathFromUri(context: Context, uri: Uri): String
}
