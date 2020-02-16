package com.example.visited_places_app.model

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.Serializable
import java.util.*

class VisitedPlace: Serializable {
    private var id: String
    private var image_uri: String? = null
    private var latLng: DoubleArray = DoubleArray(2)

    constructor(latLng: LatLng){
        this.id = UUID.randomUUID().toString()
        this.latLng[0] = latLng.latitude
        this.latLng[1] = latLng.longitude
    }

    fun GetPosition(): LatLng {
        return LatLng(this.latLng[0], this.latLng[1])
    }

    fun SetPosition(latLng: LatLng) {
        this.latLng[0] = latLng.latitude
        this.latLng[1] = latLng.longitude
    }

    fun GetImage(): String? {
        return this.image_uri
    }

    fun SetImage(image: String?) {
        this.image_uri = image
    }

    fun GetID(): String {
        return id
    }

    fun SetID(id: String) {
        this.id = id
    }
}
