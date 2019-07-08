package com.deepak.fingerprintauth

import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.TextView
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.security.*
import java.security.cert.CertificateException
import java.util.jar.Manifest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    val KEY_NAME = "fingerPrint"
    lateinit var keyStore: KeyStore
    lateinit var cipher: Cipher
    companion object {
        lateinit var textView: TextView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.errorText)

        var keyguardManager : KeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        var fingerPrintManager : FingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

        if (!fingerPrintManager.isHardwareDetected){
            errorText.text = "Your Device does not have a Fingerprint Sensor"
        } else {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                textView.text = "Fingerprint authentication permission not enabled"
            } else {
                if (!keyguardManager.isKeyguardSecure){
                    textView.text = "Lock screen security not enabled in Settings"
                } else {
                    generateKey()

                    if (cipherInit()) {
                        var cryptObject = FingerprintManager.CryptoObject(cipher)
                        var helper = FingerprintHandler(this)
                        helper.startAuth(fingerPrintManager, cryptObject)
                    }
                }
            }
        }
    }

    fun generateKey(){
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        lateinit var keyGenerator : KeyGenerator
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }

        try {
            keyStore.load(null)
            keyGenerator.init(KeyGenParameterSpec.Builder(KEY_NAME,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build())
            keyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException){
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException){
            throw RuntimeException(e)
        } catch (e: CertificateException){
            throw RuntimeException(e)
        } catch (e: IOException){
            throw RuntimeException(e)
        }
    }

    fun cipherInit (): Boolean{
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            keyStore.load(null)
            var key : SecretKey = keyStore.getKey(KEY_NAME, null) as SecretKey
            cipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyStoreException){
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: CertificateException){
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to get Cipher", e)
        }
    }
}