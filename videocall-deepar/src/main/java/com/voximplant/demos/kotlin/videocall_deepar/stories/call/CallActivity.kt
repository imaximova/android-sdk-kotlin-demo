package com.voximplant.demos.kotlin.videocall_deepar.stories.call

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.voximplant.demos.kotlin.utils.*
import com.voximplant.demos.kotlin.videocall_deepar.R
import com.voximplant.demos.kotlin.videocall_deepar.cameraHelper
import com.voximplant.demos.kotlin.videocall_deepar.stories.call_failed.CallFailedActivity
import com.voximplant.demos.kotlin.videocall_deepar.stories.main.MainActivity
import kotlinx.android.synthetic.main.activity_call.*

class CallActivity : BaseActivity<CallViewModel>(CallViewModel::class.java) {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        local_video_view.setZOrderMediaOverlay(true)

        val reducer = AnimatorInflater.loadAnimator(applicationContext, R.animator.reduce_size)
        val increaser = AnimatorInflater.loadAnimator(applicationContext, R.animator.regain_size)

        mute_button.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) animate(view, reducer)
            if (motionEvent.action == MotionEvent.ACTION_UP) animate(view, increaser)
            false
        }

        audio_button.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) animate(view, reducer)
            if (motionEvent.action == MotionEvent.ACTION_UP) animate(view, increaser)
            false
        }

        video_button.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) animate(view, reducer)
            if (motionEvent.action == MotionEvent.ACTION_UP) animate(view, increaser)
            false
        }

        hangup_button.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) animate(view, reducer)
            if (motionEvent.action == MotionEvent.ACTION_UP) animate(view, increaser)
            false
        }

        mute_button.setOnClickListener {
            model.mute()
        }

        audio_button.setOnClickListener {
            showAudioDeviceSelectionDialog(model.availableAudioDevices)
        }

        video_button.setOnClickListener {
            model.sendVideo()
        }

        hangup_button.setOnClickListener {
            model.hangup()
        }

        local_video_view.setOnClickListener {
            cameraHelper.switchCamera()
        }

        Shared.voximplantCallManager.showLocalVideoView.observe(this, {
            local_video_view.visibility = if (it) View.VISIBLE else View.INVISIBLE
        })

        Shared.voximplantCallManager.showRemoteVideoView.observe(this, {
            remote_video_view.visibility = if (it) View.VISIBLE else View.INVISIBLE
        })

        model.muted.observe(this, { muted ->
            if (muted) {
                mute_button.setBackgroundResource(R.drawable.red_call_option_back)
                mute_button_icon.setImageResource(R.drawable.ic_micoff)
            } else {
                mute_button.setBackgroundResource(R.drawable.normal_call_option_back)
                mute_button_icon.setImageResource(R.drawable.ic_micon)
            }
        })

        model.sendingVideo.observe(this, { sendingVideo ->
            if (sendingVideo) {
                video_button.setBackgroundResource(R.drawable.normal_call_option_back)
                video_button_icon.setImageResource(R.drawable.ic_camon)
            } else {
                video_button.setBackgroundResource(R.drawable.red_call_option_back)
                video_button_icon.setImageResource(R.drawable.ic_camoff)
            }
        })

        Shared.voximplantCallManager.localVideoRenderer.observe(this, { completion ->
            completion(local_video_view)
        })

        Shared.voximplantCallManager.remoteVideoRenderer.observe(this, { completion ->
            completion(remote_video_view)
        })

        model.moveToCallFailed.observe(this, { reason ->
            Intent(this, CallFailedActivity::class.java).also {
                it.putExtra(FAIL_REASON, reason)
                startActivity(it)
            }
        })

        model.moveToMainActivity.observe(this, {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        })

        model.enableVideoButton.observe(this, {
            video_button.isClickable = it
        })

        model.onCreateWithCall(
            intent.getBooleanExtra(IS_INCOMING_CALL, true),
            intent.getBooleanExtra(IS_ONGOING_CALL, false)
        )

    }

    override fun onBackPressed() {}

    private fun showAudioDeviceSelectionDialog(audioDevices: List<String>) {
        AlertDialog.Builder(this).setTitle(R.string.alert_select_audio_device)
            .setItems(audioDevices.toTypedArray()) { _, which ->
                model.selectAudioDevice(audioDevices[which])
            }
            .create()
            .show()
    }

    private fun animate(view: View, animator: Animator) {
        animator.setTarget(view)
        animator.start()
    }
}