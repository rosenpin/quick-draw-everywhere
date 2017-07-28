package com.tomer.draw

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import com.byox.drawview.enums.DrawingCapture
import com.byox.drawview.enums.DrawingMode
import com.byox.drawview.views.DrawView
import kotlinx.android.synthetic.main.quick_draw_view.view.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/27/17.
 */
class QuickDrawView(context: Context?) : FrameLayout(context), FloatingView {
	override var currentX: Int = 0
	override var currentY: Int = 0
	var isAttached = false
	override fun removeFromWindow(x: Int, y: Int) {
		if (isAttached) {
			isAttached = false
			this.circularRevealHide(cx = x + if (x == 0) 50 else -50, cy = y + 50, radius = Math.hypot(DisplaySize(context).getWidth(context).toDouble(), DisplaySize(context).getHeight(context).toDouble()).toFloat(), action = Runnable {
				WindowsManager.getInstance(context).removeView(this)
			})
		}
	}
	
	override fun addToWindow(x: Int, y: Int) {
		if (!isAttached) {
			isAttached = true
			this.circularRevealShow(x + if (x == 0) 50 else -50, y + 50, Math.hypot(DisplaySize(context).getWidth(context).toDouble(), DisplaySize(context).getHeight(context).toDouble()).toFloat())
			Handler().postDelayed({ WindowsManager.getInstance(context).addView(this) }, 100)
		}
	}
	
	override fun origHeight(): Int = (DisplaySize(context).getHeight(context) * 0.8).toInt()
	
	override fun origWidth(): Int = WindowManager.LayoutParams.MATCH_PARENT
	
	override fun gravity() = Gravity.CENTER
	
	var accessibleDrawView: DrawView? = null
	var onDrawingFinished: OnDrawingFinished? = null
	
	init {
		val drawView = LayoutInflater.from(context).inflate(R.layout.quick_draw_view, this).draw_view
		drawView.backgroundColor = Color.WHITE
		drawView.drawWidth = 8
		drawView.drawColor = Color.GRAY
		drawView.isZoomEnabled = true
		drawView.cancel.setOnClickListener { drawView.restartDrawing(); onDrawingFinished?.OnDrawingClosed() }
		drawView.undo.setOnClickListener { undo(drawView) }
		drawView.save.setOnClickListener { save(drawView) }
		drawView.eraser.setOnClickListener { v ->
			drawView.drawingMode =
					if (drawView.drawingMode == DrawingMode.DRAW) DrawingMode.ERASER
					else DrawingMode.DRAW
			drawView.drawWidth =
					if (drawView.drawingMode == DrawingMode.DRAW) 8
					else 28
			(v as FloatingActionButton).setImageResource(
					if (drawView.drawingMode == DrawingMode.DRAW) R.drawable.ic_erase
					else R.drawable.ic_pencil)
		}
		accessibleDrawView = drawView
	}
	
	fun undo(v: DrawView) {
		v.undo()
	}
	
	fun save(v: DrawView) {
		val createCaptureResponse = v.createCapture(DrawingCapture.BITMAP)
		if (!context.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			context.startActivity(Intent(context, AskPermissionActivity::class.java))
			return
		}
		saveFile(createCaptureResponse[0] as Bitmap)
	}
	
	fun saveFile(bitmap: Bitmap) {
		paintBitmapToBlack(bitmap)
		var fileName = Calendar.getInstance().timeInMillis.toString()
		try {
			if (!fileName.contains(".")) {
				fileName = "drawing-$fileName.jpg"
			}
			val imageDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), context.getString(R.string.app_name))
			imageDir.mkdirs()
			val image = File(imageDir, fileName)
			val result = image.createNewFile()
			val fileOutputStream = FileOutputStream(image)
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
			showNotification(bitmap, image)
			onDrawingFinished?.onDrawingSaved()
		} catch (e: IOException) {
			e.printStackTrace()
			onDrawingFinished?.OnDrawingSaveFailed()
		}
	}
	
	private fun paintBitmapToBlack(bitmap: Bitmap) {
		val allpixels = IntArray(bitmap.height * bitmap.width)
		bitmap.getPixels(allpixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
		(0..allpixels.size - 1)
				.filter { allpixels[it] == Color.TRANSPARENT }
				.forEach { allpixels[it] = Color.WHITE }
		bitmap.setPixels(allpixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
	}
	
	fun clear(v: DrawView) {
		v.restartDrawing()
	}
	
	fun showNotification(drawing: Bitmap, file: File) {
		val notificationManager = context
				.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		val intent = Intent()
		intent.action = Intent.ACTION_VIEW
		intent.setDataAndType(Uri.fromFile(file), "image/*")
		val pendingIntent = PendingIntent.getActivity(context, 100, intent, PendingIntent.FLAG_ONE_SHOT)
		
		val notification = Notification.Builder(context)
				/*.setContentTitle(context.resources.getString(R.string.drawing_drawing_saved))
				.setContentText(context.resources.getString(R.string.drawing_view_drawing))*/
				.setStyle(Notification.BigPictureStyle().bigPicture(drawing))
				.setSmallIcon(R.drawable.ic_screenshot)
				.setContentIntent(pendingIntent).build()
		
		notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
		notificationManager.notify(1689, notification)
	}
}
