package com.evolve.rosiautils.biometric

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt.BIOMETRIC_ERROR_NO_BIOMETRICS
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.hardware.fingerprint.FingerprintManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.evolve.rosiautils.dialog.DialogBuilder
import com.evolve.rosiautils.dialog.DialogProvider
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.*
import java.util.concurrent.Executor

class BioMetricManager private constructor(private val host: Any) {
    private var biometricCallback: BiometricCallback? = null
    private val handler = Handler()
    private val executor = Executor { command -> handler.post(command) }

    private var context: Context = when (host) {
        is Activity -> host
        is Fragment -> host.requireContext()
        else -> throw Exception("The host can be either activity or fragment only.")
    }

    companion object {
        const val BIOMETRIC_REQUEST_CODE = 0xDEAD
        private val KEY_NAME = UUID.randomUUID().toString()
        private var keyToServer = ""
        fun getInstance(host: Any): BioMetricManager {
            return BioMetricManager(host)
        }
    }

    fun setCallback(biometricCallback: BiometricCallback?) {
        this.biometricCallback = biometricCallback
    }

    fun checkIfDeviceSupportsFingerPrint(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bioMetricManager = BiometricManager.from(context)
            val canAuthenticate = bioMetricManager.canAuthenticate()
            if (canAuthenticate == BIOMETRIC_ERROR_NO_BIOMETRICS || canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS)
                return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fingerPrintManager =
                context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager?
                    ?: return false
            if (fingerPrintManager.isHardwareDetected) {
                return true
            }
        }
        return false
    }

    fun isFingerPrintAlreadyEnrolled(): Boolean {
        // Check whether the fingerprint can be used for authentication (Android M to P)
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val fingerprintManagerCompat = FingerprintManagerCompat.from(context)
            fingerprintManagerCompat.hasEnrolledFingerprints()
        } else {
            // Check biometric manager (from Android Q)
            val biometricManager = BiometricManager.from(context)
            biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
        }
    }

    fun getDialogBuilder(): DialogBuilder {
        return DialogProvider.getInstance(context)
    }

    fun startBiometricEnrollment() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val intent = Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                when (host) {
                    is Activity -> host.startActivityForResult(intent, BIOMETRIC_REQUEST_CODE)
                    is Fragment -> host.startActivityForResult(intent, BIOMETRIC_REQUEST_CODE)
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                when (host) {
                    is Activity -> host.startActivityForResult(intent, BIOMETRIC_REQUEST_CODE)
                    is Fragment -> host.startActivityForResult(intent, BIOMETRIC_REQUEST_CODE)
                }
            }
            else -> Toast.makeText(
                context,
                "This device does not support finger print",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun showBiometricDialog(
        title: String,
        description: String,
        negativeButtonName: String = "Dismiss"
    ) {
        val signature: Signature?
        try {
            var keyPair: KeyPair? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyPair = generateKeyPair()
            }
            keyToServer = Base64.encodeToString(keyPair?.public?.encoded, Base64.URL_SAFE)
            signature = initSignature()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        signature?.let {
            showBiometricPrompt(signature, title, description, negativeButtonName)
        }
    }

    @Throws(Exception::class)
    private fun initSignature(): Signature? {
        val keyPair = getKeyPair()
        if (keyPair != null) {
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(keyPair.private)
            return signature
        }
        return null
    }

    @Throws(Exception::class)
    private fun getKeyPair(): KeyPair? {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        if (keyStore.containsAlias(KEY_NAME)) {
            // Get public key
            val publicKey = keyStore.getCertificate(KEY_NAME).publicKey
            // Get private key
            val privateKey = keyStore.getKey(KEY_NAME, null) as PrivateKey
            // Return a key pair
            return KeyPair(publicKey, privateKey)
        }
        return null
    }

    fun showBiometricEnrollmentDialog(title: String, message: String) {
        getDialogBuilder()
            .setTitle(title)
            .setMessage(message)
            .setCancelable()
            .setPositiveButton("Ok") {
                this.startBiometricEnrollment()
            }
            .setNegativeButton("Cancel") {
                getDialogBuilder().dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @Throws(Exception::class)
    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator =
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")

        val builder = KeyGenParameterSpec.Builder(
            KEY_NAME,
            KeyProperties.PURPOSE_SIGN
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(
                KeyProperties.DIGEST_SHA256,
                KeyProperties.DIGEST_SHA384,
                KeyProperties.DIGEST_SHA512
            )
            // Require the user to authenticate with a biometric to authorize every use of the key
            .setUserAuthenticationRequired(true)

        // Generated keys will be invalidated if the biometric templates are added more to user device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.setInvalidatedByBiometricEnrollment(true)
        }

        keyPairGenerator.initialize(builder.build())
        return keyPairGenerator.generateKeyPair()
    }

    private fun showBiometricPrompt(
        signature: Signature,
        title: String,
        description: String,
        negativeButtonName: String = "Dismiss"
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setNegativeButtonText(negativeButtonName)
            .build()

        val biometricPrompt = BiometricPrompt(
            context as FragmentActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    biometricCallback?.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    val authenticatedCryptoObject: BiometricPrompt.CryptoObject? =
                        result.cryptoObject
                    // User has verified the signature, cipher, or message
                    // authentication code (MAC) associated with the crypto object,
                    // so you can use it in your app's crypto-driven workflows.
                    biometricCallback?.onAuthenticationSucceeded(
                        authenticatedCryptoObject,
                        keyToServer
                    )
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    biometricCallback?.onAuthenticationFailed()
                }
            })

        // Displays the "log in" prompt.
        biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(signature))
    }

    interface BiometricCallback {
        fun onAuthenticationSucceeded(
            authenticatedCryptoObject: BiometricPrompt.CryptoObject?,
            keyToServer: String
        )

        fun onAuthenticationFailed()
        fun onAuthenticationError(
            errorCode: Int,
            errString: CharSequence
        )
    }
}