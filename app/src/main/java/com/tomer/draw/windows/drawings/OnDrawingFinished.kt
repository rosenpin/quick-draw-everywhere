package com.tomer.draw.windows.drawings

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/27/17.
 */
interface OnDrawingFinished {
	fun onDrawingSaved()
	fun OnDrawingClosed()
	fun OnDrawingSaveFailed()
}
