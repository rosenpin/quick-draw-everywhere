package com.tomer.draw.gallery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.tomer.draw.R
import com.tomer.draw.utils.DRAWING_SAVED
import com.tomer.draw.utils.PREFS_KEYS
import com.tomer.draw.utils.helpers.base.BaseFragment
import com.tomer.draw.utils.safeUnregisterReceiver
import com.tomer.draw.windows.HolderService
import kotlinx.android.synthetic.main.fragment_main.view.*


/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/29/17.
 */
class MainFragment : BaseFragment(), OnLoadListener {
	var inForeground = true
	
	companion object {
		var intent: Intent? = null
		fun serviceIntent(context: Context): Intent {
			if (intent == null)
				intent = Intent(context, HolderService::class.java)
			return intent!!
		}
	}
	
	override fun onContentLoaded(result: ResultData?) {
		if (result == null)
			return
		val fastAdapter = FastItemAdapter<Item>()
		view?.gallery_list?.layoutManager = GridLayoutManager(result.context, 2)
		view?.gallery_list?.addItemDecoration(GridSpacingItemDecoration(2, 4, false))
		view?.gallery_list?.adapter = fastAdapter
		fastAdapter.add(result.items)
		fastAdapter.withSelectable(true)
		fastAdapter.withOnLongClickListener { _, _, item, _ ->
			MaterialDialog.Builder(context)
					.items(R.array.image_options)
					.itemsCallback({ _, _, which, _ ->
						when (which) {
							0 -> {
								HolderService.file = item.file
								result.context.startService(MainFragment.serviceIntent(result.context).putExtra("loadBitmap", true))
							}
							1 -> {
								item.file?.canonicalFile?.delete()
								reloadList()
							}
						}
					})
					.show()
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
	
	val newImageReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			reloadList()
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
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val files = getFiles(context)
		files?.let {
			reloadList()
		}
		val prefs = PreferenceManager.getDefaultSharedPreferences(context)
		val enabled = prefs.getBoolean(PREFS_KEYS.ENABLED.key, true)
		view.enable.isChecked = enabled
		handleCheckChange(enabled)
		view.enable.setOnCheckedChangeListener { _, isChecked ->
			handleCheckChange(isChecked)
			prefs.edit().putBoolean(PREFS_KEYS.ENABLED.key, isChecked).apply()
		}
	}
	
	private fun reloadList() {
		LoadDataTask(this).execute(RequiredAsyncData(context, getFiles(context)))
	}
	
	private fun handleCheckChange(isChecked: Boolean) {
		if (!isChecked) {
			context.stopService(serviceIntent(context))
		} else {
			context.startService(serviceIntent(context))
		}
	}
}
