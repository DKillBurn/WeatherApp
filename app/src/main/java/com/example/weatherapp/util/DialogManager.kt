package com.example.weatherapp.util

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.example.weatherapp.R

class DialogManager(){
    companion object{
        val instance = DialogManager()
    }


    fun rationalDialogForPermissions(context: Context): Dialog{
        return AlertDialog.Builder(context)
            .setMessage("It looks like you have turned off permissions required for this feature. It can be enabled under Application Settings")
            .setPositiveButton("GO TO SETTINGS"
            ){_,_->
                try{
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", context.packageName, null)
                    intent.data = uri
                    (context as Activity).startActivity(intent)
                }catch(e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }.setNegativeButton("Cancel"
            ){dialog,_->
                dialog.dismiss()
            }.create()
    }

    fun backgroundDialog(context: Context): Dialog{
        val mDialog: Dialog = Dialog(context)

        mDialog.setContentView(R.layout.dialog_custom_progress)

        mDialog.setCancelable(false)

        return mDialog
    }
}