package com.tomer.draw.ui.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.tomer.draw.R
import com.tomer.draw.ui.fragments.BaseFragment
import com.tomer.draw.ui.fragments.FragmentStateChangeListener
import com.tomer.draw.ui.fragments.IntroFragment
import com.tomer.draw.ui.fragments.MainFragment


class MainActivity : AppCompatActivity(), FragmentStateChangeListener {
	override fun OnFragmentFinish(fragment: BaseFragment) {
		when (fragment) {
			is IntroFragment -> {
				setScreen(mainFragment, "main")
			}
			is MainFragment -> {
				finish()
			}
		}
	}
	
	override fun OnFragmentCreate(fragment: BaseFragment) {}
	
	private val introFragment = IntroFragment().withFragmentStateChangeListener(this)
	private val mainFragment = MainFragment().withFragmentStateChangeListener(this)
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setScreen(introFragment, "introduction")
	}
	
	private fun setScreen(fragment: Fragment, name: String) {
		supportFragmentManager.beginTransaction()
				.replace(R.id.fragment_holder, fragment, name)
				.commit()
	}
}
