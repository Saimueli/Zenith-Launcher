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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithlauncher.data.HabitEntity
import com.example.zenithlauncher.ui.theme.DarkGray
import com.example.zenithlauncher.ui.theme.MutedGray
import com.example.zenithlauncher.ui.theme.PureBlack
import com.example.zenithlauncher.ui.theme.PureWhite
import java.time.LocalDate

@Composable
fun HabitsScreen(viewModel: ZenithViewModel) {
    val habits by viewModel.habits.collectAsState()
    var newHabitTitle by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("GOOD") }

    val goodHabits = habits.filter { it.type == "GOOD" }
    val badHabits = habits.filter { it.type == "BAD" }

    val todayStr = remember { LocalDate.now().toString() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "Habits",
            color = PureWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(DarkGray)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (newHabitTitle.isEmpty()) {
                    Text(
                        text = "New habit...",
                        color = MutedGray,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = newHabitTitle,
                    onValueChange = { newHabitTitle = it },
                    textStyle = TextStyle(color = PureWhite, fontSize = 15.sp),
                    cursorBrush = SolidColor(PureWhite),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newHabitTitle.isNotBlank()) {
                            viewModel.addHabit(newHabitTitle, selectedType)
                            newHabitTitle = ""
                        }
                    }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row {
                Text(
                    text = "Good Habit",
                    color = if (selectedType == "GOOD") PureWhite else MutedGray,
                    fontWeight = if (selectedType == "GOOD") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { selectedType = "GOOD" }
                        .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = "Bad Habit / Quitting",
                    color = if (selectedType == "BAD") PureWhite else MutedGray,
                    fontWeight = if (selectedType == "BAD") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { selectedType = "BAD" }
                        .padding(top = 4.dp, bottom = 4.dp)
                )
            }

            Text(
                text = "ADD",
                color = PureWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier
                    .clickable {
                        if (newHabitTitle.isNotBlank()) {
                            viewModel.addHabit(newHabitTitle, selectedType)
                            newHabitTitle = ""
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "GOOD HABITS",
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
                    text = "BAD HABITS / QUITTING",
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
            .background(DarkGray)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                color = PureWhite,
                fontSize = 15.sp,
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
