package com.tomer.draw.helpers

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.tomer.draw.ui.views.DraggableView


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
class HolderService : Service() {
	lateinit var draggable : DraggableView
	override fun onBind(intent: Intent?): IBinder? = null
	override fun onCreate() {
		super.onCreate()
		draggable = DraggableView(this)
		draggable.addToWindow()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		draggable.removeFromWindow()
	}
}
