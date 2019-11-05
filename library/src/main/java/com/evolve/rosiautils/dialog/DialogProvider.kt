package com.evolve.rosiautils.dialog

import android.content.Context

class DialogProvider private constructor(context: Context): DialogBuilder() {

    init {
       setContext(context)
    }

    companion object {
        fun getInstance(context: Context): DialogProvider {
            return DialogProvider(context)
        }
    }
}