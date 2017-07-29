package com.tomer.draw.helpers

import android.content.Context
import android.graphics.Point
import android.support.v7.widget.OrientationHelper
import android.view.WindowManager

/**
 * A helper object to get screen dimensions info
 */
class DisplaySize(context: Context) {
	private val size: Point
	private var height: Int = 0
	private var width: Int = 0
	
	init {
		val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
		size = Point()
		display.getSize(size)
	}
	
	fun getWidth(vertical: Boolean): Int {
		if (width == 0) width = if (vertical) size.x else size.y
		return width
	}
	
	fun getWidth(context: Context): Int {
		val vertical = context.resources.configuration.orientation == OrientationHelper.VERTICAL
		return getWidth(vertical)
	}
	
	fun getHeight(context: Context): Int {
		val vertical = context.resources.configuration.orientation == OrientationHelper.VERTICAL
		return getHeight(vertical)
	}
	
	fun getHeight(vertical: Boolean): Int {
		if (height == 0) height = if (vertical) size.y else size.x
		return height
	}
}
