package com.evolve

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evolve.rosiautils.PictureManager
import com.evolve.rosiautils.TYPE_ERROR
import com.evolve.rosiautils.TYPE_SUCCESS
import com.evolve.rosiautils.loadImage
import com.evolve.rosiautils.showToast
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity() {

    private lateinit var pictureManager: PictureManager

    companion object {
        fun getIntent(context: Context) = Intent(context, ImageActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)
        pictureManager = PictureManager(this)

        btn_take_photo.setOnClickListener {
            if (pictureManager.hasPermission(this)) {
                openCamera()
            }
        }
    }

    private fun openCamera() {
        pictureManager.startCameraIntent(this, openFrontCamera = true) { imgPath ->
            loadImage(image, imgPath) {
                if (!it) {
                    showToast("something went wrong", TYPE_ERROR)
                } else
                    showToast("success", TYPE_SUCCESS)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (pictureManager.onRequestPermissionsResult(requestCode, permissions, grantResults)) openCamera()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pictureManager.onActivityResult(requestCode, resultCode, data)
    }
}
