package com.example.zenithlauncher.ui

import android.app.AppOpsManager
import android.app.Application
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenithlauncher.data.AppPreferenceEntity
import com.example.zenithlauncher.data.HabitEntity
import com.example.zenithlauncher.data.TaskEntity
import com.example.zenithlauncher.data.ZenithDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

class ZenithViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ZenithDatabase.getDatabase(application)
    private val taskDao = database.taskDao()
    private val habitDao = database.habitDao()
    private val appPreferenceDao = database.appPreferenceDao()

    val tasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val habits: StateFlow<List<HabitEntity>> = habitDao.getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _rawInstalledApps = MutableStateFlow<List<AppInfo>>(emptyList())

    val installedApps: StateFlow<List<AppInfo>> = combine(
        _rawInstalledApps,
        appPreferenceDao.getAllPreferences()
    ) { rawApps, prefs ->
        val prefMap = prefs.associateBy { it.packageName }
        rawApps.map { app ->
            val pref = prefMap[app.packageName]
            app.copy(
                isFavorite = pref?.isFavorite ?: false,
                isHidden = pref?.isHidden ?: false
            )
        }.sortedBy { it.label.lowercase() }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (habitDao.getHabitCount() == 0) {
                val defaultHabits = listOf(
                    HabitEntity(title = "Reading books", type = "GOOD"),
                    HabitEntity(title = "Workout", type = "GOOD"),
                    HabitEntity(title = "No alcohol", type = "BAD"),
                    HabitEntity(title = "No smoking", type = "BAD")
                )
                habitDao.insertHabits(defaultHabits)
            }
        }
    }

    fun addTask(title: String, targetDate: String) {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.insertTask(TaskEntity(title = title.trim(), targetDate = targetDate))
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            taskDao.deleteTask(task)
        }
    }

    fun addHabit(title: String, type: String) {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            habitDao.insertHabit(HabitEntity(title = title.trim(), type = type))
        }
    }

    fun toggleHabitCheckIn(habit: HabitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now()
            val todayStr = today.toString()
            val yesterdayStr = today.minusDays(1).toString()
            val isCheckedToday = habit.lastCheckedDate == todayStr

            val updated = if (isCheckedToday) {
                habit.copy(
                    lastCheckedDate = "",
                    streakCount = (habit.streakCount - 1).coerceAtLeast(0)
                )
            } else {
                val newStreak = if (habit.lastCheckedDate == yesterdayStr) {
                    habit.streakCount + 1
                } else {
                    1
                }
                habit.copy(
                    lastCheckedDate = todayStr,
                    streakCount = newStreak
                )
            }
            habitDao.updateHabit(updated)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            habitDao.deleteHabit(habit)
        }
    }

    fun toggleFavorite(app: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPref = appPreferenceDao.getPreference(app.packageName)
            val newFav = !(currentPref?.isFavorite ?: false)
            val updated = (currentPref ?: AppPreferenceEntity(packageName = app.packageName)).copy(
                isFavorite = newFav,
                isHidden = false
            )
            appPreferenceDao.upsertPreference(updated)
        }
    }

    fun toggleHidden(app: AppInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentPref = appPreferenceDao.getPreference(app.packageName)
            val newHidden = !(currentPref?.isHidden ?: false)
            val updated = if (currentPref != null) {
                currentPref.copy(
                    isFavorite = if (newHidden) false else currentPref.isFavorite,
                    isHidden = newHidden
                )
            } else {
                AppPreferenceEntity(
                    packageName = app.packageName,
                    isFavorite = false,
                    isHidden = newHidden
                )
            }
            appPreferenceDao.upsertPreference(updated)
        }
    }

    fun loadInstalledApps(context: Context) {
        if (_rawInstalledApps.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolvedList = pm.queryIntentActivities(mainIntent, 0)
            val currentPkg = context.packageName
            val apps = resolvedList
                .mapNotNull { resolveInfo ->
                    val pkg = resolveInfo.activityInfo.packageName
                    if (pkg == currentPkg) null
                    else {
                        val label = resolveInfo.loadLabel(pm).toString()
                        AppInfo(label = label, packageName = pkg)
                    }
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase() }

            _rawInstalledApps.value = apps
        }
    }

    fun getTodayScreenTimeMillis(context: Context): Long {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return 0L

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val events = usageStatsManager.queryEvents(startTime, endTime) ?: return 0L
        val event = UsageEvents.Event()

        var totalTime = 0L
        val appResumeMap = HashMap<String, Long>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val eventType = event.eventType
            val timeStamp = event.timeStamp

            if (eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                appResumeMap[pkg] = timeStamp
            } else if (eventType == UsageEvents.Event.ACTIVITY_PAUSED || eventType == UsageEvents.Event.ACTIVITY_STOPPED) {
                val resumeTime = appResumeMap.remove(pkg)
                if (resumeTime != null) {
                    val effectiveStart = maxOf(resumeTime, startTime)
                    val duration = timeStamp - effectiveStart
                    if (duration > 0) {
                        totalTime += duration
                    }
                }
            }
        }

        val now = System.currentTimeMillis()
        for ((_, resumeTime) in appResumeMap) {
            val effectiveStart = maxOf(resumeTime, startTime)
            val duration = now - effectiveStart
            if (duration > 0) {
                totalTime += duration
            }
        }

        if (totalTime > 0) return totalTime

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        ) ?: return 0L

        val packageTimeMap = HashMap<String, Long>()
        for (stat in stats) {
            if (stat.lastTimeUsed >= startTime) {
                val existing = packageTimeMap[stat.packageName] ?: 0L
                if (stat.totalTimeInForeground > existing) {
                    packageTimeMap[stat.packageName] = stat.totalTimeInForeground
                }
            }
        }

        var fallbackTotal = 0L
        for (time in packageTimeMap.values) {
            fallbackTotal += time
        }
        return fallbackTotal
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getDaysRemainingInYear(): Long {
        val today = LocalDate.now()
        val endOfYear = LocalDate.of(today.year, 12, 31)
        return ChronoUnit.DAYS.between(today, endOfYear)
    }
}
