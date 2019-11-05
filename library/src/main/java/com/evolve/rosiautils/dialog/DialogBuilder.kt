package com.evolve.rosiautils.dialog

import android.app.AlertDialog
import android.content.Context

abstract class DialogBuilder {
    private var dialog: AlertDialog? = null
    private lateinit var context: Context
    private val builder : AlertDialog.Builder by  lazy {  AlertDialog.Builder(context)}

    internal fun setContext(context: Context){
        this.context = context
    }
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

    fun setPositiveButton(buttonName: String?, onPositiveButtonClicked: () -> Unit): DialogBuilder {
        builder.setPositiveButton(buttonName) { _, _ -> onPositiveButtonClicked() }
        return this
    }

    fun setNegativeButton(buttonName: String?, onNegativeButtonClicked: () -> Unit): DialogBuilder {
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