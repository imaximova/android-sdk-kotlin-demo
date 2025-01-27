/*
 * Copyright (c) 2011 - 2021, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.kotlin.audio_call

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.voximplant.demos.kotlin.audio_call.services.TelecomManager
import com.voximplant.demos.kotlin.services.AuthService
import com.voximplant.demos.kotlin.utils.*
import com.voximplant.demos.kotlin.audio_call.services.AudioCallManager
import com.voximplant.sdk.Voximplant
import com.voximplant.sdk.client.ClientConfig
import java.util.concurrent.Executors

@SuppressLint("StaticFieldLeak")
lateinit var permissionsHelper: PermissionsHelper
lateinit var audioCallManager: AudioCallManager

@SuppressLint("StaticFieldLeak")
lateinit var telecomManager: TelecomManager

class AudioCallApplication : MultiDexApplication(), LifecycleObserver {
    override fun onCreate() {
        super.onCreate()

//        FirebaseApp.initializeApp(applicationContext)

        val client = Voximplant.getClientInstance(
            Executors.newSingleThreadExecutor(),
            applicationContext,
            ClientConfig().also { it.packageName = packageName }
        )

        permissionsHelper = PermissionsHelper(
            applicationContext,
            arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.MANAGE_OWN_CALLS),
        )
        telecomManager = TelecomManager(applicationContext).apply { registerAccount() }

        Shared.notificationHelper =
            NotificationHelper(
                applicationContext,
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager,
                getString(R.string.app_name),
            )
        Shared.fileLogger = FileLogger(this)
        Shared.authService = AuthService(client, applicationContext)
        audioCallManager = AudioCallManager(
            applicationContext,
            client,
        )
        Shared.shareHelper = ShareHelper.also {
            it.init(
                this,
                "com.voximplant.demos.kotlin.audio_call.fileprovider",
            )
        }
        Shared.getResource = GetResource(applicationContext)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        Shared.appInForeground = false
        Log.d(APP_TAG, "AudioCallApplication::onAppBackgrounded")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        Shared.appInForeground = true
        Log.d(APP_TAG, "AudioCallApplication::onAppForegrounded")
    }
}