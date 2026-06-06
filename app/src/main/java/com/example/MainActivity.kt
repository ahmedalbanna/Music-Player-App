package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.MusicPlayerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MusicPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContainer(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer(viewModel: MusicPlayerViewModel) {
    val context = LocalContext.current

    // Set correct permissions depending on standard Android versioning guidelines
    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    var hasPermissions by remember {
        mutableStateOf(
            requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        hasPermissions = allGranted
        if (allGranted) {
            viewModel.loadDeviceMusic()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            launcher.launch(requiredPermissions)
        }
    }

    // UI state observations
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val deviceSongs by viewModel.deviceSongs.collectAsState()
    val sampleSongs = viewModel.sampleSongs
    val favorites by viewModel.favorites.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isShuffle by viewModel.isShuffleMode.collectAsState()
    val isRepeat by viewModel.isRepeatMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var isPlayerExpanded by remember { mutableStateOf(false) }

    // Unified List Assembly
    val songsToDisplay = remember(deviceSongs, searchQuery, selectedTab, favorites) {
        val totalList = deviceSongs + sampleSongs
        val baseList = if (selectedTab == 0) totalList else {
            val favIds = favorites.map { it.songId }
            totalList.filter { it.id in favIds }
        }

        if (searchQuery.isNotEmpty()) {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }
        } else {
            baseList
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightNavy),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "مشغل صوتيات طرب",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextGlacier,
                                fontSize = 22.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightNavy,
                    titleContentColor = TextGlacier
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightNavy)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (currentSong != null) 76.dp else 0.dp)
            ) {
                // Glow search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_bar"),
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.search_hint),
                            color = TextSilver,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = ActiveCardBg,
                        focusedContainerColor = DeepCharcoal,
                        unfocusedContainerColor = DeepCharcoal,
                        focusedTextColor = TextGlacier,
                        unfocusedTextColor = TextGlacier
                    ),
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )

                // Layout Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MidnightNavy,
                    contentColor = ElectricCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ElectricCyan
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        text = {
                            Text(
                                stringResource(id = R.string.title_all_songs),
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == 0) ElectricCyan else TextSilver
                            )
                        },
                        modifier = Modifier.testTag("tab_all")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(id = R.string.title_favorites),
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == 1) ElectricCyan else TextSilver
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = if (selectedTab == 1) AccentCoral else TextSilver,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_favorites")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Listing container
                if (songsToDisplay.isEmpty()) {
                    EmptyStateWidget(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("songs_list")
                    ) {
                        itemsIndexed(songsToDisplay) { index, song ->
                            val isCurrent = currentSong?.id == song.id
                            val isFav = favorites.any { it.songId == song.id }
                            SongRowItem(
                                song = song,
                                isCurrent = isCurrent,
                                isFavorite = isFav,
                                onPlayClick = {
                                    viewModel.playSong(songsToDisplay, index)
                                },
                                onFavoriteToggle = {
                                    viewModel.toggleFavorite(song)
                                }
                            )
                        }
                    }
                }
            }

            // Bottom Mini Player
            if (currentSong != null) {
                val activeSong = currentSong!!
                MiniPlayerBar(
                    song = activeSong,
                    isPlaying = isPlaying,
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onNext = { viewModel.next() },
                    onBarClick = { isPlayerExpanded = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .testTag("mini_player_inset")
                )
            }
        }
    }

    // Extended Player Cover View
    AnimatedVisibility(
        visible = isPlayerExpanded,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(300)
        )
    ) {
        currentSong?.let { activeSong ->
            val isFav = favorites.any { it.songId == activeSong.id }
            FullPlayerOverlay(
                song = activeSong,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                isShuffle = isShuffle,
                isRepeat = isRepeat,
                isFavorite = isFav,
                onPlayPause = { viewModel.togglePlayPause() },
                onNext = { viewModel.next() },
                onPrevious = { viewModel.previous() },
                onSeek = { pos -> viewModel.seekTo(pos) },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.toggleRepeat() },
                onToggleFavorite = { viewModel.toggleFavorite(activeSong) },
                onCollapse = { isPlayerExpanded = false }
            )
        }
    }
}
