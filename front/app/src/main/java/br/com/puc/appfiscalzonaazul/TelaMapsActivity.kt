package br.com.puc.appfiscalzonaazul

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils.LENGTH_SHORT
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import br.com.puc.appfiscalzonaazul.databinding.ActivityTelaMapsBinding
import br.com.puc.appfiscalzonaazul.functions.FunctionsGenericResponse
import br.com.puc.appfiscalzonaazul.functions.GenericInsertResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.util.Util
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.GsonBuilder


class TelaMapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityTelaMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var lastLocation : Location
    private lateinit var bd : FirebaseFirestore
    private val logEntry = "Registrar locFiscal";
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()
    private lateinit var functions: FirebaseFunctions
    /*private var coordenadas = intent.getSerializableExtra("coordenadas") as Coordenadas
    private var lat = coordenadas.latitude
    private var long  = coordenadas.longitude */

      companion object{
          private const val LOCATION_REQUEST_CODE = 1
      }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        functions = Firebase.functions("southamerica-east1")
        binding = ActivityTelaMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bd= FirebaseFirestore.getInstance()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
       fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun rota1() {
   bd.collection("Rota").document("Rota1").get().addOnCompleteListener { task->

       if(task.isSuccessful)
       {
           val documento = task.result
           if(documento !=null && documento.exists())
           {
               val dados = documento.data

               val Latitude = dados?.get("Latitude")
               val Longitude = dados?.get("Longitude")
               val currentLatLong = LatLng(Latitude as Double, Longitude as Double)
               val markerOptions = MarkerOptions().position(currentLatLong)
               markerOptions.title("proxima local")
               mMap.addMarker(markerOptions)




           }else{
               Snackbar.make(binding.root,"Erro ao ler o documento", Snackbar.LENGTH_SHORT).show()
           }

       }else{
           Snackbar.make(binding.root,"Erro ao ler dados doi servidor", Snackbar.LENGTH_SHORT).show()
       }


   }

    }


    private fun rota2() {
        bd.collection("Rota").document("Rota2").get().addOnCompleteListener { task->

            if(task.isSuccessful)
            {
                val documento = task.result
                if(documento !=null && documento.exists())
                {
                    val dados = documento.data

                    val Latitude = dados?.get("Latitude")
                    val Longitude = dados?.get("Longitude")
                    val currentLatLong = LatLng(Latitude as Double, Longitude as Double)
                    val markerOptions = MarkerOptions().position(currentLatLong)
                    markerOptions.title("proxima local 2")
                    mMap.addMarker(markerOptions)




                }else{
                    Snackbar.make(binding.root,"Erro ao ler o documento", Snackbar.LENGTH_SHORT).show()
                }

            }else{
                Snackbar.make(binding.root,"Erro ao ler dados do servidor", Snackbar.LENGTH_SHORT).show()
            }


        }

    }





    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
       mMap.uiSettings.isZoomControlsEnabled = true
      mMap.setOnMarkerClickListener(this)
       setupMap()
        //proxLoc()
        rota1()
        rota2()
        enviaLoc()
    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
         {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this){ location ->
           if(location!=null){
              lastLocation = location
               val currentLatLong = LatLng(location.latitude, location.longitude)
               placeMarkerOnMap (currentLatLong)
               mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,18f))

           }

        }

    }

    private fun enviaLoc()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this){ location ->
            if(location!=null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude, location.longitude)
                 val Latitude = currentLatLong.latitude
                val Longitude = currentLatLong.longitude
                 binding.root.postDelayed({
                     val p = Coordenadas(Latitude,Longitude)
                     RegistrarIrregularidade(p)
                         .addOnCompleteListener(OnCompleteListener { task ->
                             if (!task.isSuccessful) {

                                 val e = task.exception
                                 if (e is FirebaseFunctionsException) {
                                     val code = e.code
                                     val details = e.details
                                 }
                                 // tratar a exceção...

                             }else{
                                 val genericResp = gson.fromJson(task.result, FunctionsGenericResponse::class.java)
                                 val insertInfo = gson.fromJson(genericResp.payload.toString(), GenericInsertResponse::class.java)
                                 Snackbar.make(binding.root, "Irregularidade registrada com sucesso: " + insertInfo.docId,
                                     Snackbar.LENGTH_LONG).show();
                             }
                         })
                 },300000)
            }

        }

    }

    private fun placeMarkerOnMap(currentLatLong: LatLng) {
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("Localização atual")
        mMap.addMarker(markerOptions)

    }
    /*private fun proxLoc(){
        val currentLatLong = LatLng(37.4182527,-122.085319)
        val markerOptions = MarkerOptions().position(currentLatLong)
        markerOptions.title("proxima loc")
        mMap.addMarker(markerOptions)

    }*/

    override fun onMarkerClick(p0: Marker) = false
    private fun RegistrarIrregularidade(p: Coordenadas): Task<String> {
        val data = hashMapOf(
            "Latitude" to p.Latitude,
            "Longitude" to p.Longitude
        )
        return functions
            .getHttpsCallable("addLocFiscal")
            .call(data)
            .continueWith{task ->
                // convertendo o resultado em string Json válida
                val res = gson.toJson(task.result?.data)
                res
            }}




    }






