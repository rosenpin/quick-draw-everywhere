package com.tomer.draw.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.ViewAnimationUtils
import android.widget.Toast
import com.tomer.draw.R


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
fun Context.isAndroidNewerThan(version: Int): Boolean = Build.VERSION.SDK_INT >= version

fun Context.hasPermissions(vararg permissions: String): Boolean {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		return true
	return permissions.none { checkSelfPermission(it) == PackageManager.PERMISSION_DENIED }
}

fun View.circularRevealHide(cx: Int = width / 2, cy: Int = height / 2, radius: Float = -1f, action: Runnable? = null) {
	if (context.isAndroidNewerThan(Build.VERSION_CODES.LOLLIPOP)) {
		val finalRadius =
				if (radius == -1f)
					Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
				else
					radius
		val anim = ViewAnimationUtils
				.createCircularReveal(this, cx, cy, finalRadius, 0f)
		anim.addListener(object : AnimatorListenerAdapter() {
			override fun onAnimationEnd(animation: Animator) {
				super.onAnimationEnd(animation)
				action?.run()
			}
		})
		anim.interpolator = FastOutSlowInInterpolator()
		anim.start()
	} else {
		val fadeOut = ObjectAnimator.ofFloat(this, "alpha", 1f, .1f)
		fadeOut.duration = 300
		fadeOut.addListener(object : Animator.AnimatorListener {
			override fun onAnimationRepeat(animation: Animator?) {
			}
			
			override fun onAnimationEnd(animation: Animator?) {
				action?.run()
			}
			
			override fun onAnimationCancel(animation: Animator?) {
			}
			
			override fun onAnimationStart(animation: Animator?) {
			}
			
		})
		fadeOut.start()
	}
}

fun View.circularRevealShow(cx: Int = width / 2, cy: Int = height / 2, radius: Float = -1f) {
	post {
		if (context.isAndroidNewerThan(Build.VERSION_CODES.LOLLIPOP)) {
			val finalRadius =
					if (radius == -1f)
						Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()
					else
						radius
			val anim = ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, finalRadius)
			anim.interpolator = FastOutSlowInInterpolator()
			anim.start()
		} else {
			val fadeIn = ObjectAnimator.ofFloat(this, "alpha", 0f, 1f)
			fadeIn.duration = 300
			fadeIn.start()
		}
	}
}

fun Activity.askPermissionNoCallBack(vararg permissions: String) {
	ActivityCompat.requestPermissions(this, permissions, 22)
}

fun Activity.askDrawOverPermission() {
	val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
			Uri.parse("package:" + packageName))
	intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
	if (doesIntentExist(intent)) {
		startActivity(intent)
	} else {
		Toast.makeText(this, R.string.error_1_open_draw_over, Toast.LENGTH_LONG).show()
	}
}

fun Context.doesIntentExist(intent: Intent): Boolean {
	val mgr = packageManager
	val list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
	return list.size > 0
}

fun Context.canDrawOverlaysCompat(): Boolean {
	if (isAndroidNewerThan(Build.VERSION_CODES.M))
		return Settings.canDrawOverlays(this)
	return true
}

fun Context.safeUnregisterReceiver(receiver: BroadcastReceiver) {
	try {
		unregisterReceiver(receiver)
	} catch (ignored: IllegalArgumentException) {
	}
}
