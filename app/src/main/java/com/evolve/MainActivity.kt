package com.evolve

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.evolve.rosiautils.*
import com.evolve.rosiautils.dialog.DialogBuilder
import com.evolve.rosiautils.dialog.DialogProvider
import com.evolve.rosiautils.biometric.BioMetricManager
import com.evolve.rosiautils.biometric.BiometricCryptoObject
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BioMetricManager.BiometricCallback {

    private lateinit var dialogBuilder: DialogBuilder
    private var bioMetricManager: BioMetricManager?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bioMetricManager = BioMetricManager.getInstance(this)
        bioMetricManager?.setCallback(this)

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

        btn_show_biometric.setOnClickListener {
            initBiometric()
        }
    }

    private fun initBiometric(){
        val isBioMetricSupported = bioMetricManager?.checkIfDeviceSupportsFingerPrint() as Boolean
        if (isBioMetricSupported) {
            val isFingerPrintAlreadyEnrolled =
                bioMetricManager?.isFingerPrintAlreadyEnrolled() as Boolean
            if (isFingerPrintAlreadyEnrolled) {
                showBiometricDialog()
            } else {
                // You can create your own dialog or do what ever you want
                // and call bioMetricManager?.startBiometricEnrollment()
                // instead of calling showBiometricEnrollmentDialog function
                bioMetricManager?.showBiometricEnrollmentDialog( "Android Utils", "Your device supports fingerprint authentication. Would you like to setup it for faster login?",
                    onNegativeButtonClicked = {
                        showToast("Negative button clicked", TYPE_INFO)
                    })
            }
        } else {
            Toast.makeText(this, "Biometric not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthenticationSucceeded(
        cryptoObject: BiometricCryptoObject,
        keyToServer: String
    ) {
        Toast.makeText(this, "Authentication Success", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationFailed() {
        Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        Toast.makeText(this, "Authentication Error: $errString", Toast.LENGTH_SHORT).show()
    }

    private fun showDialog() {
        if (!::dialogBuilder.isInitialized) {
            dialogBuilder = DialogProvider.getInstance(this)
        }
        dialogBuilder.setTitle("Title")
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

    private fun showBiometricDialog() {
        bioMetricManager?.let { bm ->
            dialogBuilder =
                bm.getDialogBuilder()
                    .setTitle("Android Utils")
                    .setMessage("Next time, would you like to login faster using your fingerprint?")
                    .setCancelable()
                    .setPositiveButton("Yes") {
                        bioMetricManager?.showBiometricDialog("Android Utils","Please put your finger on your fingerprint sensor in order to validate")
                    }
                    .setNegativeButton("No") {
                        dialogBuilder.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == BioMetricManager.BIOMETRIC_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (resultCode == Activity.RESULT_FIRST_USER) {
                    showBiometricDialog()
                }
            } else {
                if (resultCode == Activity.RESULT_OK) {
                    showBiometricDialog()
                }
            }
        }
    }
}
