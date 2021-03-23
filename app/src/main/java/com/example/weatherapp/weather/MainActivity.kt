package com.example.weatherapp.weather

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.util.DialogManager
import com.example.weatherapp.util.PermissionValidator
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var bindingView: ActivityMainBinding
    private lateinit var eventListener: MainEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingView = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bindingView.root)

        MainService.instance.setFusedLocationClient(this, bindingView)

        eventListener = MainEventListener(this, bindingView)

        MainService.instance.setupUI()

        if(!PermissionValidator.instance.isLocationEnabled(this)){
            Toast.makeText(this, "Your location provider is turned off. Please turn it on in settings", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }else{
            PermissionValidator.instance.askForPermissions(this)
            //Toast.makeText(this, "Your location provider is already on", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        eventListener.onCreateOptionMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if(eventListener.onOptionsItemSelected(item)){
            true
        }else{
            super.onOptionsItemSelected(item)
        }
    }
}