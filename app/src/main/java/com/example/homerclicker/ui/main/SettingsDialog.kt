package com.example.homerclicker.ui.main

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("homer_clicker_prefs", Context.MODE_PRIVATE) }
    
    var isSoundEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("sound_enabled", true)) }
    var isVibrationEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("vibration_enabled", true)) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF152A38)),
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, Color(0xFFFFEB3B), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "⚙️ OPCIONES",
                    color = Color(0xFFFFEB3B),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Sound Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔊 Sonido",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = {
                            isSoundEnabled = it
                            sharedPrefs.edit().putBoolean("sound_enabled", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFEB3B),
                            checkedTrackColor = Color(0xFF4CAF50)
                        )
                    )
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x33FFFFFF))
                        .padding(vertical = 8.dp)
                )
                
                // Vibration Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📳 Vibración",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Switch(
                        checked = isVibrationEnabled,
                        onCheckedChange = {
                            isVibrationEnabled = it
                            sharedPrefs.edit().putBoolean("vibration_enabled", it).apply()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFFEB3B),
                            checkedTrackColor = Color(0xFF4CAF50)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5E62)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Guardar y Cerrar",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
