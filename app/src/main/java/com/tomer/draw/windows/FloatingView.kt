package com.tomer.draw.windows

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
interface FloatingView {
	var currentX: Int
	var currentY: Int
	val listeners: ArrayList<OnWindowStateChangedListener>
	fun origHeight(): Int
	fun origWidth(): Int
	fun gravity(): Int
	fun addToWindow(x: Int = 0, y: Int = 0, onWindowAdded: Runnable? = null, listener: OnWindowStateChangedListener? = null){
		addListener(listener)
	}
	fun removeFromWindow(x: Int = 0, y: Int = 0, onWindowRemoved: Runnable? = null, listener: OnWindowStateChangedListener? = null){
		addListener(listener)
	}
	fun addListener(listener: OnWindowStateChangedListener?) {
		if (listener != null)
			listeners.add(listener)
	}
}
