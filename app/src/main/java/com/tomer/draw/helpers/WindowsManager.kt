package com.tomer.draw.helpers

import android.content.Context
import android.graphics.PixelFormat
import android.view.View
import android.view.WindowManager
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.tomer.draw.ui.views.FloatingView
import com.tomer.draw.ui.views.QuickDrawView
import com.tomer.draw.utils.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */

internal class WindowsManager private constructor(context: Context) {
	private val mWindowManager: WindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
	private val viewsMap = ConcurrentHashMap<FloatingView, WindowManager.LayoutParams>()
	fun addView(view: FloatingView) {
		val mParams = WindowManager.LayoutParams(view.origWidth(), view.origHeight(),
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				if (view is QuickDrawView)
					WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM or
							WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
				else WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSPARENT)
		
		mParams.gravity = view.gravity()
		mParams.x = view.currentX
		mParams.y = view.currentY
		viewsMap.put(view, mParams)
		mWindowManager.addView(view as View, mParams)
	}
	
	fun removeView(view: FloatingView) {
		mWindowManager.removeViewImmediate(view as View)
	}
	
	fun updateView(view: FloatingView, x: Int = view.currentX, y: Int = view.currentY) {
		view.currentX = x
		view.currentY = y
		viewsMap[view]?.x = x
		viewsMap[view]?.y = y
		viewsMap[view]?.gravity = view.gravity()
		mWindowManager.updateViewLayout(view as View, viewsMap[view])
	}
	
	fun moveYAttachedView(v: FloatingView, y: Int, l: MoveListener? = null) {
		val springSystem = SpringSystem.create()
		var spring = springSystem.createSpring()
		spring = springSystem.createSpring()
		val lp = viewsMap[v] ?: throw IllegalStateException()
		spring.currentValue = lp.y.toDouble()
		spring.endValue = y.toDouble()
		spring.addListener(object : SimpleSpringListener() {
			override fun onSpringUpdate(spring: Spring) {
				Log.debug("spring value is ", spring.currentValue)
				getInstance((v as View).context).updateView(v, y = spring.currentValue.toInt())
				if (y == 0 && y > lp.y) {
					spring.destroy()
					l?.onMoveFinished()
				} else if (y != 0 && y < lp.y) {
					spring.destroy()
					l?.onMoveFinished()
				} else if (spring.currentValue.toInt() == spring.endValue.toInt()) {
					l?.onMoveFinished()
				}
			}
		})
	}
	
	interface MoveListener {
		fun onMoveFinished()
	}
	
	companion object {
		
		private var ourInstance: WindowsManager? = null
		
		fun getInstance(context: Context): WindowsManager {
			if (ourInstance == null)
				ourInstance = WindowsManager(context)
			return ourInstance!!
		}
	}
}
