package com.example.visited_places_app

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
}
