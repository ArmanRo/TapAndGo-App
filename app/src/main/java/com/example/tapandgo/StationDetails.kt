package com.example.tapandgo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class StationDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) { // window to show the details of a station, nothing is computed here
        super.onCreate(savedInstanceState)
        setContentView(R.layout.station_details)


        val btnBack = findViewById<Button>(R.id.btnBack)

        val msgStationName = intent.getStringExtra("txtStationName")
        val msgLastUpdate = intent.getStringExtra("txtLastUpdate")
        val msgStatus = intent.getStringExtra("txtStatus")
        val msgBikesAvailable = intent.getStringExtra("txtBikesAvailable")
        val msgStandsAvailable = intent.getStringExtra("txtStandsAvailable")

        val txtStationName = findViewById<TextView>(R.id.txtStationName)
        val txtLastUpdate = findViewById<TextView>(R.id.txtLastUpdate)
        val txtStatus = findViewById<TextView>(R.id.txtStatus)
        val txtBikesAvailable = findViewById<TextView>(R.id.txtBikesAvailable)
        val txtStandsAvailable = findViewById<TextView>(R.id.txtStandsAvailable)

        txtStationName.text = msgStationName
        txtLastUpdate.text = msgLastUpdate
        txtStatus.text = msgStatus
        txtBikesAvailable.text = msgBikesAvailable
        txtStandsAvailable.text = msgStandsAvailable

        btnBack.setOnClickListener{ // back button to kill this activity and go to the previous one, MainActivity
            this.finish()
        }
    }
}