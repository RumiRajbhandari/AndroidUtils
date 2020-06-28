package com.evolve.rosiautils.biometric

import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.Mac

data class BiometricCryptoObject(
    val mSignature: Signature?,
    val mCipher: Cipher?,
    val mMac: Mac?
)