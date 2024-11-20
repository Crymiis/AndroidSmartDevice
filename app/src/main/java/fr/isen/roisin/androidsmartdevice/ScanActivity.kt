package fr.isen.roisin.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

class ScanActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScanScreen()
        }
    }

    @Composable
    fun ScanScreen() {
        var isScanning by remember { mutableStateOf(false) }
        val context = LocalContext.current

        val requestBluetoothPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permission Bluetooth nécessaire.", Toast.LENGTH_LONG).show()
            }
        }

        val requestLocationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Permission de localisation nécessaire.", Toast.LENGTH_LONG).show()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isScanning) "Scan en cours..." else "Lancer le scan BLE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Image(
                    painter = painterResource(id = if (isScanning) R.drawable.stop else R.drawable.start),
                    contentDescription = if (isScanning) "Stop" else "Start",
                    modifier = Modifier
                        .size(60.dp)
                        .clickable {
                            if (!isScanning) {
                                requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            isScanning = !isScanning
                        }
                )
            }
        }
    }
}
