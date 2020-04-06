package com.example.visited_places_app

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.visited_places_app.database.DatabaseHelper

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.visited_places_app.model.VisitedPlace
import java.io.Serializable


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val CHANGE_PLACE_REQUEST = 1
    private val PERMISSION_CODE = 1000
    private var visitedPlaces: HashMap<LatLng, VisitedPlace> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DatabaseHelper.initDatabaseInstance(this)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        visitedPlaces = DatabaseHelper.getAllData()

        checkGPSPermission()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMinZoomPreference(6.0f)
        for (place in visitedPlaces.values) {
            mMap.addMarker(MarkerOptions().position(place.GetPosition()))
        }

        mMap.setOnMapClickListener {
            mMap.addMarker(MarkerOptions().position(it))
            val vp = VisitedPlace(it)
            visitedPlaces[it] = vp
            DatabaseHelper.insertData(vp)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(it))
        }

        mMap.setOnMarkerClickListener {
            val intent = Intent(this, VisitedPlaceActivity::class.java)
            intent.putExtra("VisitedPlace", visitedPlaces[it.position])
            startActivityForResult(intent, CHANGE_PLACE_REQUEST)

            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("VisitedPlaces", this.visitedPlaces as Serializable)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        this.visitedPlaces =
            savedInstanceState.getSerializable("VisitedPlaces") as HashMap<LatLng, VisitedPlace>
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to
        if (requestCode == CHANGE_PLACE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                val vp = data!!.getSerializableExtra("VisitedPlace") as VisitedPlace

                visitedPlaces[vp.GetPosition()] = vp
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DatabaseHelper.closeDatabase()
    }

    fun checkGPSPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_DENIED
            ) {
                //permission was not enabled
                val permission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                //show popup to request permission
                ActivityCompat.requestPermissions(this, permission, PERMISSION_CODE)
            } else {
                startService(Intent(applicationContext, PlacesCheckerService::class.java))
            }
        } else {
            startService(Intent(applicationContext, PlacesCheckerService::class.java))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //called when user presses ALLOW or DENY from Permission Request Popup
        when (requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.size <= 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED
                ) {
                    //permission from popup was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
