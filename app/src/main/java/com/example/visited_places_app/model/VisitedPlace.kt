package com.example.visited_places_app.model

import com.google.android.gms.maps.model.Marker
import java.io.Serializable
import java.util.*

class VisitedPlace: Serializable {
    private var id: String
    private var image_uri: String? = null

    @Transient
    private var marker: Marker

    constructor(marker: Marker){
        this.id = UUID.randomUUID().toString()
        this.marker = marker
    }

    fun GetMarker(): Marker {
        return this.marker
    }

    fun SetMarker(marker: Marker) {
        this.marker = marker
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
}
