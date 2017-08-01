package com.tomer.draw.windows.bubble

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.view.Gravity
import android.view.MotionEvent
import com.facebook.rebound.SimpleSpringListener
import com.facebook.rebound.Spring
import com.facebook.rebound.SpringSystem
import com.tomer.draw.R
import com.tomer.draw.utils.DRAWING_SAVED
import com.tomer.draw.utils.Log
import com.tomer.draw.utils.circularRevealHide
import com.tomer.draw.utils.circularRevealShow
import com.tomer.draw.utils.helpers.DisplaySize
import com.tomer.draw.windows.FloatingView
import com.tomer.draw.windows.OnWindowStateChangedListener
import com.tomer.draw.windows.WindowsManager
import com.tomer.draw.windows.drawings.OnDrawingFinished
import com.tomer.draw.windows.drawings.QuickDrawView
import java.io.File


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
@SuppressLint("ViewConstructor") class DraggableView(context: Context, override var currentY: Int = 100, override var currentX: Int = 0) : AppCompatImageView(context), FloatingView {
	
	override fun origHeight(): Int = 150
	
	override fun origWidth(): Int = 150
	
	override fun gravity(): Int = Gravity.LEFT or Gravity.TOP
	
	private val drawView = QuickDrawView(context = context)
	private val animationsHandler = Handler()
	
	init {
		initStyle()
		drawView.onDrawingFinished = object : OnDrawingFinished {
			override fun onDrawingSaved() {
				context.sendBroadcast(Intent(DRAWING_SAVED))
				drawView.removeFromWindow(x = currentX, y = currentY)
			}
			
			override fun OnDrawingClosed() {
				drawView.removeFromWindow(x = currentX, y = currentY)
			}
			
			override fun OnDrawingSaveFailed() {
			
			}
		}
	}
	
	private fun initStyle() {
		setImageResource(R.drawable.ic_pencil)
		background = ContextCompat.getDrawable(context, R.drawable.round)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			elevation = context.resources.getDimension(R.dimen.qda_design_appbar_elevation)
	}
	
	override fun removeFromWindow(x: Int, y: Int, listener: OnWindowStateChangedListener?, onWindowRemoved: Runnable?) {
		circularRevealHide(action = Runnable { WindowsManager.getInstance(context).removeView(this) })
	}
	
	override fun addToWindow(x: Int, y: Int, listener: OnWindowStateChangedListener?, onWindowAdded: Runnable?) {
		var mDx: Float = 0.toFloat()
		var mDy: Float = 0.toFloat()
		val springSystem = SpringSystem.create()
		var spring = springSystem.createSpring()
		val screenWidth = DisplaySize(context).getWidth()
		var origX: Float = -1f
		setOnTouchListener({ _, event ->
			Log.debug(this, "Touching the view")
			val action = event.action
			if (action == MotionEvent.ACTION_DOWN) {
				animate().alpha(1f).setDuration(300).start()
				spring.destroy()
				origX = event.x
				mDx = currentX - event.rawX
				mDy = currentY - event.rawY
			} else if (action == MotionEvent.ACTION_MOVE) {
				currentX = (event.rawX + mDx).toInt()
				currentY = (event.rawY + mDy).toInt()
				WindowsManager.getInstance(context).updateView(this@DraggableView)
			} else if (action == MotionEvent.ACTION_UP) {
				fadeOut()
				val finalPos = if (screenWidth - event.rawX < event.rawX) screenWidth else 0
				if (origX == event.x)
					this.callOnClick()
				spring = springSystem.createSpring()
				spring.currentValue = currentX.toDouble()
				spring.endValue = finalPos.toDouble()
				spring.addListener(object : SimpleSpringListener() {
					override fun onSpringUpdate(spring: Spring) {
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
				WindowsManager.getInstance(context).moveYAttachedView(this, y = 0)
				drawView.addToWindow(currentX, currentY, object : OnWindowStateChangedListener {
					override fun onWindowAdded() {}
					
					override fun onWindowRemoved() {
						fadeOut()
					}
					
				})
			} else
				drawView.removeFromWindow(currentX, currentY)
		})
		WindowsManager.getInstance(context).addView(this)
		circularRevealShow(currentX, currentY, 200f)
		fadeOut()
	}
	
	fun fadeOut() {
		animationsHandler.removeCallbacksAndMessages(null)
		animationsHandler.postDelayed({ if (!drawView.isAttached) animate().alpha(0.6f).setDuration(1000).start() }, 3000)
	}
	
	fun loadBitmap(image: File) {
		WindowsManager.getInstance(context).moveYAttachedView(this, 0)
		drawView.addToWindow(currentX, currentY, object : OnWindowStateChangedListener {
			override fun onWindowAdded() {
				drawView.setImage(image)
			}
			
			override fun onWindowRemoved() {
			
			}
		})
	}
}
