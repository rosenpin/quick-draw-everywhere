package com.tomer.draw.ui.views

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
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout
import com.byox.drawview.enums.BackgroundScale
import com.byox.drawview.enums.BackgroundType
import com.byox.drawview.enums.DrawingCapture
import com.byox.drawview.enums.DrawingMode
import com.byox.drawview.views.DrawView
import com.tomer.draw.R
import com.tomer.draw.helpers.*
import com.tomer.draw.utils.circularRevealHide
import com.tomer.draw.utils.circularRevealShow
import com.tomer.draw.utils.hasPermissions
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
	override fun removeFromWindow(x: Int, y: Int, listener: OnWindowStateChangedListener?) {
		if (isAttached) {
			isAttached = false
			this.circularRevealHide(cx = x + if (x == 0) 50 else -50, cy = y + 50, radius = Math.hypot(DisplaySize(context).getWidth().toDouble(), DisplaySize(context).getHeight().toDouble()).toFloat(), action = Runnable {
				WindowsManager.getInstance(context).removeView(this)
				listener?.onWindowRemoved()
			})
		}
	}
	
	override fun addToWindow(x: Int, y: Int, listener: OnWindowStateChangedListener?) {
		if (!isAttached) {
			isAttached = true
			this.circularRevealShow(x + if (x == 0) 50 else -50, y + 50, Math.hypot(DisplaySize(context).getWidth().toDouble(), DisplaySize(context).getHeight().toDouble()).toFloat())
			Handler().postDelayed({
				WindowsManager.getInstance(context).addView(this)
				Handler().postDelayed({
					listener?.onWindowAdded()
				}, 60)
			}, 100)
		}
	}
	
	override fun origHeight(): Int = (DisplaySize(context).getHeight() * (if (!fullScreen) 0.8f else 1f)).toInt()
	
	override fun origWidth(): Int = WindowManager.LayoutParams.MATCH_PARENT
	
	override fun gravity() = Gravity.CENTER
	
	var accessibleDrawView: DrawView? = null
	var onDrawingFinished: OnDrawingFinished? = null
	var fullScreen = false
	
	init {
		val drawView = LayoutInflater.from(context).inflate(R.layout.quick_draw_view, this).draw_view
		drawView.setBackgroundDrawColor(Color.WHITE)
		drawView.backgroundColor = Color.WHITE
		drawView.setDrawViewBackgroundColor(Color.WHITE)
		drawView.drawWidth = 8
		drawView.drawColor = Color.GRAY
		cancel.setOnClickListener { drawView.restartDrawing(); onDrawingFinished?.OnDrawingClosed() }
		undo.setOnClickListener { drawView.undo(); refreshRedoUndoButtons(drawView) }
		redo.setOnClickListener { drawView.redo(); refreshRedoUndoButtons(drawView) }
		minimize.setOnClickListener { removeFromWindow() }
		maximize.setOnClickListener {
			fullScreen = !fullScreen; removeFromWindow(listener = object : OnWindowStateChangedListener {
			override fun onWindowAdded() {}
			override fun onWindowRemoved() {
				addToWindow()
			}
		})
		}
		save.setOnClickListener { save(drawView) }
		eraser.setOnClickListener { v ->
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
		refreshRedoUndoButtons(drawView)
		drawView.setOnDrawViewListener(object : DrawView.OnDrawViewListener {
			override fun onStartDrawing() {
				refreshRedoUndoButtons(drawView)
			}
			
			override fun onEndDrawing() {
				refreshRedoUndoButtons(drawView)
			}
			
			override fun onClearDrawing() {
				refreshRedoUndoButtons(drawView)
			}
			
			override fun onRequestText() {
			}
			
			override fun onAllMovesPainted() {
			}
			
		})
		accessibleDrawView = drawView
	}
	
	private fun refreshRedoUndoButtons(drawView: DrawView) {
		redo.alpha = if (drawView.canRedo()) 1f else 0.4f
		undo.alpha = if (drawView.canUndo()) 1f else 0.4f
	}
	
	internal fun setImage(file: File) {
		accessibleDrawView?.setBackgroundImage(file, BackgroundType.FILE, BackgroundScale.CENTER_INSIDE)
	}
	
	override fun dispatchKeyEvent(event: KeyEvent): Boolean {
		if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event
				.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			return super.dispatchKeyEvent(event)
		}
		removeFromWindow()
		return super.dispatchKeyEvent(event)
	}
	
	
	fun undo(v: DrawView) {
		v.undo()
	}
	
	fun save(v: DrawView) {
		val createCaptureResponse = v.createCapture(DrawingCapture.BITMAP)
		if (!context.hasPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			context.startActivity(Intent(context, AskPermissionActivity::class.java))
			return
		}
		saveFile(createCaptureResponse[0] as Bitmap)
	}
	
	fun saveFile(bitmap: Bitmap) {
		paintBitmapToBlack(bitmap)
		var fileName = Calendar.getInstance().timeInMillis.toString()
		try {
			fileName = "drawing-$fileName.jpg"
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
