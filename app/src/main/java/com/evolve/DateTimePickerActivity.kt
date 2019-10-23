package com.evolve

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DateTimePickerActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, DateTimePickerActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_time_picker)
    }
}
