/*
 * Copyright (c) 2011 - 2021, Zingaya, Inc. All rights reserved.
 */

package com.voximplant.demos.kotlin.audio_call.services

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.voximplant.demos.kotlin.audio_call.R
import com.voximplant.demos.kotlin.audio_call.audioCallManager
import com.voximplant.demos.kotlin.audio_call.permissionsHelper
import com.voximplant.demos.kotlin.utils.APP_TAG

class TelecomManager(private val context: Context) {
    private val telecomManager: TelecomManager =
        context.getSystemService(AppCompatActivity.TELECOM_SERVICE) as TelecomManager

    private fun getAccountHandle(): PhoneAccountHandle {
        val componentName = ComponentName(context, CallConnectionService::class.java)
        return PhoneAccountHandle(componentName, APP_TAG)
    }

    fun registerAccount() {
        val accountHandle = getAccountHandle()
        var phoneAccount = telecomManager.getPhoneAccount(accountHandle)
        if (phoneAccount == null) {
            val builder = PhoneAccount.builder(accountHandle, APP_TAG)
            builder.setCapabilities(PhoneAccount.CAPABILITY_SELF_MANAGED)
            builder.addSupportedUriScheme("sip")
            builder.setIcon(Icon.createWithResource(context, R.drawable.ic_vox_notification))
            phoneAccount = builder.build()
            telecomManager.registerPhoneAccount(phoneAccount)
            if (telecomManager.getPhoneAccount(accountHandle) != null) {
                Log.i(APP_TAG, "TelecomManager::registerAccount: Account registered")
            } else {
                Log.w(APP_TAG, "TelecomManager::registerAccount: Account not registered")
            }
        } else {
            Log.i(APP_TAG, "TelecomManager::registerAccount: Account already exists")
        }
    }

    fun addIncomingCall() {
        val accountHandle = getAccountHandle()
        if (telecomManager.getPhoneAccount(accountHandle) != null) {
            Log.i(APP_TAG, "TelecomManager::addIncomingCall")
            val extras = Bundle()
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
            telecomManager.addNewIncomingCall(accountHandle, extras)
        } else {
            Log.w(
                APP_TAG,
                "TelecomManager::addIncomingCall: Couldn't add incoming call. Account not registered"
            )
            audioCallManager.showIncomingCallUI()
        }
    }

    @SuppressLint("MissingPermission")
    fun addOutgoingCall(userName: String) {
        val accountHandle = getAccountHandle()
        if (telecomManager.getPhoneAccount(accountHandle) != null) {
            Log.i(APP_TAG, "TelecomManager::addOutgoingCall")
            val extras = Bundle()
            extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, accountHandle)
            if (permissionsHelper.allPermissionsGranted()) {
                telecomManager.placeCall(Uri.parse("sip:$userName"), extras)
            }
        } else {
            Log.w(
                APP_TAG,
                "TelecomManager::addIncomingCall: Couldn't add outgoing call. Account not registered"
            )
        }
    }
}