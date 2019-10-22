package com.evolve.rosiautils

import android.app.AlertDialog
import android.content.Context

class DialogBuilder(var context: Context) {
    private var dialog: AlertDialog? = null
    private val builder = AlertDialog.Builder(context)

    fun create(): DialogBuilder {
        dialog = builder.create()
        return this
    }

    fun setTitle(title: String?): DialogBuilder {
        builder.setTitle(title)
        return this
    }

    fun setMessage(message: String?): DialogBuilder {
        builder.setMessage(message)
        return this
    }

    fun setCancelable(isCancelable: Boolean = false): DialogBuilder {
        builder.setCancelable(isCancelable)
        return this
    }

    fun setPositiveButton(buttonName: String, onPositiveButtonClicked: () -> Unit): DialogBuilder {
        builder.setPositiveButton(buttonName) { _, _ -> onPositiveButtonClicked() }
        return this
    }

    fun setNegativeButton(buttonName: String, onNegativeButtonClicked: () -> Unit): DialogBuilder {
        builder.setNegativeButton(buttonName) { _, _ -> onNegativeButtonClicked() }
        return this
    }

    fun show(): DialogBuilder {
        dialog?.show()
        return this
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}