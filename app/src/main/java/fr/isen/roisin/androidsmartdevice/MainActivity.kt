package fr.isen.roisin.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.activity.compose.rememberLauncherForActivityResult


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WelcomeScreen()
        }
    }
}

@Composable
fun WelcomeScreen() {
    val context = LocalContext.current
    val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    if (bluetoothAdapter == null) {
        Toast.makeText(context, "Le Bluetooth n'est pas supporté sur cet appareil.", Toast.LENGTH_LONG).show()
        return
    }

    var showDialog by remember { mutableStateOf(!bluetoothAdapter.isEnabled) }

    val requestBluetoothPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                context.startActivity(enableBtIntent)
            }
            showDialog = false
        } else {
            Toast.makeText(context, "Bluetooth requis pour continuer.", Toast.LENGTH_SHORT).show()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Bluetooth désactivé") },
            text = { Text("Voulez-vous activer le Bluetooth ?") },
            confirmButton = {
                Button(onClick = { requestBluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT) }) {
                    Text("Activer")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.TopCenter)) {
            Text(
                text = "Bienvenue dans votre application Smart Device",
                color = Color(0xFF3399FF),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 55.dp)
            )
            Text(
                text = "Pour démarrer vos interactions avec les appareils BLE, cliquez sur START",
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 32.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.ble),
                contentDescription = "BLE Image",
                modifier = Modifier
                    .requiredSize(150.dp)
                    .padding(top = 50.dp)
            )
        }

        Button(
            onClick = {
                if (bluetoothAdapter.isEnabled) {
                    val intent = Intent(context, ScanActivity::class.java)
                    context.startActivity(intent)
                } else {
                    showDialog = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3399FF)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .fillMaxWidth(0.6f)
        ) {
            Text(
                text = "START",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}
