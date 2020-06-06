package mx.edu.ittepic.ladm_u5_practica1_arturolarios

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_maps.*

val TEC = LatLng(21.479242, -104.865529)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation : LocationManager
    private val arraySpinner = ArrayList<String>()
    private lateinit var listener : CurrentLocationListener

    val buildings = ArrayList<Building>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        grantPermission()
        getData()

        currentLocation = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        listener = CurrentLocationListener(this)

        try {
            currentLocation.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 01f, listener)
        }
        catch (e: SecurityException)
        {
            AlertDialog.Builder(this).setTitle("Atención")
                .setMessage(e.message)
                .setPositiveButton("OK"){_, _ ->}
                .show()
        }

        btnWhereAmI.setOnClickListener {
            var message = ""
            buildings.forEach { b ->
                if (b.isIn(listener.currentLocation))
                {
                    message = "Usted se encuentra cerca o dentro de ${b.name}"
                }
            }

            if (message.isEmpty())
            {
                message = "No se encuentra cerca de ninguna ubicación registrada del Tecnológico"
            }

            AlertDialog.Builder(this).setTitle("Su Ubicación")
                .setMessage(message)
                .setPositiveButton("OK"){_, _ ->}
                .show()
        }
    }

    private fun getData()
    {
        db.collection("Tec").addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null)
            {
                AlertDialog.Builder(this).setTitle("Atención")
                    .setMessage(firebaseFirestoreException.message)
                    .setPositiveButton("OK"){_, _ ->}
                    .show()
                return@addSnapshotListener
            }

            buildings.clear()
            arraySpinner.clear()

            querySnapshot?.forEach { doc ->
                doc.getString("name")?.let { arraySpinner.add(it) }
                buildings.add(Building(doc.getString("name"),
                                       doc.getGeoPoint("point1"),
                                       doc.getGeoPoint("point2"),
                                       doc.getGeoPoint("location")))
            }

            arraySpinner.sort()
            buildings.sortBy {
                it.name
            }

            arraySpinner.add(0, "Seleccione una opción")

            spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arraySpinner)
            spinner.onItemSelectedListener = this
        }
    }

    private fun grantPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 10)
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.isMyLocationEnabled = true
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TEC, 17f))
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != 0)
        {
            buildings[position - 1].location?.let { l ->
                mMap.clear()
                val newLocation = LatLng(l.latitude, l.longitude)
                mMap.addMarker(MarkerOptions().position(newLocation).title(buildings[position].name))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 18f))
            }
        }
    }
}
