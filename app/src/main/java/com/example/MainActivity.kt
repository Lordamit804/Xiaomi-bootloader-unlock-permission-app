package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DeviceTimer
import com.example.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: UnlockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    UnlockAppContent(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun UnlockAppContent(
    viewModel: UnlockViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Status & Checklist", "Unlock Timers", "Official Guide")
    val deviceSpecs = remember { viewModel.getDeviceSpecs() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Custom Header
        AppHeader(deviceSpecs = deviceSpecs)

        // Custom Tab Selection (Styled like a modern dark carbon panel)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }

        // Subtext / State Area
        val checklistProgress by viewModel.checklistProgress.collectAsStateWithLifecycle()
        val totalSteps = viewModel.checklistSchema.size
        val completedSteps = checklistProgress.values.count { it }

        // Pages content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> ChecklistTabScreen(
                    viewModel = viewModel,
                    checklistProgress = checklistProgress,
                    completedSteps = completedSteps,
                    totalSteps = totalSteps,
                    deviceSpecs = deviceSpecs
                )
                1 -> CountdownTabScreen(
                    viewModel = viewModel
                )
                2 -> ManualTabScreen()
            }
        }
    }
}

@Composable
fun AppHeader(deviceSpecs: DeviceSpecs) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // High-tech Glowing Lock Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    )
                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Unlock status",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "XIAOMI UNLOCK HELPER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Bootloader Status Utility",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Small badge checking if running on a Mi/Redmi/Poco device
            if (deviceSpecs.isXiaomiFamily) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                        )
                        Text(
                            text = deviceSpecs.brand,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "External Device",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistTabScreen(
    viewModel: UnlockViewModel,
    checklistProgress: Map<String, Boolean>,
    completedSteps: Int,
    totalSteps: Int,
    deviceSpecs: DeviceSpecs
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Device scan status card
        item {
            DeviceSpecsCard(deviceSpecs = deviceSpecs)
        }

        // Circular progress status tracker
        item {
            EligibilityProgressCard(
                completedCount = completedSteps,
                totalCount = totalSteps,
                onResetChecklist = {
                    viewModel.checklistSchema.forEach {
                        viewModel.toggleChecklistStep(it.key, false)
                    }
                }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Required Steps Checklist",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Checklist steps list
        items(viewModel.checklistSchema) { item ->
            val isChecked = checklistProgress[item.key] ?: false
            ChecklistItemCard(
                title = item.title,
                description = item.description,
                isChecked = isChecked,
                onCheckedChange = { checked ->
                    viewModel.toggleChecklistStep(item.key, checked)
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DeviceSpecsCard(deviceSpecs: DeviceSpecs) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (deviceSpecs.isXiaomiFamily) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (deviceSpecs.isXiaomiFamily) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (deviceSpecs.isXiaomiFamily) Icons.Default.Check else Icons.Default.Search,
                        contentDescription = "Device specs status",
                        tint = if (deviceSpecs.isXiaomiFamily) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "DEVICE ARCHITECTURE HARDWARE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (deviceSpecs.isXiaomiFamily) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Two-column layout for device properties
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    PropertyRow(label = "Manufacturer", value = deviceSpecs.manufacturer)
                    PropertyRow(label = "Brand", value = deviceSpecs.brand)
                    PropertyRow(label = "Model", value = deviceSpecs.model)
                }
                Column(modifier = Modifier.weight(1.5f)) {
                    PropertyRow(label = "Android Release", value = "v" + deviceSpecs.androidVersion)
                    PropertyRow(label = "SDK Level", value = deviceSpecs.sdkLevel.toString())
                    PropertyRow(label = "Security Patch", value = deviceSpecs.securityPatch)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            Spacer(modifier = Modifier.height(8.dp))

            // Emulation Notice
            if (deviceSpecs.isXiaomiFamily) {
                Text(
                    text = "✓ Official Xiaomi/Redmi/POCO hardware discovered. Bootloader is unlockable with developer configuration.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary,
                    lineHeight = 16.sp
                )
            } else {
                Text(
                    text = "ℹ Running on external devices. Emulating HyperOS/MIUI unlocking tools so you can configure checklist states.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 16.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@Composable
fun PropertyRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EligibilityProgressCard(
    completedCount: Int,
    totalCount: Int,
    onResetChecklist: () -> Unit
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val isFullyCompleted = completedCount == totalCount

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "Eligibility progress animate"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .drawBehind {
                        // Background full circle
                        drawCircle(
                            color = Color.DarkGray.copy(alpha = 0.15f),
                            radius = size.minDimension / 2,
                            style = Stroke(width = 8.dp.toPx())
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    color = if (isFullyCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    modifier = Modifier.fillMaxSize()
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Ready",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(18.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isFullyCompleted) "Permissions Met!" else "Unlocking Progress",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFullyCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$completedCount of $totalCount criteria checked",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isFullyCompleted) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF10B981).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "READY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PENDING REQUIREMENTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Reset action button
                    Text(
                        text = "Reset All",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onResetChecklist)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistItemCard(
    title: String,
    description: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) },
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag("step_checkbox_$title")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isChecked) TextDecoration.LineThrough else null
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun CountdownTabScreen(
    viewModel: UnlockViewModel
) {
    val timers by viewModel.timers.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    // Live refresh ticket that recomposes the timers every 1 second
    var tickTrigger by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            tickTrigger++
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (timers.isEmpty()) {
            // High fidelity modern Empty State block
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .drawBehind {
                            // Circular dashboard boundary
                            drawCircle(
                                color = Color(0xFFFF6700).copy(alpha = 0.08f),
                                radius = size.width / 1.7f
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = "No Waiting Countdown Active",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Xiaomi fastboot unlocks require linked accounts to endure a 168-hour (7 days) curing cycle. Start an active timer tracker below.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Add Wait Countdown", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Tracked counters
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Wait Schedules (${timers.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedButton(
                        onClick = { showDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(timers, key = { it.id }) { timer ->
                        TimerItemCard(
                            timer = timer,
                            tickTrigger = tickTrigger,
                            onDelete = { viewModel.deleteTimer(timer) }
                        )
                    }
                }
            }
        }

        // Add countdown timer dialog setup
        if (showDialog) {
            AddTimerDialog(
                onDismiss = { showDialog = false },
                onConfirm = { deviceName, account, hours, daysAgo ->
                    viewModel.addTimer(deviceName, account, hours, daysAgo)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun TimerItemCard(
    timer: DeviceTimer,
    tickTrigger: Int,
    onDelete: () -> Unit
) {
    val totalSeconds = timer.durationHours * 3600L
    val remainingMillis = timer.getTimeRemainingMillis()
    val elapsedMillis = (timer.durationHours * 3600 * 1000L) - remainingMillis
    val percentElapsed = if (totalSeconds > 0) elapsedMillis.toFloat() / (totalSeconds * 1000L).toFloat() else 1.0f
    val isCompleted = remainingMillis <= 0

    // Time calculations
    val remSecs = remainingMillis / 1000L
    val days = remSecs / (24 * 3600)
    val hours = (remSecs % (24 * 3600)) / 3600
    val minutes = (remSecs % 3600) / 60
    val seconds = remSecs % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isCompleted) Color(0xFF10B981).copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = timer.deviceName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Mi Account Info
            Text(
                text = "Mi Account ID: ${timer.miAccount}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Time Breakdown Display Card
            if (!isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeBoxState(value = days.toString(), label = "DAYS")
                    TimeBoxState(value = hours.toString().padStart(2, '0'), label = "HRS")
                    TimeBoxState(value = minutes.toString().padStart(2, '0'), label = "MIN")
                    TimeBoxState(value = seconds.toString().padStart(2, '0'), label = "SEC")
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF10B981).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                        Column {
                            Text(
                                text = "WAIT CYCLE COMPLETED",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "Xiaomi permission is ready to flash. Connect Fastboot to PC.",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981).copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar representing curing sequence
            LinearProgressIndicator(
                progress = { percentElapsed.coerceIn(0.0f, 1.0f) },
                color = if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Linked details line
            val sdf = remember { SimpleDateFormat("MMMM dd, yyyy h:mm a", Locale.getDefault()) }
            val formattedBind = sdf.format(Date(timer.bindTimeMillis))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bound: $formattedBind",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${(percentElapsed * 100).toInt()}% elapsed",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCompleted) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun TimeBoxState(value: String, label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddTimerDialog(
    onDismiss: () -> Unit,
    onConfirm: (deviceName: String, account: String, hours: Int, daysAgo: Double) -> Unit
) {
    var deviceName by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf("") }
    var durationHours by remember { mutableStateOf(168) } // standard 168 hours (7 days)
    var daysElapsed by remember { mutableStateOf(0.0) } // days ago linked

    val durationPresets = listOf(
        168 to "7 Days (168h)",
        360 to "15 Days (360h)",
        720 to "30 Days (720h)",
        72 to "3 Days (72h)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Device wait clock",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = deviceName,
                    onValueChange = { deviceName = it },
                    label = { Text("Xiaomi Device Name") },
                    placeholder = { Text("e.g. Redmi Note 13 Pro+") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = accountId,
                    onValueChange = { accountId = it },
                    label = { Text("Mi Account Number (digits)") },
                    placeholder = { Text("e.g. 624510341") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Wait duration selector
                Column {
                    Text(
                        text = "Official Server Wait Time",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        durationPresets.forEach { (hours, label) ->
                            val isSelected = durationHours == hours
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { durationHours = hours }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.replace(" Days", "D").replace(" (", "\n("),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }

                // Days elapsed slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Linked to device",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (daysElapsed == 0.0) "Just Now" else String.format("%.1f days ago", daysElapsed),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = daysElapsed.toFloat(),
                        onValueChange = { daysElapsed = it.toDouble() },
                        valueRange = 0f..7f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(deviceName, accountId, durationHours, daysElapsed) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Track Wait")
                    }
                }
            }
        }
    }
}

@Composable
fun ManualTabScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            InteractiveInfoBanner()
        }

        item {
            Text(
                text = "Official Fastboot PC Runbook",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            FastbootConsoleRunbook()
        }

        item {
            Text(
                text = "Troubleshooting & Support Details",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Expandable help questions
        item {
            ExpandableHelpCard(
                question = "How to satisfy HyperOS Level 5 Requirements?",
                answer = "On official Xiaomi HyperOS globally, you apply for permission via the Mi Community app -> Profile -> Unlock Bootloader. Account age must exceed 30 days. On China developer ROMs, users need to answer developer questions on the community forum to bypass the limit."
            )
        }

        item {
            ExpandableHelpCard(
                question = "Why has my waiting timer reset?",
                answer = "The server tracks your link status based on unique secure tokens. If you sign out of your Mi Account on the device, factory reset, or attempt to 'Add Account' again, a brand new lock query request is sent, resetting the waiting countdown."
            )
        }

        item {
            ExpandableHelpCard(
                question = "Received 'Account Error 86012'?",
                answer = "This means Xiaomi's authorization API is rejecting the bind request. This is usually caused by network issues or IP address limits. Toggle your mobile network data off/on, toggle Airplane Mode, or retry linking later."
            )
        }

        item {
            ExpandableHelpCard(
                question = "What happens after unlocking the Bootloader?",
                answer = "Your smartphone's user partition will be wiped on unlock for security. You will be able to flash fastboot ROMs, install recovery kernels (like TWRP), root with Magisk/KernelSU, and enjoy extreme customization."
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InteractiveInfoBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Understanding the Protocol Limit",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Xiaomi permits a maximum of 3 unique devices unlocked per calendar year under one approved Mi Account ID. Do not link secondary test devices prematurely.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun FastbootConsoleRunbook() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Technical console green indicator
                Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                Text(
                    text = "ADB & FASTBOOT SHELL TERMINAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Fastboot steps with styled code block indicators
            TerminalCodeStep(
                stepNum = "1",
                instruction = "Reboot Xiaomi device into Fastboot status by pressing Power + Volume Down keys concurrent. Disclose orange FASTBOOT mascot.",
                command = "adb reboot bootloader"
            )

            TerminalCodeStep(
                stepNum = "2",
                instruction = "Detect connection over USB cable in PC Command Prompt or Terminal shell.",
                command = "fastboot devices"
            )

            TerminalCodeStep(
                stepNum = "3",
                instruction = "Verify that the official Xiaomi Mi Flash Unlock application is running on Windows PC and signed into the same Mi Account.",
                command = "Start MIFLASHUNLOCK.exe"
            )
        }
    }
}

@Composable
fun TerminalCodeStep(stepNum: String, instruction: String, command: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "[$stepNum]",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = instruction,
                fontSize = 12.sp,
                color = SlateGray,
                lineHeight = 16.sp
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F172A), RoundedCornerShape(6.dp))
                .border(0.5.dp, Color(0xFF1E293B), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "$ " + command,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ExpandableHelpCard(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = answer,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
