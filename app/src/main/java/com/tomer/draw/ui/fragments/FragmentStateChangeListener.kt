package com.tomer.draw.ui.fragments

import android.support.v4.app.Fragment

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/29/17.
 */
interface FragmentStateChangeListener {
	fun OnFinish(fragment: BaseFragment)
	fun OnCreate(fragment: BaseFragment)
}
