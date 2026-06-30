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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HospitalVisit
import com.example.viewmodel.MedicalViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HospitalLogScreen(
    viewModel: MedicalViewModel,
    modifier: Modifier = Modifier
) {
    val visits by viewModel.allVisits.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Bottom Sheet states for Add/Edit Form
    var isSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    
    // Active visit item being edited (null = add new)
    var editingVisit by remember { mutableStateOf<HospitalVisit?>(null) }
    
    // Delete validation dialog
    var deletingVisit by remember { mutableStateOf<HospitalVisit?>(null) }

    // Filtered past visits list
    val filteredVisits = remember(visits, searchQuery) {
        if (searchQuery.isBlank()) {
            visits
        } else {
            visits.filter {
                it.hospitalName.contains(searchQuery, ignoreCase = true) ||
                it.doctorName.contains(searchQuery, ignoreCase = true) ||
                it.reason.contains(searchQuery, ignoreCase = true) ||
                it.diagnosis.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingVisit = null
                    isSheetOpen = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_visit_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Log Hospital Visit"
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
            // Search & Stats bar
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Hospital Visit History",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Record symptoms, diagnosis, doctor recommendations, and prescription details.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search visits, diagnoses, or doctors...", fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("visit_search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true
                )
            }

            if (filteredVisits.isEmpty()) {
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
                            imageVector = Icons.Default.MedicalInformation,
                            contentDescription = "Empty Hospital History",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No Matching Visits Found" else "No Hospital Visits Logged Yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try searching different terms." else "Tap the '+' button below to save your hospital check-ups and medical outcomes.",
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVisits, key = { it.id }) { visit ->
                        HospitalVisitItemCard(
                            visit = visit,
                            onEdit = {
                                editingVisit = visit
                                isSheetOpen = true
                            },
                            onDelete = {
                                deletingVisit = visit
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Add / Edit
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
            HospitalVisitForm(
                initialVisit = editingVisit,
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            isSheetOpen = false
                        }
                    }
                },
                onSave = { savedVisit ->
                    if (editingVisit == null) {
                        viewModel.addVisit(savedVisit)
                    } else {
                        viewModel.updateVisit(savedVisit)
                    }
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            isSheetOpen = false
                        }
                    }
                }
            )
        }
    }

    // Delete verification Dialog
    deletingVisit?.let { visit ->
        AlertDialog(
            onDismissRequest = { deletingVisit = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVisit(visit)
                        deletingVisit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deletingVisit = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Log Entry?") },
            text = { Text("Are you sure you want to permanently delete the hospital visit record at ${visit.hospitalName} on ${visit.visitDate}?") }
        )
    }
}

@Composable
fun HospitalVisitItemCard(
    visit: HospitalVisit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Main row: Icon & primary details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Hospital Log",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visit.hospitalName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Reason: ${visit.reason}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Info Row (Date & Doctor)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = visit.visitDate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Doctor",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Dr. ${visit.doctorName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded detail section
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                    Spacer(modifier = Modifier.height(12.dp))

                    DetailField(label = "Diagnosis / Outcome", value = visit.diagnosis)
                    DetailField(label = "Treatment Plan / Recommendations", value = visit.treatmentPlan)
                    DetailField(label = "Prescriptions & Medications", value = visit.prescriptions)
                    DetailField(label = "Personal Notes", value = visit.notes)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Visit Log",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Visit Log",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailField(label: String, value: String) {
    if (value.isNotBlank()) {
        Column(modifier = Modifier.padding(bottom = 10.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun HospitalVisitForm(
    initialVisit: HospitalVisit?,
    onDismiss: () -> Unit,
    onSave: (HospitalVisit) -> Unit
) {
    var hospitalName by remember { mutableStateOf(initialVisit?.hospitalName ?: "") }
    var doctorName by remember { mutableStateOf(initialVisit?.doctorName ?: "") }
    var visitDate by remember { mutableStateOf(initialVisit?.visitDate ?: "") }
    var reason by remember { mutableStateOf(initialVisit?.reason ?: "") }
    var diagnosis by remember { mutableStateOf(initialVisit?.diagnosis ?: "") }
    var treatmentPlan by remember { mutableStateOf(initialVisit?.treatmentPlan ?: "") }
    var prescriptions by remember { mutableStateOf(initialVisit?.prescriptions ?: "") }
    var notes by remember { mutableStateOf(initialVisit?.notes ?: "") }

    var formError by remember { mutableStateOf("") }

    // Auto fill current date if empty
    LaunchedEffect(Unit) {
        if (visitDate.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            visitDate = sdf.format(Date())
        }
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
                text = if (initialVisit == null) "Log Past Hospital Visit" else "Edit Hospital Visit Details",
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
                value = hospitalName,
                onValueChange = { hospitalName = it },
                label = { Text("Hospital / Clinic Name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_hospital_name"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("Doctor Name *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_doctor_name"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = visitDate,
                onValueChange = { visitDate = it },
                label = { Text("Visit Date (YYYY-MM-DD) *") },
                placeholder = { Text("e.g. 2026-06-30") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_visit_date"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        }

        item {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason for Visit *") },
                placeholder = { Text("e.g. Routine oncology check, chemotherapy, symptoms check") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_reason_for_visit"),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = diagnosis,
                onValueChange = { diagnosis = it },
                label = { Text("Diagnosis / Examination Results") },
                placeholder = { Text("e.g. Normal blood counts, tumor shrinkage confirmed") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            OutlinedTextField(
                value = treatmentPlan,
                onValueChange = { treatmentPlan = it },
                label = { Text("Treatment Plan / Recommendations") },
                placeholder = { Text("e.g. Next chemo scheduled in 3 weeks, daily hydration") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            OutlinedTextField(
                value = prescriptions,
                onValueChange = { prescriptions = it },
                label = { Text("Prescribed Medications & Refills") },
                placeholder = { Text("e.g. Ondansetron 4mg as needed, Filgrastim injection") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Personal Notes & Feeling Logs") },
                placeholder = { Text("e.g. Had slight nausea, doctor was very reassuring") },
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
                        if (hospitalName.isBlank() || doctorName.isBlank() || visitDate.isBlank() || reason.isBlank()) {
                            formError = "Please fill in all starred (*) fields."
                        } else {
                            val visitObj = HospitalVisit(
                                id = initialVisit?.id ?: 0,
                                hospitalName = hospitalName.trim(),
                                doctorName = doctorName.trim(),
                                visitDate = visitDate.trim(),
                                reason = reason.trim(),
                                diagnosis = diagnosis.trim(),
                                treatmentPlan = treatmentPlan.trim(),
                                prescriptions = prescriptions.trim(),
                                notes = notes.trim()
                            )
                            onSave(visitObj)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_save_button")
                ) {
                    Text("Save Log")
                }
            }
        }
    }
}
