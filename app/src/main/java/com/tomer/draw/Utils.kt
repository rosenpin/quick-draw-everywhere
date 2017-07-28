package com.tomer.draw

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.view.View
import android.view.ViewAnimationUtils

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
fun QuickDrawView.isAndroidNewerThanM(): Boolean {
	return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
}

fun Context.isAndroidNewerThan(version: Int): Boolean = Build.VERSION.SDK_INT >= version

fun Context.hasPermission(permissionName: String): Boolean {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
		return true
	return checkSelfPermission(permissionName) == PackageManager.PERMISSION_GRANTED
}

fun View.circularRevealHide(cx: Int = width / 2, cy: Int = height / 2, radius: Float = -1f, action: Runnable?) {
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
		anim.start()
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
			anim.duration = 500
			anim.start()
		}
	}
}
