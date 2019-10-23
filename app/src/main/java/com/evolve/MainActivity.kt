package com.evolve

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evolve.rosiautils.DialogBuilder
import com.evolve.rosiautils.TYPE_ERROR
import com.evolve.rosiautils.TYPE_SUCCESS
import com.evolve.rosiautils.checkNetworkAvailability
import com.evolve.rosiautils.showToast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var dialogBuilder: DialogBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_connect.setOnClickListener {
            if (checkNetworkAvailability(this))
                showToast("Connected to network", TYPE_SUCCESS)
            else
                showToast("Please turn on wifi", TYPE_ERROR)

        }

        btn_take_photo.setOnClickListener {
            startActivity(ImageActivity.getIntent(this))
        }

        btn_show_dialog.setOnClickListener { showDialog() }

        btn_show_pie.setOnClickListener {
            startActivity(PieActivity.getIntent(this))

        }

        btn_date_time.setOnClickListener {
            startActivity(DateTimePickerActivity.getIntent(this))

        }
    }

    private fun showDialog() {
        dialogBuilder = DialogBuilder(this)
            .setTitle("Title")
            .setMessage("This is dialog")
            .setNegativeButton("Cancel") {
                dialogBuilder.dismiss()
            }
            .setPositiveButton("OK") {
                showToast(
                    "OK Clicked",
                    TYPE_SUCCESS
                )
            }
            .create()
            .show()
    }
}
