package com.tomer.draw.gallery

import android.content.Context
import android.os.AsyncTask
import android.os.Environment
import com.squareup.picasso.Picasso
import com.tomer.draw.R
import com.tomer.draw.utils.Log
import com.tomer.draw.utils.helpers.DisplaySize
import java.io.File

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 8/1/17.
 */

data class RequiredAsyncData(val context: Context, val files: Array<File>)

data class ResultData(val context: Context, val items: ArrayList<Item>?)

interface OnLoadListener {
	fun onContentLoaded(result: ResultData?)
}

fun getFiles(context: Context) = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name)).listFiles({ _, filename -> filename.contains(".jpg") })

class LoadDataTask(val onLoadListener: OnLoadListener) : AsyncTask<RequiredAsyncData, Void, ResultData>() {
	
	override fun doInBackground(vararg requiredData: RequiredAsyncData): ResultData {
		val data = requiredData[0]
		val items = arrayListOf<Item>()
		val width = DisplaySize(data.context).getWidth()
		for (file in data.files) {
			Log.debug("Found file", file.name ?: "no name")
			items.add(Item().withBitmap(Picasso.with(data.context).load(file).resize(width / 2, width / 2).centerCrop().get()).withFile(file))
		}
		return ResultData(data.context, items)
	}
	
	override fun onPostExecute(result: ResultData?) {
		super.onPostExecute(result)
		onLoadListener.onContentLoaded(result)
	}
}
