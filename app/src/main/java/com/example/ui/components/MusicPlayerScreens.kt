package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Song
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

// --- Custom Vector Icons ---

@Composable
fun SkipNextIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.25f)
            lineTo(w * 0.65f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.75f)
            close()
        }
        drawPath(path, color = color)
        drawRect(
            color = color,
            topLeft = Offset(w * 0.7f, h * 0.25f),
            size = Size(w * 0.1f, h * 0.5f)
        )
    }
}

@Composable
fun SkipPreviousIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.35f, h * 0.5f)
            lineTo(w * 0.75f, h * 0.75f)
            close()
        }
        drawPath(path, color = color)
        drawRect(
            color = color,
            topLeft = Offset(w * 0.2f, h * 0.25f),
            size = Size(w * 0.1f, h * 0.5f)
        )
    }
}

@Composable
fun ShuffleIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Draw intersecting wave lines representing shuffle
        val strokeWidth = 3f
        val path1 = Path().apply {
            moveTo(w * 0.15f, h * 0.3f)
            cubicTo(w * 0.4f, h * 0.3f, w * 0.6f, h * 0.7f, w * 0.85f, h * 0.7f)
        }
        val path2 = Path().apply {
            moveTo(w * 0.15f, h * 0.7f)
            cubicTo(w * 0.4f, h * 0.7f, w * 0.6f, h * 0.3f, w * 0.85f, h * 0.3f)
        }
        drawPath(path1, color = color, style = Stroke(strokeWidth))
        drawPath(path2, color = color, style = Stroke(strokeWidth))
        
        // Arrow heads
        drawPath(
            path = Path().apply {
                moveTo(w * 0.78f, h * 0.62f)
                lineTo(w * 0.85f, h * 0.7f)
                lineTo(w * 0.78f, h * 0.78f)
            },
            color = color,
            style = Stroke(strokeWidth)
        )
        drawPath(
            path = Path().apply {
                moveTo(w * 0.78f, h * 0.22f)
                lineTo(w * 0.85f, h * 0.3f)
                lineTo(w * 0.78f, h * 0.38f)
            },
            color = color,
            style = Stroke(strokeWidth)
        )
    }
}

@Composable
fun RepeatIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeWidth = 3f
        // Draw looped arrows
        drawArc(
            color = color,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.2f),
            size = Size(w * 0.6f, h * 0.6f),
            style = Stroke(strokeWidth)
        )
        // Arrow head for loop
        drawPath(
            path = Path().apply {
                moveTo(w * 0.42f, h * 0.12f)
                lineTo(w * 0.5f, h * 0.2f)
                lineTo(w * 0.42f, h * 0.28f)
            },
            color = color,
            style = Stroke(strokeWidth)
        )
    }
}

// --- Empty Placeholder / Illustration ---

@Composable
fun EmptyStateWidget(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Brush.radialGradient(listOf(ElectricCyan.copy(alpha = 0.2f), Color.Transparent)))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = ElectricCyan
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.empty_song_list),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSilver,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            ),
            modifier = Modifier.fillMaxWidth(0.85f)
        )
    }
}

// --- Component: Song Row Item ---

@Composable
fun SongRowItem(
    song: Song,
    isCurrent: Boolean,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerBg = if (isCurrent) ActiveCardBg else Color.Transparent
    val accentBorder = if (isCurrent) ElectricCyan else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerBg)
            .clickable(onClick = onPlayClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail with glowing disc pattern
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isCurrent) ElectricCyan.copy(alpha = 0.2f) else DeepCharcoal),
            contentAlignment = Alignment.Center
        ) {
            if (isCurrent) {
                // Moving frequency bars simulation
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val height1 by infiniteTransition.animateFloat(
                    initialValue = 0.3f, targetValue = 0.9f,
                    animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse), label = "h1"
                )
                val height2 by infiniteTransition.animateFloat(
                    initialValue = 0.8f, targetValue = 0.2f,
                    animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse), label = "h2"
                )
                val height3 by infiniteTransition.animateFloat(
                    initialValue = 0.4f, targetValue = 0.95f,
                    animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse), label = "h3"
                )

                Row(
                    modifier = Modifier.fillMaxSize(0.6f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(height1).background(ElectricCyan, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(height2).background(ElectricCyan, RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(height3).background(ElectricCyan, RoundedCornerShape(2.dp)))
                }
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = if (song.isSample) SoftIndigo else TextSilver,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Titles and Text Details (RTL Optimized Alignments)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) ElectricCyan else TextGlacier,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSilver,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Song duration and favorite toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = song.durationText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSilver,
                    fontWeight = FontWeight.Bold
                )
            )

            IconButton(
                onClick = onFavoriteToggle,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("favorite_button_${song.id}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "المفضلة",
                    tint = if (isFavorite) AccentCoral else TextSilver,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- Component: Spinning Graphic Record ---

@Composable
fun SpinningRecordDisk(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val currentRotation = if (isPlaying) rotation else 0f

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .fillMaxWidth(0.7f)
            .shadow(24.dp, shape = CircleShape, ambientColor = ElectricCyan, spotColor = MidnightNavy)
            .clip(CircleShape)
            .background(MidnightNavy)
            .rotate(currentRotation),
        contentAlignment = Alignment.Center
    ) {
        // Flat vinyl grooves design drawing with core Canvas features
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.width / 2

            // Draw solid vinyl background
            drawCircle(color = Color(0xFF111422), radius = outerRadius)

            // Draw sound grooves
            for (i in 2..8) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = outerRadius * (i * 0.1f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Draw stylized light reflections on vinyl
            drawArc(
                color = Color.White.copy(alpha = 0.03f),
                startAngle = 45f,
                sweepAngle = 30f,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = size
            )
            drawArc(
                color = Color.White.copy(alpha = 0.03f),
                startAngle = 225f,
                sweepAngle = 30f,
                useCenter = true,
                topLeft = Offset(0f, 0f),
                size = size
            )
        }

        // Inner label sticker
        Box(
            modifier = Modifier
                .fillMaxSize(0.35f)
                .clip(CircleShape)
                .background(Brush.sweepGradient(listOf(ElectricCyan, SoftIndigo, ElectricCyan))),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .clip(CircleShape)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// --- Bottom Mini Player Bar ---

@Composable
fun MiniPlayerBar(
    song: Song,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    onNext: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color(0xFF1E2640))
            .clickable(onClick = onBarClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Record Disc Animation in Mini Bar
        val infiniteTransition = rememberInfiniteTransition(label = "mini_disc")
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing)),
            label = "mini_rot"
        )
        val currentRotation = if (isPlaying) rotation else 0f

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(DeepCharcoal)
                .rotate(currentRotation),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(color = Color(0xFF1A1F38))
                drawCircle(color = ElectricCyan, radius = size.width * 0.15f)
            }
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = SoftIndigo,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Titles (RTL Direction)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSilver,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Mini player controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onPlayPauseToggle,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("mini_play_pause")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "تشغيل/إيقاف مؤقت",
                    tint = ElectricCyan,
                    modifier = Modifier.size(30.dp)
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("mini_next")
            ) {
                SkipNextIcon(
                    modifier = Modifier.size(24.dp),
                    color = ElectricCyan
                )
            }
        }
    }
}

// --- Extended Full Player Sheet / Overlay ---

@Composable
fun FullPlayerOverlay(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isShuffle: Boolean,
    isRepeat: Boolean,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draggingPosition by remember { mutableStateOf<Long?>(null) }
    val displayPosition = draggingPosition ?: currentPosition

    val totalSeconds = duration / 1000
    val currentSeconds = displayPosition / 1000

    val currentMin = currentSeconds / 60
    val currentSec = currentSeconds % 60
    val totalMin = totalSeconds / 60
    val totalSec = totalSeconds % 60

    val timeNow = String.format("%02d:%02d", currentMin, currentSec)
    val timeTotal = String.format("%02d:%02d", totalMin, totalSec)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightNavy)
            .padding(24.dp)
    ) {
        // Decorative glowing ambient light in top corner
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .background(Brush.radialGradient(listOf(ElectricCyan.copy(alpha = 0.15f), Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Drop down and Info triggers (Standard 48dp component targets)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_collapse")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "الرجوع",
                        tint = TextGlacier,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = stringResource(id = R.string.now_playing),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextGlacier
                    )
                )

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("player_favorite")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (isFavorite) AccentCoral else TextGlacier,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Middle: Elegant Rotating Vinyl Label disc card
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpinningRecordDisk(isPlaying = isPlaying)
            }

            // Lower Core Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Audio Details (Titles)
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ElectricCyan,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSilver,
                        textAlign = TextAlign.Center
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Progress SeekBar and Time labels
                Slider(
                    value = if (duration > 0) displayPosition.toFloat() else 0f,
                    onValueChange = { draggingPosition = it.toLong() },
                    onValueChangeFinished = {
                        draggingPosition?.let { onSeek(it) }
                        draggingPosition = null
                    },
                    valueRange = 0f..(duration.toFloat().coerceAtLeast(1f)),
                    colors = SliderDefaults.colors(
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = ActiveCardBg,
                        thumbColor = ElectricCyan
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_slider")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = timeNow,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSilver,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = timeTotal,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = TextSilver,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Playback Control Button Rows (Strict 48dp+ interactive dimensions)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(
                        onClick = onToggleShuffle,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_shuffle")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ShuffleIcon(
                                modifier = Modifier.size(24.dp),
                                color = if (isShuffle) ElectricCyan else TextSilver
                            )
                            if (isShuffle) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan)
                                )
                            }
                        }
                    }

                    // Previous
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_prev")
                    ) {
                        SkipPreviousIcon(
                            modifier = Modifier.size(28.dp),
                            color = TextGlacier
                        )
                    }

                    // Main Center Play Pause
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .shadow(12.dp, CircleShape, ambientColor = ElectricCyan)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                            .clickable(onClick = onPlayPause)
                            .testTag("player_play_pause"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "تشغيل أو إيقاف",
                            tint = MidnightNavy,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Next
                    IconButton(
                        onClick = onNext,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_next")
                    ) {
                        SkipNextIcon(
                            modifier = Modifier.size(28.dp),
                            color = TextGlacier
                        )
                    }

                    // Repeat
                    IconButton(
                        onClick = onToggleRepeat,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("player_repeat")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            RepeatIcon(
                                modifier = Modifier.size(24.dp),
                                color = if (isRepeat) ElectricCyan else TextSilver
                            )
                            if (isRepeat) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
