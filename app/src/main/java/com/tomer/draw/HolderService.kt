package com.tomer.draw

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.R.attr.y
import android.R.attr.x
import android.R.attr.gravity




/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
class HolderService : Service() {

	override fun onBind(intent: Intent?): IBinder? = null
	override fun onCreate() {
		super.onCreate()
		val draggable = DraggableView(this)
		draggable.addToWindow()
	}
}
