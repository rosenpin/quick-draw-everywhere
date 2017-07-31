package com.tomer.draw.gallery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tomer.draw.R
import com.tomer.draw.utils.DRAWING_SAVED
import com.tomer.draw.utils.helpers.base.BaseFragment
import com.tomer.draw.utils.safeUnregisterReceiver
import com.tomer.draw.windows.HolderService
import kotlinx.android.synthetic.main.fragment_main.view.*


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/29/17.
 */
class MainFragment : BaseFragment() {
	var inForeground = true
	
	companion object {
		var intent: Intent? = null
		fun serviceIntent(context: Context): Intent {
			if (intent == null)
				intent = Intent(context, HolderService::class.java)
			return intent!!
		}
	}
	
	val newImageReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			context?.let {
				if (inForeground) {
					activity.finish()
					activity.startActivity(Intent(context, MainActivity::class.java))
				}
			}
		}
	}
	
	override fun onPause() {
		super.onPause()
		inForeground = false
	}
	
	override fun onResume() {
		super.onResume()
		inForeground = true
	}
	
	override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		return inflater?.inflate(R.layout.fragment_main, container, false)
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		context.registerReceiver(newImageReceiver, IntentFilter(DRAWING_SAVED))
	}
	
	override fun onDestroy() {
		super.onDestroy()
		context.safeUnregisterReceiver(newImageReceiver)
	}
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val files = getFiles(context)
		files?.let {
			LoadDataTask().execute(RequiredAsyncData(context, view, files))
		}
		view?.enable?.setOnCheckedChangeListener { _, isChecked ->
			if (!isChecked)
				context.stopService(serviceIntent(context))
			else
				context.startService(serviceIntent(context))
		}
		context.startService(serviceIntent(context))
	}
	
}
