package com.tomer.draw

import android.app.Application
import android.os.StrictMode

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/28/17.
 */
class DrawApp : Application() {
	override fun onCreate() {
		super.onCreate()
		val vmpolicyBuilder = StrictMode.VmPolicy.Builder()
		StrictMode.setVmPolicy(vmpolicyBuilder.build())
	}
}
