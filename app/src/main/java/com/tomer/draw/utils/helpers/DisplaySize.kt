package com.tomer.draw.utils.helpers

import android.content.Context
import android.graphics.Point
import android.view.Display
import android.view.WindowManager

/**
 * A helper object to get screen dimensions info
 */
class DisplaySize(context: Context) {
	private val display: Display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
	private val size: Point = Point()
		get() {
			display.getSize(field)
			return field
		}
	
	fun getWidth() = size.x
	
	fun getHeight() = size.y
}
