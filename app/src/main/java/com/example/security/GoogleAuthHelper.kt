package com.example.security

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

object GoogleAuthHelper {
    private const val RC_SIGN_IN = 9001

    fun getSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    fun handleSignInResult(data: Intent?, callback: (Boolean) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            // Sign in successful
            callback(true)
        } catch (e: ApiException) {
            // Sign in failed
            callback(false)
        }
    }
}
