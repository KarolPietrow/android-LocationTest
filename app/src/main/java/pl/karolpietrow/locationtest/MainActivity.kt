package pl.karolpietrow.locationtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import pl.karolpietrow.locationtest.ui.theme.LocationTestTheme

class MainActivity : ComponentActivity() {

    // Zmienna do zarządzania prośbą o uprawnienia
    private lateinit var locationPermissionRequest: ActivityResultLauncher<Array<String>>

    // Globalna zmienna FusedLocationProviderClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicjalizacja FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Rejestracja ActivityResultLauncher do żądania uprawnień
        locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Sprawdzamy, które uprawnienia zostały przyznane
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    // Uprawnienia do dokładnej lokalizacji przyznane
                    getLocation()
                }
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Uprawnienia do przybliżonej lokalizacji przyznane
                    Toast.makeText(this, "Zezwolono na przybliżoną lokalizację", Toast.LENGTH_SHORT).show()
                    getLocation()
                }
                else -> {
                    // Brak uprawnień
                    Toast.makeText(this, "Nie zezwolono na lokalizację", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Ustawienie widoku aplikacji
        setContent {
            LocationTestTheme {
                MainScreen(this) // Przekazujemy 'this', ponieważ to jest Activity
            }
        }
    }

    // Funkcja do pobierania lokalizacji
    fun getLocation() {
        // Sprawdzenie uprawnień do lokalizacji
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Jeśli nie ma uprawnień, prosimy o nie
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        // Uzyskiwanie ostatniej znanej lokalizacji
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Wyświetlanie lokalizacji w Toast
                    Toast.makeText(
                        this,
                        "Lokalizacja: Lat: $latitude, Lon: $longitude",
                        Toast.LENGTH_LONG
                    ).show()
                    openMap(latitude, longitude)
                } else {
                    Toast.makeText(this, "Nie udało się pobrać lokalizacji", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun openMap(latitude:Double, longitude:Double) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("geo:0,0?q=$latitude,$longitude")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Błąd: Brak aplikacji do wyświetlania map!", Toast.LENGTH_SHORT)
                .show()
        }
    }

}

@Composable
fun MainScreen(activity: MainActivity) {
    Button(
        onClick = {
            // Wywołanie getLocation przy kliknięciu
            activity.getLocation() // Teraz wywołujemy funkcję getLocation z MainActivity
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Get user location")
    }
}
