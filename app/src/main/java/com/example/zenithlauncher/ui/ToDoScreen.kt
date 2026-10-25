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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenithlauncher.data.TaskEntity
import com.example.zenithlauncher.ui.theme.DarkGray
import com.example.zenithlauncher.ui.theme.MutedGray
import com.example.zenithlauncher.ui.theme.PureBlack
import com.example.zenithlauncher.ui.theme.PureWhite

@Composable
fun ToDoScreen(viewModel: ZenithViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }
    var selectedTargetDate by remember { mutableStateOf("TODAY") }

    val todayTasks = tasks.filter { it.targetDate == "TODAY" }
    val tomorrowTasks = tasks.filter { it.targetDate == "TOMORROW" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .padding(16.dp)
    ) {
        Text(
            text = "ToDo",
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
                if (newTaskTitle.isEmpty()) {
                    Text(
                        text = "New task...",
                        color = MutedGray,
                        fontSize = 15.sp
                    )
                }
                BasicTextField(
                    value = newTaskTitle,
                    onValueChange = { newTaskTitle = it },
                    textStyle = TextStyle(color = PureWhite, fontSize = 15.sp),
                    cursorBrush = SolidColor(PureWhite),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTask(newTaskTitle, selectedTargetDate)
                            newTaskTitle = ""
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
                    text = "Today",
                    color = if (selectedTargetDate == "TODAY") PureWhite else MutedGray,
                    fontWeight = if (selectedTargetDate == "TODAY") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { selectedTargetDate = "TODAY" }
                        .padding(end = 16.dp, top = 4.dp, bottom = 4.dp)
                )
                Text(
                    text = "Tomorrow",
                    color = if (selectedTargetDate == "TOMORROW") PureWhite else MutedGray,
                    fontWeight = if (selectedTargetDate == "TOMORROW") FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { selectedTargetDate = "TOMORROW" }
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
                        if (newTaskTitle.isNotBlank()) {
                            viewModel.addTask(newTaskTitle, selectedTargetDate)
                            newTaskTitle = ""
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
                    text = "TODAY",
                    color = MutedGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (todayTasks.isEmpty()) {
                item {
                    Text(
                        text = "No tasks for today.",
                        color = MutedGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(todayTasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "TOMORROW",
                    color = MutedGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            if (tomorrowTasks.isEmpty()) {
                item {
                    Text(
                        text = "No tasks for tomorrow.",
                        color = MutedGray,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            } else {
                items(tomorrowTasks, key = { it.id }) { task ->
                    TaskItemRow(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onDelete = { viewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskItemRow(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkGray)
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (task.isCompleted) "[✓] " else "[ ] ",
                color = PureWhite,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = task.title,
                color = if (task.isCompleted) MutedGray else PureWhite,
                fontSize = 15.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
        }
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
