package com.tomer.draw.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import com.squareup.picasso.Picasso
import com.tomer.draw.R
import com.tomer.draw.utils.Log
import com.tomer.draw.utils.helpers.DisplaySize
import com.tomer.draw.windows.HolderService
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.io.File

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 8/1/17.
 */

data class RequiredAsyncData(val context: Context, val view: View?, val files: Array<File>)

data class ResultData(val context: Context, val view: View?, val items: ArrayList<Item>?)

fun getFiles(context: Context) = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name)).listFiles({ _, filename -> filename.contains(".jpg") })

class LoadDataTask : AsyncTask<RequiredAsyncData, Void, ResultData>() {
	
	override fun doInBackground(vararg requiredData: RequiredAsyncData): ResultData {
		val data = requiredData[0]
		val items = arrayListOf<Item>()
		val width = DisplaySize(data.context).getWidth()
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
		val fastAdapter = FastItemAdapter<Item>()
		result.view?.gallery_list?.layoutManager = GridLayoutManager(result.context, 2)
		result.view?.gallery_list?.addItemDecoration(GridSpacingItemDecoration(2, 4, false))
		result.view?.gallery_list?.adapter = fastAdapter
		fastAdapter.add(result.items)
		fastAdapter.withSelectable(true)
		fastAdapter.withOnLongClickListener { _, _, item, _ ->
			HolderService.file = item.file
			result.context.stopService(MainFragment.serviceIntent(result.context))
			result.context.startService(MainFragment.serviceIntent(result.context).putExtra("loadBitmap", true))
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
