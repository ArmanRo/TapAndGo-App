package com.example.tapandgo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.tapandgo.dataFromAPI.Contract
import com.example.tapandgo.dataFromAPI.Station
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


const val BASE_URL = "https://api.jcdecaux.com/"

/*  How does this code work ?
*
*   At the start of the app, it asks the API the list of all contracts from a country (here country_code = FR)
*   Then create a list of all the city (contract.name) in this country. In france they are 10 cities.
*   Right after that, it requests all the stations from all contracts stored in a variable.
*   This variable will be filtered in another variable to get the information we need.
*   For example for a particular city we filter the main stations variable by its contractName (cityName) in the "stationsFromCity" variable.
*   This stationsFromCity variable is cleared and created again each time the user selects a different city.
*   stationsFromCity may be filtered again after user uses the search filters.
*
*/

class MainActivity : AppCompatActivity() {

        //////////////////////////////////////////////////////////////
        //////////      Global variables initialisation
        //////////////////////////////////////////////////////////////

    // variables to store row information from API
    lateinit var stationResponseBody: List<Station>
    lateinit var contractResponseBody: List<Contract>

    // global variables to store filtered information
    var cities: MutableList<String> = mutableListOf()
    var actualCity: String = ""
    var stationsFromCity: MutableList<Station> = mutableListOf()
    var filteredStationsFromCity: MutableList<Station> = mutableListOf()




        //////////////////////////////////////////////////////////////
        //////////      Life Cycle of th activity
        //////////////////////////////////////////////////////////////

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
        val btnFilterOpen = findViewById<Button>(R.id.btnFilterOpen)
        val btnFilterBikeAvailable = findViewById<Button>(R.id.btnFilterBikeAvailable)
        var inputStationName = findViewById<TextInputEditText>(R.id.inputStationName)


        btnReloadData.setOnClickListener{
            getStationsData()
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
                    if(city!=actualCity){       // no need to compute anything if the user selects the same city twice
                        actualCity = city
                        selectStationsFromCity(city)
                        display(stationsFromCity)
                    }
                    dialog.dismiss()
                    btnFilterBikeAvailable.text = "BIKES AVAILABLE"      // reset filters
                    btnFilterOpen.text = "OPEN"
                }
            }
            dialog.setContentView(citiesLayout)
            dialog.show()
        }


        btnOkSearch.setOnClickListener{
            inputStationName.onEditorAction(EditorInfo.IME_ACTION_DONE)
            if(inputStationName.text.toString() == ""){
                if(btnFilterOpen.text == "OPEN"){
                    if(btnFilterBikeAvailable.text == "BIKES AVAILABLE"){
                        display(stationsFromCity)
                    }else{display(filterAvailableBikesStations(stationsFromCity))}
                }else{
                    if(btnFilterBikeAvailable.text == "BIKES AVAILABLE"){
                        display(filterOpenStations(stationsFromCity))
                    }else{display(filterOpenStations(filterAvailableBikesStations(stationsFromCity)))}
                }
            }
            else{
                if(btnFilterOpen.text == "OPEN"){
                    if(btnFilterBikeAvailable.text == "BIKES AVAILABLE"){
                        display(search(inputStationName.text, stationsFromCity))
                    }else{display(search(inputStationName.text, filterAvailableBikesStations(stationsFromCity)))}
                }else{
                    if(btnFilterBikeAvailable.text == "BIKES AVAILABLE"){
                        display(search(inputStationName.text, filterOpenStations(stationsFromCity)))
                    }else{display(search(inputStationName.text, filterOpenStations(filterAvailableBikesStations(stationsFromCity))))}
                }
            }
        }

        btnFilterOpen.setOnClickListener{
            if (btnFilterOpen.text == "OPEN"){                                                  //  this is not the optimal way to filter data
                btnFilterOpen.text = "OPEN  ✔"                                                  //  but this is the way to have a clearer code
                if(btnFilterBikeAvailable.text == "BIKES AVAILABLE"){                           //  the test mention that it wil assess "Qualité et lisibilité du code" and not optimisation
                    if(inputStationName.text.toString() == ""){
                        display(filterOpenStations(stationsFromCity))
                    }else{display(filterOpenStations(search(inputStationName.text, stationsFromCity)))}
                }
                else{
                    if(inputStationName.text.toString() == ""){
                        display(filterAvailableBikesStations(filterOpenStations(stationsFromCity)))
                    }else{display(filterAvailableBikesStations(filterOpenStations(search(inputStationName.text, stationsFromCity))))}
                }
            }
            else {
                btnFilterOpen.text = "OPEN"
                if(btnFilterBikeAvailable.text == "BIKES AVAILABLE"){
                    if(inputStationName.text.toString() == ""){
                        display(stationsFromCity)
                    }else{display(search(inputStationName.text, stationsFromCity))}
                }
                else{
                    if(inputStationName.text.toString() == ""){
                        display(filterAvailableBikesStations(stationsFromCity))
                    }else{display(filterAvailableBikesStations(search(inputStationName.text, stationsFromCity)))}
                }
            }
        }

        btnFilterBikeAvailable.setOnClickListener{
            if (btnFilterBikeAvailable.text == "BIKES AVAILABLE"){
                btnFilterBikeAvailable.text = "BIKES AVAILABLE  ✔"
                if(inputStationName.text.toString() == ""){
                    if(btnFilterOpen.text == "OPEN"){
                        display(filterAvailableBikesStations(stationsFromCity))
                    }else{display(filterAvailableBikesStations(filterOpenStations(stationsFromCity)))}
                }
                else{
                    if(btnFilterOpen.text == "OPEN"){
                        display(search(inputStationName.text, filterAvailableBikesStations(stationsFromCity)))
                    }else{display(search(inputStationName.text, filterAvailableBikesStations(filterOpenStations(stationsFromCity))))}
                }
            }
            else {
                btnFilterBikeAvailable.text = "BIKES AVAILABLE"
                if(inputStationName.text.toString() == ""){
                    if(btnFilterOpen.text == "OPEN"){
                        display(stationsFromCity)
                    }else{display(filterOpenStations(stationsFromCity))}
                }
                else{
                    if(btnFilterOpen.text == "OPEN"){
                        display(search(inputStationName.text, stationsFromCity))
                    }else{display(search(inputStationName.text, filterOpenStations(stationsFromCity)))}
                }
            }
        }

    }



        //////////////////////////////////////////////////////////////
        //////////      API request functions
        //////////////////////////////////////////////////////////////

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



        //////////////////////////////////////////////////////////////
        //////////      Filter lists and Search information
        //////////////////////////////////////////////////////////////

    private fun selectCitiesInCountry(countryCode: String){     // store all cities name of target country in a list
        for (data in contractResponseBody){
            if(data.country_code == countryCode){
                cities.add(data.name)
            }
        }
    }

    private fun selectStationsFromCity(city: String){
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

    private fun filterOpenStations(stations: List<Station>): MutableList<Station> {
        var askedStations: MutableList<Station> = mutableListOf()
        for (station in stations){
            if(station.status == "OPEN"){
                askedStations.add(station)
            }
        }
        return askedStations
    }

    private fun filterAvailableBikesStations(stations: List<Station>): MutableList<Station> {
        var askedStations: MutableList<Station> = mutableListOf()
        for (station in stations){
            if(station.mainStands.availabilities.bikes + station.mainStands.availabilities.electricalBikes != 0){
                askedStations.add(station)
            }
        }
        return askedStations
    }





        //////////////////////////////////////////////////////////////
        //////////      Layout display functions
        //////////////////////////////////////////////////////////////

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
