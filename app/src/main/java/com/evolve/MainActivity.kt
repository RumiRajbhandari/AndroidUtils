package com.evolve

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evolve.rosiautils.DialogBuilder
import com.evolve.rosiautils.PictureManager
import com.evolve.rosiautils.TYPE_ERROR
import com.evolve.rosiautils.TYPE_SUCCESS
import com.evolve.rosiautils.checkNetworkAvailability
import com.evolve.rosiautils.loadImage
import com.evolve.rosiautils.showToast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var pictureManager: PictureManager
    lateinit var dialogBuilder: DialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pictureManager = PictureManager(this)

        btn_connect.setOnClickListener {
            println("network state is " + checkNetworkAvailability(this))

        }

        btn_take_photo.setOnClickListener {
            if (pictureManager.hasPermission(this)) {
                openCamera()
            }
        }

        btn_show_dialog.setOnClickListener {
            dialogBuilder = DialogBuilder(this)
                .setTitle("Title")
                .setMessage("This is dialog")
                .setNegativeButton("Cancel") {
                    dialogBuilder.dismiss()
                }
                .setPositiveButton("OK") { showToast("OK Clicked",
                    TYPE_SUCCESS
                ) }
                .create()
                .show()
        }
    }

    private fun openCamera() {
        pictureManager.startCameraIntent(this) { imgPath ->
            loadImage(img, imgPath) {
                if (!it) {
                    showToast("something went wrong", TYPE_ERROR)
                } else
                    showToast("success", TYPE_SUCCESS)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (pictureManager.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        ) openCamera()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        pictureManager.onActivityResult(requestCode, resultCode, data)
    }
}
