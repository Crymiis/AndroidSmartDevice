package fr.isen.roisin.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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

        // Gestion des permissions
        val requestPermissionsLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            if (!allGranted) {
                Toast.makeText(context, "Permissions nécessaires pour scanner les appareils BLE.", Toast.LENGTH_SHORT).show()
            }
        }

        // Demande des permissions au lancement
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
                        devices.clear() // Nettoyer les appareils détectés
                        seenAddresses.clear() // Nettoyer les adresses uniques
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

            if (devices.isEmpty()) {
                Text(
                    text = "Aucun appareil détecté.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    devices.forEach { device ->
                        DeviceItem(device)
                    }
                }
            }
        }
    }

    @Composable
    fun DeviceItem(device: BluetoothDevice) {
        val deviceName = device.name ?: "Nom inconnu"
        val deviceAddress = device.address

        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text(text = "Nom : $deviceName", fontSize = 16.sp)
            Text(text = "Adresse : $deviceAddress", fontSize = 14.sp)
        }
    }

    private fun startScan(
        devices: MutableList<BluetoothDevice>,
        seenAddresses: MutableList<String>
    ) {
        bluetoothLeScanner?.startScan(object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (device.address !in seenAddresses) {
                    seenAddresses.add(device.address) // Ajouter une adresse unique
                    devices.add(device) // Ajouter l'appareil correspondant
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

    private fun stopScan() {
        bluetoothLeScanner?.stopScan(object : ScanCallback() {})
        Toast.makeText(this, "Scan arrêté.", Toast.LENGTH_SHORT).show()
    }
}
