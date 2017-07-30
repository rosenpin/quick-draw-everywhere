package com.tomer.draw.helpers

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

class AskPermissionActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		ActivityCompat.requestPermissions(this,
				arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
						android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
	}
	
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		finish()
	}
}
