package com.deepak.fingerprintauth

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import androidx.core.app.ActivityCompat
import androidx.core.os.CancellationSignal
import android.provider.SyncStateContract.Helpers.update
import android.R
import android.R.attr.colorPrimaryDark
import androidx.core.content.ContextCompat
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.widget.TextView
import android.provider.SyncStateContract.Helpers.update
import android.provider.SyncStateContract.Helpers.update
import android.provider.SyncStateContract.Helpers.update
import org.w3c.dom.Text


class FingerprintHandler : FingerprintManager.AuthenticationCallback {
    var context: Context

    constructor(mContext: Context) {
        context = mContext
    }

    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        var cancellationSignal = android.os.CancellationSignal()
        if(ActivityCompat.checkSelfPermission(context, android.Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
            return
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null)
    }

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        this.update("Fingerprint Authentication error\n$errString", false)
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        this.update("Fingerprint Authentication help\n$helpString", false)
    }

    override fun onAuthenticationFailed() {
        this.update("Fingerprint Authentication failed.", false)
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        this.update("Fingerprint Authentication succeeded.", true)
    }

    fun update(e: String, success: Boolean){
        MainActivity.textView.text = e

        if (success) {
            MainActivity.textView.setTextColor(Color.parseColor("#FFFFFF"))
            context.startActivity(Intent(context, HomeActivity::class.java))
        }
    }
}