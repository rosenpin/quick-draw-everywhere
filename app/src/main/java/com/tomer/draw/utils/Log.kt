package com.tomer.draw.utils

import com.tomer.draw.BuildConfig

/**
 * A helper class to log events
 */
object Log {
	/**
	 * @param c class to get tag for
	 * *
	 * @return tag for the object
	 */
	fun getTag(c: Class<*>?): String {
		return if (c != null) c.simpleName else "com.tomer.draw"
	}
	
	/**
	 * @param o object to get tag for
	 * *
	 * @return tag for the object
	 */
	fun getTag(o: Any): String {
		val c = o.javaClass
		return c.simpleName
	}
	
	private fun log(flag: LOG_FLAGS, var1: Any?, var2: Any?) {
		if (!BuildConfig.DEBUG) return
		if (var1 != null && var2 != null) {
			when (flag) {
				LOG_FLAGS.ERROR -> android.util.Log.e(var1.toString(), var2.toString())
				LOG_FLAGS.DEBUG -> android.util.Log.d(var1.toString(), var2.toString())
				LOG_FLAGS.INFO -> android.util.Log.i(var1.toString(), var2.toString())
			}
		}
	}
	
	/**
	 * Send a debug log message.
	 
	 * @param o    Used to identify the source of a log message.  It usually identifies
	 * *             the class or activity where the log call occurs.
	 * *
	 * @param var2 The message you would like logged.
	 */
	fun debug(o: Any, var2: Any) {
		log(LOG_FLAGS.DEBUG, getTag(o), var2)
	}
	
	/**
	 * Send a debug log message.
	 
	 * @param tag  Used to identify the source of a log message.  It usually identifies
	 * *             the class or activity where the log call occurs.
	 * *
	 * @param var2 The message you would like logged.
	 * *
	 * @param var3 The message you would like logged.
	 */
	fun debug(tag: String, var2: Any, var3: Any) {
		log(LOG_FLAGS.DEBUG, tag, var2.toString() + " , " + var3)
	}
	
	/**
	 * Send a debug log message.
	 
	 * @param var1 Used to identify the source of a log message.  It usually identifies
	 * *             the class or activity where the log call occurs.
	 * *
	 * @param var2 The message you would like logged.
	 */
	fun debug(var1: String, var2: Any) {
		log(LOG_FLAGS.DEBUG, var1, var2)
	}
	
	/**
	 * Send an error log message.
	 
	 * @param var1 Used to identify the source of a log message.  It usually identifies
	 * *             the class or activity where the log call occurs.
	 * *
	 * @param var2 The message you would like logged.
	 */
	fun error(var1: String, var2: String) {
		log(LOG_FLAGS.ERROR, var1, var2)
	}
	
	/**
	 * Send an error log message.
	 
	 * @param o    Used to identify the source of a log message.  It usually identifies
	 * *             the class or activity where the log call occurs.
	 * *
	 * @param var2 The message you would like logged.
	 */
	fun error(o: Any, var2: Any) {
		log(LOG_FLAGS.ERROR, getTag(o), var2)
	}
	
	/**
	 * Send an info log message.
	 
	 * @param var1 Used to identify the source of a log message.  It usually identifies
	 * *             the class or activity where the log call occurs.
	 * *
	 * @param var2 The message you would like logged.
	 */
	fun info(var1: String, var2: String) {
		log(LOG_FLAGS.INFO, var1, var2)
	}
	
	/**
	 * Send an info log message.
	 
	 * @param o    Used to identify the source of a log message.  It usually identifies
	 * *             the class or activity where the log call occurs.
	 * *
	 * @param var2 The message you would like logged.
	 */
	fun info(o: Any, var2: Any) {
		log(LOG_FLAGS.INFO, getTag(o), var2)
	}
	
	private enum class LOG_FLAGS {
		ERROR, DEBUG, INFO
	}
}
