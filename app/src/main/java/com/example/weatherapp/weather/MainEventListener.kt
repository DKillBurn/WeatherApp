package com.example.weatherapp.weather

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.R
import com.example.weatherapp.databinding.ActivityMainBinding

class MainEventListener(private val context: Context, private val binding: ActivityMainBinding) {

    init{
        //region EventListeners
        binding.btnRefresh.setOnClickListener {
            MainService.instance.requestLocationData()
        }
        //endregion
    }

    //region NativEvents
    fun onCreateOptionMenu(menu: Menu?){
        (context as AppCompatActivity).menuInflater.inflate(R.menu.menu_main, menu)
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean{
        return when(item.itemId){
            R.id.action_refresh -> {
                MainService.instance.requestLocationData()
                true
            }else -> false
        }
    }
    //endregion
}