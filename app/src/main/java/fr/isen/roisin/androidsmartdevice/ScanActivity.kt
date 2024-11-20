package fr.isen.roisin.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ScanActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScanScreen()
        }
    }

    @Composable
    fun ScanScreen() {
        var isScanning by remember { mutableStateOf(false) }
        val devices = remember { mutableStateListOf<BluetoothDevice>() }
        val seenAddresses = remember { mutableStateListOf<String>() } // Pour éviter les doublons
        val context = LocalContext.current

        val requestPermissionsLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (!allGranted) {
                Toast.makeText(context, "Permissions nécessaires pour scanner les appareils BLE.", Toast.LENGTH_SHORT).show()
            }
        }

        LaunchedEffect(Unit) {
            requestPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isScanning) "Scan en cours..." else "Appuyez pour lancer le scan BLE",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )

            Button(
                onClick = {
                    if (isScanning) {
                        stopScan()
                        isScanning = false
                    } else {
                        devices.clear()
                        seenAddresses.clear()
                        startScan(devices, seenAddresses)
                        isScanning = true
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = if (isScanning) "Arrêter le scan" else "Démarrer le scan")
            }

            Text(
                text = "Appareils détectés (${devices.size}) :",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 16.dp)
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(devices) { device ->
                    DeviceCard(device)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    @Composable
    fun DeviceCard(device: BluetoothDevice) {
        val context = LocalContext.current
        val deviceName = device.name ?: "Nom inconnu"
        val deviceAddress = device.address

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable {
                    val intent = Intent(context, DeviceActivity::class.java).apply {
                        putExtra("DEVICE_NAME", deviceName)
                        putExtra("DEVICE_ADDRESS", deviceAddress)
                    }
                    context.startActivity(intent)
                },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Nom : $deviceName", fontSize = 18.sp)
                Text(text = "Adresse : $deviceAddress", fontSize = 14.sp)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan(
        devices: MutableList<BluetoothDevice>,
        seenAddresses: MutableList<String>
    ) {
        bluetoothLeScanner?.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (device.address !in seenAddresses) {
                    seenAddresses.add(device.address)
                    devices.add(device)
                }
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                for (result in results) {
                    val device = result.device
                    if (device.address !in seenAddresses) {
                        seenAddresses.add(device.address)
                        devices.add(device)
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                Toast.makeText(this@ScanActivity, "Erreur de scan : $errorCode", Toast.LENGTH_SHORT).show()
            }
        })

        handler.postDelayed({
            stopScan()
        }, 10000) // Arrêt automatique après 10 secondes

        Toast.makeText(this, "Scan démarré.", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothLeScanner?.stopScan(object : ScanCallback() {})
        Toast.makeText(this, "Scan arrêté.", Toast.LENGTH_SHORT).show()
    }
}
