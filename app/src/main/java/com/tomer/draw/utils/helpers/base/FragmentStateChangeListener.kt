package com.tomer.draw.utils.helpers.base

/**
 * DrawEverywhere
 * Created by Tomer Rosenfeld on 7/29/17.
 */
interface FragmentStateChangeListener {
	fun OnFragmentFinish(fragment: BaseFragment)
	fun OnFragmentCreate(fragment: BaseFragment)
}
