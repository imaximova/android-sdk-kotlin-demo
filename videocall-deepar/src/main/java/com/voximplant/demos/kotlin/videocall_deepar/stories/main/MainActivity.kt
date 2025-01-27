package com.voximplant.demos.kotlin.videocall_deepar.stories.main

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.voximplant.demos.kotlin.videocall_deepar.R
import com.voximplant.demos.kotlin.videocall_deepar.stories.call.CallActivity
import com.voximplant.demos.kotlin.videocall_deepar.stories.login.LoginActivity
import com.voximplant.demos.kotlin.utils.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity<MainViewModel>(MainViewModel::class.java) {
    private var permissionsRequestCompletion: (() -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val reducer = AnimatorInflater.loadAnimator(this.applicationContext, R.animator.reduce_size)
        val increaser =
            AnimatorInflater.loadAnimator(this.applicationContext, R.animator.regain_size)

        call_to.setText(OUTGOING_USERNAME.getStringFromPrefs(applicationContext).orEmpty())

        start_call_button.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) animate(view, reducer)
            if (motionEvent.action == MotionEvent.ACTION_UP) animate(view, increaser)
            false
        }

        logout_button.setOnClickListener {
            model.logout()
        }

        shareLogMainButton.setOnClickListener {
            Shared.shareHelper.shareLog(this)
        }

        start_call_button.setOnClickListener {
            call_to.text.toString().saveToPrefs(applicationContext, OUTGOING_USERNAME)
            permissionsRequestCompletion = {
                model.call(call_to.text.toString())
            }
            requestPermissions()
        }

        model.displayName.observe(this, {
            logged_in_label.text = it
        })

        model.moveToCall.observe(this, {
            Intent(this, CallActivity::class.java).also {
                it.putExtra(IS_INCOMING_CALL, false)
                startActivity(it)
            }
        })

        model.moveToLogin.observe(this, {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
            }
        })
    }

    override fun onBackPressed() {}

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty()) {
            for (grantResult in grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    return  // no permission
                }
            }
            permissionsRequestCompletion?.invoke()
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO
                ),
                1
            )
        } else {
            // Permission has already been granted
            permissionsRequestCompletion?.invoke()
        }
    }

    private fun animate(view: View, animator: Animator) {
        animator.setTarget(view)
        animator.start()
    }

    companion object {
        internal const val OUTGOING_USERNAME = "outgoing_username"
    }
}