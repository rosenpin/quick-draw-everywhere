package com.tomer.draw.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.squareup.picasso.Picasso
import com.tomer.draw.R
import com.tomer.draw.helpers.DisplaySize
import com.tomer.draw.helpers.HolderService
import com.tomer.draw.ui.activities.MainActivity
import com.tomer.draw.ui.imagesgrid.GridSpacingItemDecoration
import com.tomer.draw.ui.imagesgrid.Item
import com.tomer.draw.utils.DRAWING_SAVED
import com.tomer.draw.utils.Log
import java.io.File


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/29/17.
 */
class MainFragment : BaseFragment() {
	var files: Array<File>? = null
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
	
	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val imageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name))
		val finalFiles: Array<File>? = imageDir.listFiles({ _, filename -> filename.contains(".jpg") })
		files = finalFiles
		if (finalFiles != null)
			LoadDataTask().execute(RequiredAsyncData(context, view, finalFiles))
		view?.findViewById<Switch>(R.id.enable)?.setOnCheckedChangeListener { _, isChecked ->
			if (!isChecked)
				context.stopService(serviceIntent(context))
			else
				context.startService(serviceIntent(context))
		}
		context.startService(serviceIntent(context))
	}
	
	data class RequiredAsyncData(val context: Context, val view: View?, val files: Array<File>)
	data class ResultData(val context: Context, val view: View?, val items: ArrayList<Item>?)
	
	class LoadDataTask : AsyncTask<RequiredAsyncData, Void, ResultData>() {
		
		override fun doInBackground(vararg requiredData: RequiredAsyncData): ResultData {
			val data = requiredData[0]
			val items = arrayListOf<com.tomer.draw.ui.imagesgrid.Item>()
			val width = DisplaySize(data.context).getWidth(data.context)
			for (file in data.files) {
				Log.debug("file ", file.name ?: "no name")
				items.add(Item().withBitmap(Picasso.with(data.context).load(file).resize(width / 2, width / 2).centerCrop().get()).withFile(file))
			}
			return ResultData(data.context, data.view, items)
		}
		
		override fun onPostExecute(result: ResultData?) {
			super.onPostExecute(result)
			if (result == null)
				return
			val fastAdapter = FastItemAdapter<com.tomer.draw.ui.imagesgrid.Item>()
			result.view?.findViewById<RecyclerView>(R.id.gallery_list)?.layoutManager = GridLayoutManager(result.context, 2)
			result.view?.findViewById<RecyclerView>(R.id.gallery_list)?.addItemDecoration(GridSpacingItemDecoration(2, 4, false))
			result.view?.findViewById<RecyclerView>(R.id.gallery_list)?.adapter = fastAdapter
			fastAdapter.add(result.items)
			fastAdapter.withSelectable(true)
			fastAdapter.withOnLongClickListener { v, adapter, item, position ->
				HolderService.file = item.file
				result.context.stopService(serviceIntent(result.context))
				result.context.startService(serviceIntent(result.context).putExtra("loadBitmap", true))
				true
			}
			fastAdapter.withOnClickListener { _, _, item, _ ->
				item.file?.let {
					val intent = Intent()
					intent.action = Intent.ACTION_VIEW
					intent.setDataAndType(Uri.fromFile(item.file), "image/*")
					result.context.startActivity(intent)
				}
				false
			}
		}
	}
}
