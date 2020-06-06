package mx.edu.ittepic.ladm_u5_practica1_arturolarios

import com.google.firebase.firestore.GeoPoint
import kotlin.math.abs

data class Building(val name: String?, val location1: GeoPoint?, val location2: GeoPoint?, val location: GeoPoint?)
{
    fun isIn(currentLocation: GeoPoint) : Boolean
    {
        location1?.let { l1 ->
            location2?.let { l2 ->
                if (currentLocation.latitude >= l1.latitude && currentLocation.latitude <= l2.latitude)
                {
                    if (abs(currentLocation.longitude) >= abs(l1.longitude) && abs(currentLocation.longitude) <= abs(l2.longitude))
                    {
                        return true
                    }
                }
            }
        }

        return false
    }
}