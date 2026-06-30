package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppointmentReminder
import com.example.viewmodel.MedicalViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    viewModel: MedicalViewModel,
    modifier: Modifier = Modifier
) {
    val reminders by viewModel.allReminders.collectAsState()
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Separate active and completed reminders
    val activeReminders = reminders.filter { !it.isCompleted }
    val completedReminders = reminders.filter { it.isCompleted }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isSheetOpen = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("add_reminder_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Appointment Reminder"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Reminders & Appointments",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Ensure you attend every appointment and check-up with automated system alerts.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Empty Reminders",
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Appointments Scheduled",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the '+' button below to schedule oncology visits, check-ups, or treatment reminders.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Active reminders section
                    if (activeReminders.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Upcoming Appointments",
                                icon = Icons.Default.Alarm,
                                count = activeReminders.size
                            )
                        }
                        items(activeReminders, key = { it.id }) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onToggleComplete = {
                                    viewModel.updateReminder(reminder.copy(isCompleted = true))
                                },
                                onDelete = {
                                    viewModel.deleteReminder(reminder)
                                }
                            )
                        }
                    }

                    // Completed reminders section
                    if (completedReminders.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Completed / Past Appointments",
                                icon = Icons.Default.CheckCircle,
                                count = completedReminders.size
                            )
                        }
                        items(completedReminders, key = { it.id }) { reminder ->
                            ReminderItemCard(
                                reminder = reminder,
                                onToggleComplete = {
                                    viewModel.updateReminder(reminder.copy(isCompleted = false))
                                },
                                onDelete = {
                                    viewModel.deleteReminder(reminder)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isSheetOpen = false },
            sheetState = sheetState,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                )
            }
        ) {
            ReminderForm(
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            isSheetOpen = false
                        }
                    }
                },
                onSave = { reminder ->
                    viewModel.addReminder(reminder)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            isSheetOpen = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$title ($count)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun ReminderItemCard(
    reminder: AppointmentReminder,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val isPast = reminder.dateTimeMillis < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Checkbox Icon
            IconButton(onClick = onToggleComplete) {
                Icon(
                    imageVector = if (reminder.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (reminder.isCompleted) "Mark Pending" else "Mark Complete",
                    tint = if (reminder.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                )

                if (reminder.hospitalName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Hospital",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reminder.hospitalName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule",
                        tint = if (isPast && !reminder.isCompleted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = reminder.dateTimeStr,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isPast && !reminder.isCompleted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isPast && !reminder.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(Missed)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (reminder.notes.isNotBlank()) {
                    Text(
                        text = reminder.notes,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                        lineHeight = 16.sp
                    )
                }
            }

            // Delete action
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Reminder",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ReminderForm(
    onDismiss: () -> Unit,
    onSave: (AppointmentReminder) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var hospitalName by remember { mutableStateOf("") }
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var formError by remember { mutableStateOf("") }

    // Prefill date input with tomorrow as default
    LaunchedEffect(Unit) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // default tomorrow
        val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateInput = dateSdf.format(calendar.time)
        timeInput = "09:00" // default morning
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Schedule Appointment Reminder",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        if (formError.isNotBlank()) {
            item {
                Text(
                    text = formError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Appointment Title *") },
                placeholder = { Text("e.g. Oncology Doctor Visit, Chemotherapy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_reminder_title"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = { Text("Hospital / Location Name *") },
                placeholder = { Text("e.g. General Medical Oncology Center") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_reminder_hospital"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = dateInput,
                onValueChange = { dateInput = it },
                label = { Text("Date (YYYY-MM-DD) *") },
                placeholder = { Text("e.g. 2026-07-05") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_reminder_date"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = timeInput,
                onValueChange = { timeInput = it },
                label = { Text("Time (HH:MM, 24-hr format) *") },
                placeholder = { Text("e.g. 14:30") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_reminder_time"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Preparation Instructions") },
                placeholder = { Text("e.g. Fasting 8 hrs before blood work, bring insurance card") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        if (title.isBlank() || hospitalName.isBlank() || dateInput.isBlank() || timeInput.isBlank()) {
                            formError = "Please fill in all starred (*) fields."
                        } else {
                            val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val dateTimeStr = "${dateInput.trim()} ${timeInput.trim()}"
                            val parsedDate = try {
                                format.parse(dateTimeStr)
                            } catch (e: Exception) {
                                null
                            }

                            if (parsedDate == null) {
                                formError = "Invalid Date/Time formatting. Follow YYYY-MM-DD and HH:MM."
                            } else {
                                val reminderObj = AppointmentReminder(
                                    title = title.trim(),
                                    hospitalName = hospitalName.trim(),
                                    dateTimeStr = dateTimeStr,
                                    dateTimeMillis = parsedDate.time,
                                    notes = notes.trim()
                                )
                                onSave(reminderObj)
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_save_reminder_button")
                ) {
                    Text("Save Reminder")
                }
            }
        }
    }
}
