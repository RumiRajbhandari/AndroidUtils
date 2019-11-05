package com.evolve

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import com.evolve.rosiautils.dialog.DialogBuilder
import com.evolve.rosiautils.dialog.DialogProvider
import com.evolve.rosiautils.TYPE_ERROR
import com.evolve.rosiautils.TYPE_SUCCESS
import com.evolve.rosiautils.biometric.BioMetricManager
import com.evolve.rosiautils.checkNetworkAvailability
import com.evolve.rosiautils.showToast
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
                // and call bioMetricManager?.startFingerprintEnrollment(this)
                // instead of calling showDefaultDialog function
                bioMetricManager?.showDefaultDialog( "Android Utils", "Your device supports fingerprint authentication. Would you like to setup it for faster login?")
            }
        } else {
            Toast.makeText(this, "Biometric not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAuthenticationSucceeded(
        authenticatedCryptoObject: BiometricPrompt.CryptoObject?,
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
                        bioMetricManager?.startBiometricPrompt("Android Utils","Please put your finger on your fingerprint sensor in order to validate")
                    }
                    .setNegativeButton("No") {
                        dialogBuilder.dismiss()
                    }
                    .setCancelable(false)
                    .create()
                    .show()
        }
    }
}
