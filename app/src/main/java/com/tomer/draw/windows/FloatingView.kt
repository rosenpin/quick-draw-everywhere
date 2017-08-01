package com.tomer.draw.windows

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
interface FloatingView {
	var currentX: Int
	var currentY: Int
	fun origHeight(): Int
	fun origWidth(): Int
	fun gravity(): Int
	fun addToWindow(x: Int = 0, y: Int = 0, listener: OnWindowStateChangedListener? = null, onWindowAdded: Runnable? = null)
	fun removeFromWindow(x: Int = 0, y: Int = 0, listener: OnWindowStateChangedListener? = null, onWindowRemoved: Runnable? = null)
}
