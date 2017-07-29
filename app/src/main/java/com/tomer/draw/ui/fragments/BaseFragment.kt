package com.tomer.draw.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/29/17.
 */
abstract class BaseFragment : Fragment() {
	private var fragmentStateChangeListener: FragmentStateChangeListener? = null
	internal fun withFragmentStateChangeListener(listener: FragmentStateChangeListener): BaseFragment {
		fragmentStateChangeListener = listener
		return this
	}
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		fragmentStateChangeListener?.OnCreate(this)
	}
	
	fun finish(){
		fragmentStateChangeListener?.OnFinish(this)
	}
}
