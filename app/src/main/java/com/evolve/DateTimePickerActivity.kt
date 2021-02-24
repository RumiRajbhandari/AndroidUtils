package com.evolve

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_date_time_picker.*

class DateTimePickerActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, DateTimePickerActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_date_time_picker)
        date_selection_view2.setAllOwToPickPastDate(true)
        btn_reset_date.setOnClickListener {
            date_selection_view2.resetToCurrentDate()
        }
        time_selection_view2.setMinMaxTime(true, 9, 17)
    }
}
