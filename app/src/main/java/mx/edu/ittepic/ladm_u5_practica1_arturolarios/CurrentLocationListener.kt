package mx.edu.ittepic.ladm_u5_practica1_arturolarios

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.google.firebase.firestore.GeoPoint


class CurrentLocationListener (val main: MapsActivity) : LocationListener
{
    lateinit var currentLocation : GeoPoint

    @SuppressLint("ShowToast")
    override fun onLocationChanged(location: Location?) {
        location?.let { l ->
            currentLocation = GeoPoint(l.latitude, l.longitude)
        }

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}
}