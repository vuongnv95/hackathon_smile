package com.example.baseproject.ui.map

import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class GoogleMap : SupportMapFragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.apply {
            val latLng = LatLng(21.01777, 105.78100)
            addMarker(MarkerOptions().position(latLng).title("Ahihi"))
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))

            setOnMapClickListener {
                clear()
                animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15F))
                addMarker(MarkerOptions().position(it).title("Ahihi"))
            }
        }
    }
}