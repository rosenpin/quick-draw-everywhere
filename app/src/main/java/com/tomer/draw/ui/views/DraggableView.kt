package com.tomer.draw.ui.views

import android.content.Context
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.view.Gravity
import android.view.MotionEvent
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.tomer.draw.R
import com.tomer.draw.helpers.DisplaySize
import com.tomer.draw.helpers.OnDrawingFinished
import com.tomer.draw.helpers.WindowsManager
import com.tomer.draw.utils.Log
import com.tomer.draw.utils.circularRevealHide
import com.tomer.draw.utils.circularRevealShow


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
class DraggableView(context: Context, override var currentY: Int = 100, override var currentX: Int = 0) : AppCompatImageView(context), FloatingView {
	
	override fun origHeight(): Int = 150
	
	override fun origWidth(): Int = 150
	
	override fun gravity(): Int = Gravity.LEFT or Gravity.TOP
	
	private val drawView = QuickDrawView(context = context)
	
	init {
		initStyle()
		drawView.onDrawingFinished = object : OnDrawingFinished {
			override fun onDrawingSaved() {
				drawView.removeFromWindow()
			}
			
			override fun OnDrawingClosed() {
				drawView.removeFromWindow()
			}
			
			override fun OnDrawingSaveFailed() {
			
			}
		}
	}
	
	private fun initStyle() {
		setImageResource(R.drawable.ic_pencil)
		background = ContextCompat.getDrawable(context, R.drawable.round)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			elevation = 16f
	}
	
	override fun removeFromWindow(x: Int, y: Int) {
		circularRevealHide(action = Runnable { WindowsManager.getInstance(context).removeView(this) })
	}
	
	override fun addToWindow(x: Int, y: Int) {
		var mDx: Float = 0.toFloat()
		var mDy: Float = 0.toFloat()
		val springSystem = SpringSystem.create()
		var spring = springSystem.createSpring()
		val screenWidth = DisplaySize(context).getWidth(context)
		var origX: Float = -1f
		setOnTouchListener({ _, event ->
			Log.debug(this, "Touching the view")
			val action = event.action
			if (action == MotionEvent.ACTION_DOWN) {
				spring.destroy()
				origX = event.x
				mDx = currentX - event.rawX
				mDy = currentY - event.rawY
			} else if (action == MotionEvent.ACTION_MOVE) {
				currentX = (event.rawX + mDx).toInt()
				currentY = (event.rawY + mDy).toInt()
				WindowsManager.getInstance(context).updateView(this@DraggableView)
			} else if (action == MotionEvent.ACTION_UP) {
				val finalPos = if (screenWidth - event.rawX < event.rawX) screenWidth else 0
				if (origX == event.x)
					this.callOnClick()
				spring = springSystem.createSpring()
				spring.currentValue = currentX.toDouble()
				spring.endValue = finalPos.toDouble()
				spring.addListener(object : SimpleSpringListener() {
					override fun onSpringUpdate(spring: Spring) {
						Log.debug("spring value is ", spring.currentValue)
						currentX = spring.currentValue.toInt()
						WindowsManager.getInstance(context).updateView(this@DraggableView)
						if (finalPos == 0 && finalPos > currentX)
							spring.destroy()
						else if (finalPos != 0 && finalPos < currentX)
							spring.destroy()
					}
				})
			}
			true
		})
		setOnClickListener({
			if (!drawView.isAttached) {
				WindowsManager.getInstance(context).moveYAttachedView(this, 0)
				drawView.addToWindow(currentX, currentY)
			} else
				drawView.removeFromWindow(currentX, currentY)
		})
		WindowsManager.getInstance(context).addView(this)
		circularRevealShow(currentX, currentY, 200f)
	}
}
