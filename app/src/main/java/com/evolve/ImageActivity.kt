package com.evolve

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evolve.rosiautils.ImageListenerNullException
import com.evolve.rosiautils.ImagePathNullException
import com.evolve.rosiautils.PictureManager2
import com.evolve.rosiautils.TYPE_ERROR
import com.evolve.rosiautils.loadImage
import com.evolve.rosiautils.showToast
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    private lateinit var pictureManager: PictureManager2

    companion object {
        fun getIntent(context: Context) = Intent(context, ImageActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        pictureManager = PictureManager2(this)

        btn_take_photo.setOnClickListener {
            if (pictureManager.hasPermission()) {
                openCamera()
            }
        }
    }

    private fun openCamera() {
        pictureManager.dispatchTakePictureIntent(imagePathListener = getImageListener())
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (pictureManager.onRequestPermissionsResult(requestCode, permissions, grantResults)) openCamera()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (pictureManager.isImagePathListenerNull()) {
                pictureManager.setListener(getImageListener())
            }
            pictureManager.onActivityResult(requestCode, resultCode, data)
        } catch (e: Exception) {
            when (e) {
                is ImagePathNullException -> {
                    showToast(e.message, TYPE_ERROR)
                }
                is ImageListenerNullException -> {
                    showToast(e.message, TYPE_ERROR)
                }
                else -> {
                    showToast(e.localizedMessage, TYPE_ERROR)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pictureManager.setListener(getImageListener())
    }

    private fun getImageListener(): (String) -> Unit {
        return { imgPath ->
            loadImage(image, imgPath)
        }
    }
}
