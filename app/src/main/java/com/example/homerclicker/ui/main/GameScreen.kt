package com.example.homerclicker.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.media.SoundPool
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.homerclicker.R
import com.example.homerclicker.data.FirebaseManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class SlapEffect(val id: Long, val x: Float, val y: Float)

data class DonutRainParticle(
    val id: Int,
    val startXFraction: Float,
    val durationMs: Int,
    val delayMs: Int,
    val sizeDp: Int,
    val rotationSpeed: Float
)

@Composable
fun FallingDonut(
    particle: DonutRainParticle,
    screenWidth: Float,
    screenHeight: Float
) {
    val density = LocalDensity.current
    val animProgress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        delay(particle.delayMs.toLong())
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = particle.durationMs,
                easing = LinearEasing
            )
        )
    }

    if (animProgress.value > 0f && animProgress.value < 1f) {
        val x = particle.startXFraction * screenWidth
        val startY = -150f
        val endY = screenHeight + 150f
        val y = startY + (endY - startY) * animProgress.value
        val rotation = particle.rotationSpeed * animProgress.value
        val sizePx = with(density) { particle.sizeDp.dp.toPx() }

        Image(
            painter = painterResource(id = R.drawable.donut),
            contentDescription = null,
            modifier = Modifier
                .offset { IntOffset((x - sizePx / 2).roundToInt(), y.roundToInt()) }
                .size(particle.sizeDp.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
        )
    }
}

@Composable
fun GameScreen(
    onBackToStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sharedPrefs = remember { context.getSharedPreferences("homer_clicker_prefs", Context.MODE_PRIVATE) }

    // Slipper Custom Pointer Icon
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

    // SoundPool Initialization
    val soundPool = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(android.media.AudioAttributes.USAGE_GAME)
                        .build()
                )
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0)
        }
    }

    val soundMap = remember { mutableMapOf<Int, Int>() }

    DisposableEffect(Unit) {
        soundMap[R.raw.homero_normal] = soundPool.load(context, R.raw.homero_normal, 1)
        soundMap[R.raw.homero_hola] = soundPool.load(context, R.raw.homero_hola, 1)
        soundMap[R.raw.simpson_wmv] = soundPool.load(context, R.raw.simpson_wmv, 1)
        soundMap[R.raw.ay_que_rico_homero] = soundPool.load(context, R.raw.ay_que_rico_homero, 1)
        soundMap[R.raw.homero_gimiendo] = soundPool.load(context, R.raw.homero_gimiendo, 1)

        onDispose {
            soundPool.release()
        }
    }

    val playSound = { resId: Int ->
        if (sharedPrefs.getBoolean("sound_enabled", true)) {
            val soundId = soundMap[resId]
            if (soundId != null && soundId != 0) {
                soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            }
        }
    }

    // Vibrator Setup
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator }
    val triggerVibration = {
        if (sharedPrefs.getBoolean("vibration_enabled", true)) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            } catch (e: Exception) {
                // Fail-safe
            }
        }
    }

    // Deep neon background gradient matching start screen
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F2027),
            Color(0xFF203A43),
            Color(0xFF2C5364)
        )
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .pointerHoverIcon(slipperPointerIcon)
    ) {
        val layoutWidthPx = constraints.maxWidth.toFloat()
        val layoutHeightPx = constraints.maxHeight.toFloat()
        
        val homerSize = 140.dp
        val homerSizePx = with(density) { homerSize.toPx() }

        // Donut Rain Effect
        val donutParticles = remember {
            List(25) { id ->
                DonutRainParticle(
                    id = id,
                    startXFraction = kotlin.random.Random.nextFloat(),
                    durationMs = kotlin.random.Random.nextInt(2500, 4500),
                    delayMs = kotlin.random.Random.nextInt(0, 1500),
                    sizeDp = kotlin.random.Random.nextInt(35, 65),
                    rotationSpeed = kotlin.random.Random.nextInt(-180, 180).toFloat()
                )
            }
        }

        donutParticles.forEach { particle ->
            FallingDonut(
                particle = particle,
                screenWidth = layoutWidthPx,
                screenHeight = layoutHeightPx
            )
        }

        var clicks by remember { mutableStateOf(0) }
        
        // Physics state for bouncing
        var posX by remember { mutableStateOf(0f) }
        var posY by remember { mutableStateOf(0f) }
        var vx by remember { mutableStateOf(7f) }
        var vy by remember { mutableStateOf(9f) }

        val scope = rememberCoroutineScope()

        // Juiciness scale anim for click feedback
        var scaleTarget by remember { mutableStateOf(1f) }
        val scale by animateFloatAsState(
            targetValue = scaleTarget,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "clickScale"
        )

        // Stopwatch Timing State
        var elapsedTime by remember { mutableStateOf(0L) }
        var timerStarted by remember { mutableStateOf(false) }
        var timerActive by remember { mutableStateOf(false) }
        var startTime by remember { mutableStateOf(0L) }

        // Results Dialog state
        var showResultsDialog by remember { mutableStateOf(false) }
        var rankText by remember { mutableStateOf("-") }
        var bestTimeText by remember { mutableStateOf("-") }
        var isNewRecord by remember { mutableStateOf(false) }

        // Speech bubble message variables
        var showSpeechBubble by remember { mutableStateOf(false) }
        var bubbleText by remember { mutableStateOf("") }
        var bubbleJob by remember { mutableStateOf<Job?>(null) }

        // Slap Particles state
        val slaps = remember { mutableStateListOf<SlapEffect>() }

        // Position Homer in the center initially
        LaunchedEffect(layoutWidthPx, layoutHeightPx) {
            if (layoutWidthPx > 0f && layoutHeightPx > 0f) {
                posX = (layoutWidthPx - homerSizePx) / 2
                posY = (layoutHeightPx - homerSizePx) / 2
            }
        }

        // Bouncing physics engine (triggered at 1000 clicks)
        if (clicks >= 1000) {
            LaunchedEffect(layoutWidthPx, layoutHeightPx) {
                while (true) {
                    withFrameMillis {
                        var nextX = posX + vx
                        var nextY = posY + vy

                        // X-axis collision with bounce
                        if (nextX <= 0f) {
                            nextX = 0f
                            vx = -vx
                        } else if (nextX >= layoutWidthPx - homerSizePx) {
                            nextX = layoutWidthPx - homerSizePx
                            vx = -vx
                        }

                        // Y-axis collision with bounce
                        if (nextY <= 0f) {
                            nextY = 0f
                            vy = -vy
                        } else if (nextY >= layoutHeightPx - homerSizePx) {
                            nextY = layoutHeightPx - homerSizePx
                            vy = -vy
                        }

                        posX = nextX
                        posY = nextY
                    }
                }
            }
        }

        // Click handler
        val handleHomerClick = { tapX: Float, tapY: Float ->
            // Trigger vibration haptics
            triggerVibration()

            // Spawn slap particle
            val slap = SlapEffect(System.currentTimeMillis(), tapX, tapY)
            slaps.add(slap)
            scope.launch {
                delay(300)
                slaps.remove(slap)
            }

            if (clicks < 1000) {
                clicks++

                // Start timer on the first click
                if (clicks == 1 && !timerStarted) {
                    timerStarted = true
                    timerActive = true
                    startTime = System.currentTimeMillis()
                    scope.launch {
                        while (timerActive) {
                            elapsedTime = System.currentTimeMillis() - startTime
                            delay(16) // ~60fps
                        }
                    }
                }

                // Run bouncy scale animation
                scope.launch {
                    scaleTarget = 0.85f
                    delay(40)
                    scaleTarget = 1.15f
                    delay(40)
                    scaleTarget = 1.0f
                }

                // Periodic message every 10 clicks
                if (clicks % 10 == 0 && clicks > 0) {
                    // Play milestone sound
                    val milestoneSounds = listOf(R.raw.ay_que_rico_homero, R.raw.homero_gimiendo)
                    playSound(milestoneSounds.random())

                    val bubbleMessages = listOf(
                        "oh si tocame mas fuerte",
                        "tu mamá me apretaba mas fuerte",
                        "¿eso es todo lo que tienes, gordit@?",
                        "¡más rápido! ¡las donuts no se ganan solas!",
                        "¡dame más clics, me gusta la fricción!",
                        "¡vamos, mis llantas necesitan cariño!",
                        "¡uf, qué manos tan suaves!",
                        "¡no pares, ya casi tocas pasto!"
                    )
                    bubbleText = bubbleMessages.random()
                    showSpeechBubble = true
                    bubbleJob?.cancel()
                    bubbleJob = scope.launch {
                        delay(2000)
                        showSpeechBubble = false
                    }
                } else {
                    // Play normal click sound
                    val normalSounds = listOf(R.raw.homero_normal, R.raw.homero_hola, R.raw.simpson_wmv)
                    playSound(normalSounds.random())
                }

                // Game Finished Trigger
                if (clicks >= 1000) {
                    timerActive = false
                    val finalTime = elapsedTime
                    playSound(R.raw.homero_hola)
                    
                    FirebaseManager.saveScore(context, finalTime) {
                        FirebaseManager.getPersonalBest(context) { best ->
                            val isNew = best == null || finalTime <= best
                            isNewRecord = isNew
                            bestTimeText = String.format("%.3fs", (best ?: finalTime) / 1000.0)
                            FirebaseManager.getRanking(context) { rank ->
                                rankText = rank
                                showResultsDialog = true
                            }
                        }
                    }
                }
            } else {
                // Keep clicking mini-challenge when bouncing!
                clicks++
                val normalSounds = listOf(R.raw.homero_normal, R.raw.homero_hola, R.raw.simpson_wmv)
                playSound(normalSounds.random())
            }
        }

        // Main Character and Speech bubble container
        Box(
            modifier = Modifier
                .offset { IntOffset(posX.roundToInt(), posY.roundToInt()) }
                .size(homerSize)
        ) {
            // Clickable Homer Avatar (using pointerInput to get offset)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val parentX = posX + offset.x
                            val parentY = posY + offset.y
                            handleHomerClick(parentX, parentY)
                        }
                    }
                    .pointerHoverIcon(slipperPointerIcon)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.homer),
                    contentDescription = "Homer Click Target",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(4.dp, Color(0xFFFFEB3B), CircleShape)
                )
            }

            // Speech Bubble floating above
            if (showSpeechBubble && clicks < 1000) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-65).dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFFFF5E62), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = bubbleText,
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Slap Particles Overlay
        slaps.forEach { slap ->
            val anim = remember { Animatable(0f) }
            LaunchedEffect(slap.id) {
                anim.animateTo(1f, tween(durationMillis = 300, easing = LinearEasing))
            }
            
            Image(
                painter = painterResource(id = R.drawable.slipper),
                contentDescription = null,
                modifier = Modifier
                    .offset {
                        val offsetXDp = with(density) { 50.dp.toPx() }.roundToInt()
                        val offsetYDp = with(density) { 50.dp.toPx() }.roundToInt()
                        IntOffset(slap.x.roundToInt() - offsetXDp, slap.y.roundToInt() - offsetYDp)
                    }
                    .size(100.dp)
                    .graphicsLayer {
                        alpha = 1f - anim.value
                        rotationZ = -45f * anim.value
                        scaleX = 1f + 0.2f * anim.value
                        scaleY = 1f + 0.2f * anim.value
                    }
            )
        }

        var showSettings by remember { mutableStateOf(false) }

        if (showSettings) {
            SettingsDialog(onDismiss = { showSettings = false })
        }

        // Top Navigation and Stats Overlays
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                    .clickable { onBackToStart() }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .pointerHoverIcon(slipperPointerIcon)
            ) {
                Text(
                    text = "↩ Volver",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Settings Icon
                IconButton(
                    onClick = { showSettings = true },
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), CircleShape)
                        .size(36.dp)
                        .pointerHoverIcon(slipperPointerIcon)
                ) {
                    Text("⚙️", fontSize = 18.sp)
                }

                // Clicks count badge
                Box(
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "🎯 $clicks/1000",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Stopwatch count badge
                Box(
                    modifier = Modifier
                        .background(Color(0x33FFFFFF), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0x66FFFFFF), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    val seconds = String.format("%.3fs", elapsedTime / 1000.0)
                    Text(
                        text = "⏱️ $seconds",
                        color = Color(0xFFFFEB3B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 1000 clicks message banner
        if (clicks >= 1000) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 110.dp, start = 24.dp, end = 24.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF11998E), Color(0xFF38EF7D))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "okey ve a tocar pasto gordit@",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }

            // Reset Game Button to allow trying again
            Button(
                onClick = {
                    clicks = 0
                    elapsedTime = 0
                    timerStarted = false
                    timerActive = false
                    showSpeechBubble = false
                    posX = (layoutWidthPx - homerSizePx) / 2
                    posY = (layoutHeightPx - homerSizePx) / 2
                    vx = 7f
                    vy = 9f
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .height(50.dp)
                    .graphicsLayer { shadowElevation = 8f }
                    .pointerHoverIcon(slipperPointerIcon)
            ) {
                Text(
                    text = "Reiniciar Juego",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Victory Results Dialog Overlay ---
        if (showResultsDialog) {
            Dialog(onDismissRequest = { }) {
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
                            text = "🎉 ¡META COMPLETADA! 🎉",
                            color = Color(0xFFFFEB3B),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Has tocado a Homero 1,000 veces",
                            color = Color.White,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )

                        // Digital Clock Display
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x11FFFFFF), RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "TIEMPO FINAL",
                                color = Color(0x99FFFFFF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val finalSecs = String.format("%.3fs", elapsedTime / 1000.0)
                            Text(
                                text = finalSecs,
                                color = Color(0xFF38EF7D),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        if (isNewRecord) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(Color(0xFFF12711), Color(0xFFF5AF19))
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔥 ¡NUEVO RÉCORD PERSONAL! 🔥",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Personal best stats
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Text("MEJOR MARCA", color = Color(0x66FFFFFF), fontSize = 10.sp)
                                Text(
                                    text = bestTimeText,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Rank stats
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                Text("RANKING GLOBAL", color = Color(0x66FFFFFF), fontSize = 10.sp)
                                Text(
                                    text = rankText,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Close/play again button
                        Button(
                            onClick = {
                                clicks = 0
                                elapsedTime = 0
                                timerStarted = false
                                timerActive = false
                                showResultsDialog = false
                                showSpeechBubble = false
                                posX = (layoutWidthPx - homerSizePx) / 2
                                posY = (layoutHeightPx - homerSizePx) / 2
                                vx = 7f
                                vy = 9f
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Volver a Jugar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
