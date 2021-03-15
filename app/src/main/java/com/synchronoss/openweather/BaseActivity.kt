package com.synchronoss.openweather

import com.synchronoss.openweather.util.CustomProgressDialog
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.synchronoss.openweather.view.service.MyLocationUpdateService

open class BaseActivity : AppCompatActivity() {
    private val mProgressDialog = CustomProgressDialog()

    protected fun addFragment(
        @IdRes containerViewId: Int,
        fragment: Fragment,
        fragmentTag: String
    ) {
        supportFragmentManager
            .beginTransaction()
            .add(containerViewId, fragment, fragmentTag)
            .disallowAddToBackStack()
            .commit()
    }

    protected fun replaceFragment(
        @IdRes containerViewId: Int,
        fragment: Fragment,
        fragmentTag: String
    ) {
        val fm = supportFragmentManager
        val fragment1 = fm.findFragmentByTag(fragmentTag)
        if (fragment1 == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(containerViewId, fragment, fragmentTag)
                .addToBackStack("")
                .commit()
        } else {
            supportFragmentManager
                .beginTransaction()
                .replace(containerViewId, fragment1, fragmentTag)
                .addToBackStack("")
                .commit()
        }
    }

    protected open fun showProgress(msg: String?) {
        mProgressDialog.initDialog(this)
        if (mProgressDialog != null && mProgressDialog!!.dialog.isShowing) dismissProgress()
        mProgressDialog.show(this, msg)

    }

    protected open fun dismissProgress() {
        if (mProgressDialog != null && mProgressDialog!!.dialog.isShowing) {
            mProgressDialog!!.dialog.dismiss()
        }
    }


    protected open fun showAlert(msg: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.app_name))
            .setMessage(msg)
            .setCancelable(false)
            .setPositiveButton(
                "OK"
            ) { dialogInterface, i -> dialogInterface.dismiss() }.create().show()
    }

    open fun startTracking() {
        if (!isMyServiceRunning(MyLocationUpdateService::class.java)) {
            val myService = Intent(this, MyLocationUpdateService::class.java)
            startService(myService)
        }
    }

    open fun stopTracking() {
        if (isMyServiceRunning(MyLocationUpdateService::class.java)) {
            val myService = Intent(this, MyLocationUpdateService::class.java)
            stopService(myService)
        }
    }

    /**
     * Check the Service state to update the driver location in the background.
     */
    open fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}