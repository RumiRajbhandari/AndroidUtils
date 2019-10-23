package com.evolve

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.evolve.rosiautils.initPieGraph
import kotlinx.android.synthetic.main.activity_pie.*

class PieActivity : AppCompatActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, PieActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pie)
        showPieGraph()
    }

    private fun showPieGraph() {
        initPieGraph(30f, pie_graph)
    }
}
