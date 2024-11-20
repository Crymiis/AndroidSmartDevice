package fr.isen.roisin.androidsmartdevice

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DeviceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Nom inconnu"
        val deviceAddress = intent.getStringExtra("DEVICE_ADDRESS") ?: "Adresse inconnue"

        setContent {
            DeviceScreen(deviceName, deviceAddress)
        }
    }

    @Composable
    fun DeviceScreen(deviceName: String, deviceAddress: String) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Appareil sélectionné", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text(text = "Nom : $deviceName", fontSize = 18.sp)
            Text(text = "Adresse : $deviceAddress", fontSize = 16.sp, modifier = Modifier.padding(bottom = 32.dp))

            Button(
                onClick = { showToast("LED Bleue activée") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "LED Bleue")
            }

            Button(
                onClick = { showToast("LED Rouge activée") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "LED Rouge")
            }

            Button(
                onClick = { showToast("LED Verte activée") },
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "LED Verte")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
