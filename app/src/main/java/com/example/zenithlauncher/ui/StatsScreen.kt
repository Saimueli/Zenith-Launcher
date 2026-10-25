package com.example.zenithlauncher.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithlauncher.ui.theme.DarkGray
import com.example.zenithlauncher.ui.theme.MutedGray
import com.example.zenithlauncher.ui.theme.PureBlack
import com.example.zenithlauncher.ui.theme.PureWhite
import java.time.LocalDate

@Composable
fun StatsScreen(viewModel: ZenithViewModel) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val habits by viewModel.habits.collectAsState()

    val hasPermission = remember(tasks, habits) {
        viewModel.hasUsageStatsPermission(context)
    }

    val screenTimeMillis = remember(hasPermission, tasks, habits) {
        if (hasPermission) viewModel.getTodayScreenTimeMillis(context) else 0L
    }

    val screenTimeHours = screenTimeMillis / (1000 * 60 * 60)
    val screenTimeMinutes = (screenTimeMillis / (1000 * 60)) % 60

    val daysRemaining = remember { viewModel.getDaysRemainingInYear() }

    val todayTasks = tasks.filter { it.targetDate == "TODAY" }
    val completedTodayTasksCount = todayTasks.count { it.isCompleted }
    val taskPercentage = if (todayTasks.isNotEmpty()) {
        (completedTodayTasksCount * 100) / todayTasks.size
    } else 0

    val todayStr = remember { LocalDate.now().toString() }
    val checkedHabitsCount = habits.count { it.lastCheckedDate == todayStr }
    val habitPercentage = if (habits.isNotEmpty()) {
        (checkedHabitsCount * 100) / habits.size
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Stats",
            color = PureWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        StatCard(
            title = "TODAY'S SCREEN TIME",
            value = if (hasPermission) "${screenTimeHours}h ${screenTimeMinutes}m" else "Permission Required",
            subtitle = if (!hasPermission) "Tap to grant Usage Access permission" else null,
            onClick = if (!hasPermission) {
                {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                }
            } else null
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            title = "YEAR PROGRESS",
            value = "$daysRemaining days remaining",
            subtitle = "Until the end of ${LocalDate.now().year}"
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            title = "TODAY'S TASKS",
            value = "$completedTodayTasksCount / ${todayTasks.size} completed ($taskPercentage%)",
            subtitle = if (todayTasks.isEmpty()) "No tasks created for today" else null
        )

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            title = "HABIT ADHERENCE",
            value = "$checkedHabitsCount / ${habits.size} checked ($habitPercentage%)",
            subtitle = if (habits.isEmpty()) "No habits active" else "Daily completion rate"
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkGray)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = MutedGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = PureWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MutedGray,
                fontSize = 13.sp
            )
        }
    }
}
