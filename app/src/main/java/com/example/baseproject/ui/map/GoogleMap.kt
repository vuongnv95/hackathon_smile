package com.example.baseproject.ui.map

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import com.example.baseproject.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
            addMarker(
                MarkerOptions().position(latLng)
                    .title("Tòa nhà Sông Đà").icon(
                        BitmapDescriptorFactory.fromBitmap(
                            Bitmap.createScaledBitmap(
                                BitmapFactory.decodeResource(
                                    resources,
                                    R.drawable.ic_car_in_map
                                ), 64, 64, false
                            )
                        )
                    )
            )?.showInfoWindow()
            moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
//
//            setOnMapClickListener {
//                clear()
//                animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15F))
//                addMarker(MarkerOptions().position(it).title("Ahihi"))
//            }
        }
    }
}