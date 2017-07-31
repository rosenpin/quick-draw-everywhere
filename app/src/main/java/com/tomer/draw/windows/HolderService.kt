package com.tomer.draw.windows

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.tomer.draw.windows.bubble.DraggableView
import java.io.File


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
class HolderService : Service() {
	
	companion object {
		var file: File? = null
	}
	
	lateinit var draggable: DraggableView
	override fun onBind(intent: Intent?): IBinder? = null
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		val image: Boolean = intent?.getBooleanExtra("loadBitmap", false) ?: false
		if (image) {
			if (file != null)
				draggable.loadBitmap(file!!)
		}
		return super.onStartCommand(intent, flags, startId)
	}
	
	override fun onCreate() {
		super.onCreate()
		draggable = DraggableView(this)
		draggable.addToWindow()
	}
	
	override fun onDestroy() {
		super.onDestroy()
		draggable.removeFromWindow()
		file = null
	}
}
