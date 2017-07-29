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
		val finalFiles: Array<File> = imageDir.listFiles({ _, filename -> filename.contains(".jpg") })
		files = finalFiles
		LoadDataTask(context, view).execute(*finalFiles)
		val serviceIntent = Intent(context, HolderService::class.java)
		view?.findViewById<Switch>(R.id.enable)?.setOnCheckedChangeListener { _, isChecked ->
			if (!isChecked)
				context.stopService(serviceIntent)
			else
				context.startService(serviceIntent)
		}
		context.startService(serviceIntent)
	}
	
	class LoadDataTask(private val context: Context, private val view: View?) : AsyncTask<File?, Void, ArrayList<Item>>() {
		
		override fun doInBackground(vararg files: File?): ArrayList<Item> {
			val items = arrayListOf<com.tomer.draw.ui.imagesgrid.Item>()
			val width = DisplaySize(context).getWidth(context)
			for (file in files) {
				Log.debug("file ", file?.name ?: "no name")
				items.add(Item().withBitmap(Picasso.with(context).load(file).resize(width / 2, width / 2).centerCrop().get()).withFile(file))
			}
			return items
		}
		
		override fun onPostExecute(result: ArrayList<Item>?) {
			super.onPostExecute(result)
			val fastAdapter = FastItemAdapter<com.tomer.draw.ui.imagesgrid.Item>()
			view?.findViewById<RecyclerView>(R.id.gallery_list)?.layoutManager = GridLayoutManager(context, 2)
			view?.findViewById<RecyclerView>(R.id.gallery_list)?.addItemDecoration(GridSpacingItemDecoration(2, 4, false))
			view?.findViewById<RecyclerView>(R.id.gallery_list)?.adapter = fastAdapter
			fastAdapter.add(result)
			fastAdapter.withSelectable(true)
			fastAdapter.withOnClickListener { v, adapter, item, position ->
				val intent = Intent()
				intent.action = Intent.ACTION_VIEW
				intent.setDataAndType(Uri.fromFile(item.file), "image/*")
				context.startActivity(intent)
				false
			}
		}
	}
	
}
