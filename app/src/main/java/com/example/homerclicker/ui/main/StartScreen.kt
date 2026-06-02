package com.example.homerclicker.ui.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.homerclicker.R
import com.example.homerclicker.data.FirebaseManager
import com.example.homerclicker.data.LeaderboardEntry
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import java.io.ByteArrayOutputStream

// Preset Avatars Emoji Map matching the Web version
val emojiMap = mapOf(
    "donut.png" to "🍩",
    "homer.png" to "👨‍🦲",
    "bart" to "🛹",
    "lisa" to "🎷",
    "marge" to "👑"
)

@Composable
fun AvatarImage(photoUrl: String, modifier: Modifier = Modifier) {
    if (photoUrl.startsWith("data:image/")) {
        val base64Data = photoUrl.substringAfter("base64,")
        val bitmap = remember(base64Data) {
            try {
                val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Avatar",
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        } else {
            Text("🍩", modifier = modifier.wrapContentSize(), fontSize = 24.sp)
        }
    } else {
        val emoji = emojiMap[photoUrl] ?: "🍩"
        Text(emoji, modifier = modifier.wrapContentSize(), fontSize = 24.sp)
    }
}

@Composable
fun StartScreen(
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val slipperPointerIcon = remember {
        val nativeIcon: android.view.PointerIcon? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val drawable = context.getDrawable(R.drawable.slipper)
                if (drawable is BitmapDrawable) {
                    val bitmap = drawable.bitmap
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
                    android.view.PointerIcon.create(scaledBitmap, 32f, 32f)
                } else null
            } else null
        } catch (e: Exception) {
            null
        }

        if (nativeIcon != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PointerIcon(nativeIcon)
        } else {
            PointerIcon.Default
        }
    }

    var showSettings by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }

    // Pulsing animation for the preview card glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Deep neon background gradient
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .pointerHoverIcon(slipperPointerIcon),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Spacer to avoid overlapping elements at top
            Spacer(modifier = Modifier.height(40.dp))

            // Title block
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "HOMERO",
                    color = Color(0xFFFFEB3B), // Homer Yellow
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "CHINO",
                    color = Color(0xFFFFEB3B), // Homer Yellow
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "CLICKER",
                    color = Color(0xFFF44336), // Cap Red
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Homer Face Preview Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF)),
                modifier = Modifier
                    .size(220.dp)
                    .scale(pulseScale)
                    .border(2.dp, Color(0x66FFFFFF), RoundedCornerShape(24.dp))
                    .graphicsLayer {
                        shadowElevation = 16f
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x44FFEB3B),
                                        Color(0x00FFEB3B)
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    Image(
                        painter = painterResource(id = R.drawable.homer),
                        contentDescription = "Homer Simpson Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color(0xFFFFEB3B), CircleShape)
                    )
                }
            }

            // Action Buttons Group
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(0.85f)
            ) {
                // Play / Start Button
                Button(
                    onClick = {
                        if (FirebaseManager.currentUser == null) {
                            showLoginDialog = true
                        } else {
                            onStartGame()
                        }
                    },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer { shadowElevation = 8f }
                        .pointerHoverIcon(slipperPointerIcon)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF9900), Color(0xFFFF5E62))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "INICIAR JUEGO",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Global Leaderboard Button
                Button(
                    onClick = { showLeaderboardDialog = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .pointerHoverIcon(slipperPointerIcon)
                ) {
                    Text(
                        text = "🏆 PUNTUACIONES GLOBALES",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- Top Bar HUD (Login status & Settings) ---
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: User profile status
            val user = FirebaseManager.currentUser
            if (user != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), RoundedCornerShape(50))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarImage(photoUrl = user.photoUrl, modifier = Modifier.size(20.dp))
                    }
                    Text(
                        text = user.username,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 100.dp)
                    )
                    Text(
                        text = "🚪",
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { FirebaseManager.logout() }
                            .padding(2.dp)
                    )
                }
            } else {
                Button(
                    onClick = { showLoginDialog = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FFFFFF)),
                    border = BorderStroke(1.dp, Color(0x33FFFFFF)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Registro / Login 🔑", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Right Side: Settings Toggle
            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier.pointerHoverIcon(slipperPointerIcon)
            ) {
                Text("⚙️", fontSize = 28.sp)
            }
        }

        // --- Modals / Overlays Dialogs ---

        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }

        if (showLoginDialog) {
            LoginRegisterDialog(
                onDismiss = { showLoginDialog = false }
            )
        }

        if (showLeaderboardDialog) {
            LeaderboardDialog(
                onDismiss = { showLeaderboardDialog = false }
            )
        }
    }
}


// ==================== REGISTRATION & LOGIN COMPOSE DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginRegisterDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var selectedAvatarKey by remember { mutableStateOf("donut.png") }
    var uploadStatusText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
 
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                isLoading = true
                FirebaseManager.loginWithGoogle(context, idToken) { success ->
                    isLoading = false
                    if (success) {
                        Toast.makeText(context, "¡Sesión iniciada con Google!", Toast.LENGTH_SHORT).show()
                        onDismiss()
                    } else {
                        Toast.makeText(context, "Error al iniciar sesión con Firebase.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "No se pudo obtener el ID token de Google.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Fallo el inicio de sesión con Google: Código ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }

    // Launcher for Custom Photo Uploading
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    // Resize to 80x80 thumbnail image Base64 to save database space
                    val resized = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
                    val outputStream = ByteArrayOutputStream()
                    resized.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                    val bytes = outputStream.toByteArray()
                    val base64 = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
                    
                    selectedAvatarKey = base64
                    uploadStatusText = "¡Imagen lista! 📁"
                } else {
                    uploadStatusText = "Error al abrir la imagen."
                }
            } catch(e: Exception) {
                uploadStatusText = "Error al cargar archivo."
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14232D)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🔑 REGISTRO DE JUGADOR",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Regístrate para guardar tu récord en la nube y competir globalmente",
                    color = Color(0x99FFFFFF),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )

                // Google Login Actual
                Button(
                    onClick = {
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar con Google 🔴", color = Color(0xFF333333), fontWeight = FontWeight.Bold)
                }

                // Divider Line
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x22FFFFFF)))
                    Text("o registro manual", color = Color(0x66FFFFFF), fontSize = 11.sp)
                    Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0x22FFFFFF)))
                }

                // Text Input
                TextField(
                    value = username,
                    onValueChange = { if (it.length <= 15) username = it },
                    placeholder = { Text("Nombre de usuario", color = Color(0x66FFFFFF)) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0x22000000),
                        unfocusedContainerColor = Color(0x22000000),
                        focusedIndicatorColor = Color(0xFFFFEB3B),
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Avatar options selector titles
                Text(
                    text = "Selecciona tu Avatar:",
                    color = Color(0xAAFFFFFF),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Avatars Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    emojiMap.forEach { (key, emoji) ->
                        val isSelected = selectedAvatarKey == key
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0x33FFEB3B) else Color(0x11FFFFFF))
                                .border(
                                    2.dp,
                                    if (isSelected) Color(0xFFFFEB3B) else Color.Transparent,
                                    CircleShape
                                )
                                .clickable {
                                    selectedAvatarKey = key
                                    uploadStatusText = ""
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 22.sp)
                        }
                    }
                }

                // File upload trigger
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📁 Subir foto personalizada",
                        color = Color(0xFFFFEB3B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { imagePickerLauncher.launch("image/*") }
                            .padding(vertical = 4.dp)
                    )
                    if (uploadStatusText.isNotEmpty()) {
                        Text(uploadStatusText, color = Color(0xFF38EF7D), fontSize = 11.sp)
                    }
                }

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar", color = Color.White)
                    }
                    Button(
                        onClick = {
                            val trimmed = username.trim()
                            if (trimmed.isEmpty()) {
                                Toast.makeText(context, "Escribe tu nombre de usuario.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isLoading = true
                            FirebaseManager.registerManual(
                                context = context,
                                username = trimmed,
                                photoUrl = selectedAvatarKey
                            ) { success ->
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "¡Usuario registrado!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Text("Registrar", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}


// ==================== GLOBAL LEADERBOARD COMPOSE DIALOG ====================

@Composable
fun LeaderboardDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var leaderboardList by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseManager.fetchLeaderboard(context) { entries ->
            leaderboardList = entries
            isLoading = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14232D)),
            border = BorderStroke(1.dp, Color(0x33FFFFFF)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 PUNTUACIONES GLOBALES",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Top 50 más rápidos en alcanzar 1,000 clicks",
                    color = Color(0x99FFFFFF),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                if (isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFFEB3B))
                    }
                } else if (leaderboardList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Ningún récord registrado aún.\n¡Sé el primero!",
                            color = Color(0x66FFFFFF),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        itemsIndexed(leaderboardList) { idx, entry ->
                            val rank = idx + 1
                            val isCurrentUser = FirebaseManager.currentUser?.uid == entry.uid
                            
                            val rankColor = when (rank) {
                                1 -> Color(0xFFFFD13B)
                                2 -> Color(0xFFE0E0E0)
                                3 -> Color(0xFFCD7F32)
                                else -> Color.White
                            }
                            
                            val rankText = when (rank) {
                                1 -> "🥇"
                                2 -> "🥈"
                                3 -> "🥉"
                                else -> "$rank"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isCurrentUser) Color(0x33FFEB3B) else Color(0x0AFFFFFF),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isCurrentUser) Color(0xFFFFEB3B) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rankText,
                                    color = rankColor,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0x22FFFFFF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AvatarImage(photoUrl = entry.photoUrl, modifier = Modifier.size(24.dp))
                                    }
                                    Text(
                                        text = entry.username,
                                        color = if (isCurrentUser) Color(0xFFFFEB3B) else Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = entry.formattedTime,
                                    color = Color(0xFF38EF7D),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar", color = Color.White)
                }
            }
        }
    }
}
