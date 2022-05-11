package com.example.tapandgo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tapandgo.dataFromAPI.Contract
import com.example.tapandgo.dataFromAPI.Station
import com.example.tapandgo.StationDetails
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


const val BASE_URL = "https://api.jcdecaux.com/"

class MainActivity : AppCompatActivity() {

    // variables to store row information from API
    lateinit var stationResponseBody: List<Station>
    lateinit var contractResponseBody: List<Contract>

    // global variables to store filtered information
    var cities: MutableList<String> = mutableListOf()
    lateinit var actualCity: String
    var stationsFromCity: MutableList<Station> = mutableListOf()
    var filteredStationsFromCity: MutableList<Station> = mutableListOf()


    lateinit var inputStationName: TextInputEditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // functions for API request, triggered at the start of the app
        // getStationsData() may be triggered to update information on stations if button btnReloadData pressed
        getContractsData()
        getStationsData()


        val btnOkSearch = findViewById<Button>(R.id.btnOkSearch)
        val btnReloadData= findViewById<Button>(R.id.buttonId)
        val btnChooseCity = findViewById<Button>(R.id.btnChooseCity)
        inputStationName = findViewById(R.id.inputStationName)


        btnReloadData.setOnClickListener{
            getStationsData()
        }

        btnOkSearch.setOnClickListener{
            inputStationName.onEditorAction(EditorInfo.IME_ACTION_DONE)
            if(inputStationName.text.toString() == ""){display(stationsFromCity)}
            else{
                filteredStationsFromCity = search(inputStationName.text,stationsFromCity)
                display(filteredStationsFromCity)
            }
        }

        btnChooseCity.setOnClickListener{
            val dialog = Dialog(this)
            val citiesLayout = LinearLayout(this)
            citiesLayout.orientation = LinearLayout.VERTICAL
            citiesLayout.gravity = LinearLayout.TEXT_ALIGNMENT_CENTER
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.setMargins(30, 10, 30, 10)
            for(city in cities){
                val cityButton = Button(this)
                cityButton.text = city
                cityButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                citiesLayout.addView(cityButton, layoutParams)
                cityButton.setOnClickListener{
                    actualCity = city
                    selectStationFromCity(city)
                    display(stationsFromCity)
                    dialog.dismiss()
                }
            }
            dialog.setContentView(citiesLayout)
            dialog.show()
        }


    }

    private fun getContractsData(){     // API request
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val retrofitData = retrofitBuilder.getContracts("c27b57afb6de0fd475f24b5009810078201ba237")
        retrofitData.enqueue(object : Callback<List<Contract>?> {

            override fun onResponse(call: Call<List<Contract>?>, response: Response<List<Contract>?>) {
                contractResponseBody = response.body()!!
                selectCitiesInCountry("FR")     // country code could be dynamic and editable by user
            }

            override fun onFailure(call: Call<List<Contract>?>, t: Throwable) {
            }
        })
    }

    private fun selectCitiesInCountry(countryCode: String){     // store all cities name of target country in a list
        for (data in contractResponseBody){
            if(data.country_code == countryCode){
                cities.add(data.name)
            }
        }
    }


    private fun getStationsData() {     // API request
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .build()
            .create(ApiInterface::class.java)

        val retrofitData = retrofitBuilder.getStations("c27b57afb6de0fd475f24b5009810078201ba237")
        retrofitData.enqueue(object : Callback<List<Station>?> {

            override fun onResponse(call: Call<List<Station>?>, response: Response<List<Station>?>) {
                stationResponseBody = response.body()!!
            }

            override fun onFailure(call: Call<List<Station>?>, t: Throwable) {
            }
        })
    }

    private fun selectStationFromCity(city: String){
        stationsFromCity.clear()
        for (data in stationResponseBody){
            if (data.contractName == city){
                stationsFromCity.add(data)
            }
        }
    }

    private fun search(name: Editable?, stations: List<Station>): MutableList<Station> {    // function used to search stations according to user string input
        var askedStations: MutableList<Station> = mutableListOf()                           // return list of stations containing the input string
        for (station in stations){
            if (station.name.contains(name.toString())){
                askedStations.add(station)
            }
        }
        return askedStations
    }

    private fun clearLayout(){
        val mainLinearLayout = findViewById<LinearLayout>(R.id.mainLinearLayout)
        mainLinearLayout.removeAllViews()
    }

    private fun display(stations: List<Station>){       // display dynamic layout of button of stations
        val mainLinearLayout = findViewById<LinearLayout>(R.id.mainLinearLayout)

        clearLayout()

        for (station in stations){
            val stationButton = Button(this)
            stationButton.text = station.name
            stationButton.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mainLinearLayout.addView(stationButton)

            stationButton.setOnClickListener{
                val intent = Intent(this, StationDetails::class.java).also { // send all needed information to StationDetails for it to be displayed
                    it.putExtra("txtStationName", station.name)
                    it.putExtra("txtLastUpdate", station.lastUpdate)
                    it.putExtra("txtStatus", station.status)
                    it.putExtra("txtBikesAvailable", station.mainStands.availabilities.bikes.toString())
                    it.putExtra("txtElectricBikeAvailable", station.mainStands.availabilities.electricalBikes.toString())
                    it.putExtra("txtStandsAvailable", station.mainStands.availabilities.stands.toString())
                    startActivity(it)
                }
            }
        }
    }


}
