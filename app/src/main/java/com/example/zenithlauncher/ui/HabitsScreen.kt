package com.example.zenithlauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.zenithlauncher.data.HabitEntity
import com.example.zenithlauncher.ui.theme.DarkGray
import com.example.zenithlauncher.ui.theme.MutedGray
import com.example.zenithlauncher.ui.theme.PureBlack
import com.example.zenithlauncher.ui.theme.PureWhite
import java.time.LocalDate

@Composable
fun HabitsScreen(viewModel: ZenithViewModel) {
    val habits by viewModel.habits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val goodHabits = habits.filter { it.type == "GOOD" }
    val badHabits = habits.filter { it.type == "BAD" }

    val todayStr = remember { LocalDate.now().toString() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Habits",
                color = PureWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "GOOD HABITS (TO BUILD)",
                        color = MutedGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (goodHabits.isEmpty()) {
                    item {
                        Text(
                            text = "No good habits added.",
                            color = MutedGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(goodHabits, key = { it.id }) { habit ->
                        HabitItemRow(
                            habit = habit,
                            todayStr = todayStr,
                            onToggle = { viewModel.toggleHabitCheckIn(habit) },
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "BAD HABITS (TO BREAK / QUITTING)",
                        color = MutedGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                if (badHabits.isEmpty()) {
                    item {
                        Text(
                            text = "No bad habits added.",
                            color = MutedGray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                } else {
                    items(badHabits, key = { it.id }) { habit ->
                        HabitItemRow(
                            habit = habit,
                            todayStr = todayStr,
                            onToggle = { viewModel.toggleHabitCheckIn(habit) },
                            onDelete = { viewModel.deleteHabit(habit) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PureWhite,
            contentColor = PureBlack,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = "+",
                color = PureBlack,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (showAddDialog) {
            AddHabitDialog(
                onDismiss = { showAddDialog = false },
                onAddHabit = { title, type ->
                    viewModel.addHabit(title, type)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun HabitItemRow(
    habit: HabitEntity,
    todayStr: String,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isChecked = habit.lastCheckedDate == todayStr

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGray)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                color = PureWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (habit.streakCount > 0) "${habit.streakCount} day streak" else "No streak yet",
                color = MutedGray,
                fontSize = 12.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (isChecked) "[DONE]" else "[CHECK IN]",
                color = PureWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onToggle() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "DELETE",
                color = MutedGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onDelete() }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("GOOD") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(DarkGray)
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "New Habit",
                    color = PureWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "HABIT NAME",
                    color = MutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PureBlack)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    if (title.isEmpty()) {
                        Text(text = "Habit title (e.g. Reading, Quit smoking)...", color = MutedGray, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(color = PureWhite, fontSize = 14.sp),
                        cursorBrush = SolidColor(PureWhite),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HABIT TYPE",
                    color = MutedGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Good Habit (Build)",
                        color = if (selectedType == "GOOD") PureWhite else MutedGray,
                        fontWeight = if (selectedType == "GOOD") FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { selectedType = "GOOD" }
                            .padding(vertical = 4.dp)
                    )
                    Text(
                        text = "Bad Habit (Break)",
                        color = if (selectedType == "BAD") PureWhite else MutedGray,
                        fontWeight = if (selectedType == "BAD") FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { selectedType = "BAD" }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "CANCEL",
                        color = MutedGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "SAVE HABIT",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                if (title.isNotBlank()) {
                                    onAddHabit(title, selectedType)
                                }
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
