package com.android.ajay.demoproductapp.view

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.ajay.demoproductapp.R

abstract class BaseActivity : AppCompatActivity(), IDialogHelper {

    private val IMAGE_PERMISSION = 10000
    private var permissionRequestCallBack: (() -> Unit)? = null

    private lateinit var progressDialog: Dialog
    private lateinit var progressTitleMsg: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setProgressDialog()
    }


    private fun setProgressDialog() {
        progressDialog = Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(R.layout.item_progress_dialog)
            setCancelable(false)
        }
        progressTitleMsg = progressDialog.findViewById(R.id.msg_et)
    }

    override fun showProgress() {
        runOnUiThread {
            try {
                if (!progressDialog.isShowing && !isFinishing) progressDialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun hideProgress() {
        runOnUiThread {
            try {
                if (progressDialog.isShowing)
                    progressDialog.dismiss()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }


    fun askGalleryPermission(onPermissionSuccess: () -> Unit) {
        permissionRequestCallBack = onPermissionSuccess
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                IMAGE_PERMISSION
            )
        } else permissionRequestCallBack?.invoke()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            IMAGE_PERMISSION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permissionRequestCallBack?.invoke()
                }
            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}


interface IDialogHelper {
    fun showProgress()
    fun hideProgress()
}



