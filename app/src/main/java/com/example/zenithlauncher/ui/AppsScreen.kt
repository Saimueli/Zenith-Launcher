package com.example.zenithlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.zenithlauncher.ui.theme.DarkGray
import com.example.zenithlauncher.ui.theme.MutedGray
import com.example.zenithlauncher.ui.theme.PureBlack
import com.example.zenithlauncher.ui.theme.PureWhite

@Composable
fun AppsScreen(viewModel: ZenithViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val installedApps by viewModel.installedApps.collectAsState()

    var showFullDrawer by remember { mutableStateOf(false) }
    var showHiddenApps by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedAppForDialog by remember { mutableStateOf<AppInfo?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
    }

    val favoriteApps by remember(installedApps) {
        derivedStateOf {
            installedApps.filter { it.isFavorite && !it.isHidden }.take(6)
        }
    }

    val visibleApps by remember(installedApps, searchQuery) {
        derivedStateOf {
            installedApps.filter { !it.isHidden && (searchQuery.isBlank() || it.label.contains(searchQuery.trim(), ignoreCase = true)) }
        }
    }

    val hiddenApps by remember(installedApps) {
        derivedStateOf {
            installedApps.filter { it.isHidden }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        if (!showFullDrawer) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Zenith",
                        color = PureWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    Text(
                        text = "FAVORITES",
                        color = MutedGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    if (favoriteApps.isEmpty()) {
                        Text(
                            text = "No favorite apps pinned.\nTap 'VIEW ALL APPS' below or long-press an app to pin.",
                            color = MutedGray,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(favoriteApps, key = { it.packageName }) { app ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkGray)
                                        .pointerInput(app.packageName) {
                                            detectTapGestures(
                                                onTap = {
                                                    val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                                    if (intent != null) context.startActivity(intent)
                                                },
                                                onLongPress = {
                                                    selectedAppForDialog = app
                                                }
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Text(
                                        text = app.label,
                                        color = PureWhite,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkGray)
                        .clickable { showFullDrawer = true }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VIEW ALL APPS",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "All Apps",
                        color = PureWhite,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "CLOSE",
                        color = PureWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                showFullDrawer = false
                                searchQuery = ""
                            }
                            .padding(8.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(DarkGray)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search apps...",
                            color = MutedGray,
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(color = PureWhite, fontSize = 15.sp),
                        cursorBrush = SolidColor(PureWhite),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(visibleApps, key = { it.packageName }) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkGray)
                                .pointerInput(app.packageName) {
                                    detectTapGestures(
                                        onTap = {
                                            val intent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                            if (intent != null) context.startActivity(intent)
                                        },
                                        onLongPress = {
                                            selectedAppForDialog = app
                                        }
                                    )
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = app.label,
                                color = PureWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    if (hiddenApps.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (showHiddenApps) "HIDE HIDDEN APPS (${hiddenApps.size})" else "SHOW HIDDEN APPS (${hiddenApps.size})",
                                color = MutedGray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { showHiddenApps = !showHiddenApps }
                                    .padding(vertical = 8.dp)
                            )
                        }

                        if (showHiddenApps) {
                            items(hiddenApps, key = { "hidden_${it.packageName}" }) { app ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkGray)
                                        .pointerInput(app.packageName) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    selectedAppForDialog = app
                                                }
                                            )
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = app.label,
                                        color = MutedGray,
                                        fontSize = 15.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "[UNHIDE]",
                                        color = PureWhite,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable { viewModel.toggleHidden(app) }
                                            .padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selectedAppForDialog != null) {
            val app = selectedAppForDialog!!
            Dialog(onDismissRequest = { selectedAppForDialog = null }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkGray)
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = app.label,
                            color = PureWhite,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.toggleFavorite(app)
                                    selectedAppForDialog = null
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (app.isFavorite) "Unpin from Favorites" else "Pin to Favorites",
                                color = PureWhite,
                                fontSize = 16.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.toggleHidden(app)
                                    selectedAppForDialog = null
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = if (app.isHidden) "Unhide App" else "Hide App",
                                color = PureWhite,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAppForDialog = null }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "CANCEL",
                                color = MutedGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
