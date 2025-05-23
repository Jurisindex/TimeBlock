package com.example.timeblock.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.timeblock.data.entity.Entry
import com.example.timeblock.data.entity.User
import com.example.timeblock.ui.MainViewModel
import com.example.timeblock.util.proteinGoalForWeightString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toKotlinInstant

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun UserSetupScreen(onUserCreated: (String, String) -> Unit) {
    var displayName by remember { mutableStateOf("") }
    var weightValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedUnit by remember { mutableStateOf("kg") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to TimeBlock",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Please enter your name to get started",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = weightValue,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weightValue = it },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                OutlinedButton(onClick = { expanded = true }) {
                    Text(selectedUnit)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("kg") }, onClick = { selectedUnit = "kg"; expanded = false })
                    DropdownMenuItem(text = { Text("lbs") }, onClick = { selectedUnit = "lbs"; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (displayName.isNotBlank()) {
                    val weightString = if (weightValue.isNotBlank() && weightValue.toDoubleOrNull() != null && weightValue.toDouble() > 0) {
                        "$weightValue $selectedUnit"
                    } else {
                        "0"
                    }
                    onUserCreated(displayName, weightString)
                }
            },
            enabled = displayName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Continue")
        }
    }
}

@Composable
fun HomeScreen(
    user: User,
    trackingData: Entry?,
    currentEditMode: MainViewModel.EditMode?,
    onEditModeSelected: (MainViewModel.EditMode) -> Unit,
    onDismissDialog: () -> Unit,
    onUpdateValue: (Int, Boolean) -> Unit,
    onViewHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    showWeightPrompt: Boolean,
    onWeightSet: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with user info and history button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenSettings) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
            Text(
                text = "Hello, ${user.displayName}",
                style = MaterialTheme.typography.headlineMedium
            )
            val clipboard = LocalClipboardManager.current
            IconButton(onClick = {
                if (trackingData != null) {
                    val ldt = trackingData.timeCreated
                        .toKotlinInstant()
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                    val formattedDate = "%04d-%02d-%02d %02d:%02d".format(
                        ldt.year,
                        ldt.monthNumber,
                        ldt.dayOfMonth,
                        ldt.hour,
                        ldt.minute
                    )
                    val copyText = """
                        Date: $formattedDate
                        Protein: ${trackingData.proteinGrams}g
                        Veggies: ${trackingData.vegetableServings}
                        Steps: ${trackingData.steps}
                    """.trimIndent()
                    clipboard.setText(AnnotatedString(copyText))
                }
            }) {
                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Copy")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Today's stats
        if (trackingData != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Today's Progress",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val goal = proteinGoalForWeightString(user.weight)
                        StatItem(
                            label = "Protein",
                            value = "${trackingData.proteinGrams}g/$goal g"
                        )
                        StatItem(
                            label = "Veggies",
                            value = "${trackingData.vegetableServings} serv/5"
                        )
                        StatItem(
                            label = "Steps",
                            value = "${trackingData.steps}/8500"
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onViewHistory, modifier = Modifier.align(Alignment.Start)) {
            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "View History")
            Spacer(modifier = Modifier.width(8.dp))
            Text("View History")
        }

        // Bottom third with 3 buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TrackingButton(
                label = "Protein",
                modifier = Modifier.weight(1f),
                onClick = { onEditModeSelected(MainViewModel.EditMode.PROTEIN) }
            )

            TrackingButton(
                label = "Veggies",
                modifier = Modifier.weight(1f),
                onClick = { onEditModeSelected(MainViewModel.EditMode.VEGETABLES) }
            )

            TrackingButton(
                label = "Steps",
                modifier = Modifier.weight(1f),
                onClick = { onEditModeSelected(MainViewModel.EditMode.STEPS) }
            )
        }
    }

    // Edit dialog
    if (currentEditMode != null && trackingData != null) {
        EditDialog(
            mode = currentEditMode,
            currentValue = when (currentEditMode) {
                MainViewModel.EditMode.PROTEIN -> trackingData.proteinGrams
                MainViewModel.EditMode.VEGETABLES -> trackingData.vegetableServings
                MainViewModel.EditMode.STEPS -> trackingData.steps
            },
            onDismiss = onDismissDialog,
            onUpdate = onUpdateValue,
            onViewHistory = onViewHistory
        )
    }

    if (showWeightPrompt) {
        WeightDialog(onDismiss = {}, onSet = onWeightSet)
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TrackingButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Visible
        )
    }
}

@Composable
fun EditDialog(
    mode: MainViewModel.EditMode,
    currentValue: Int,
    onDismiss: () -> Unit,
    onUpdate: (Int, Boolean) -> Unit,
    onViewHistory: () -> Unit
) {
    var inputValue by remember(mode) { mutableStateOf("") }
    val title = when (mode) {
        MainViewModel.EditMode.PROTEIN -> "Update Protein"
        MainViewModel.EditMode.VEGETABLES -> "Update Vegetable Servings"
        MainViewModel.EditMode.STEPS -> "Update Steps"
    }

    val unit = when (mode) {
        MainViewModel.EditMode.PROTEIN -> "grams"
        MainViewModel.EditMode.VEGETABLES -> "servings"
        MainViewModel.EditMode.STEPS -> "steps"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Current: $currentValue $unit",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = inputValue,
                    onValueChange = {
                        // Only allow numbers
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            inputValue = it
                        }
                    },
                    label = { Text("Value to update") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Add button
                    Button(
                        onClick = {
                            val value = inputValue.toIntOrNull() ?: 0
                            onUpdate(value, true)
                        },
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Text("Add")
                    }

                    // Set button
                    Button(
                        onClick = {
                            val value = inputValue.toIntOrNull() ?: 0
                            onUpdate(value, false)
                        },
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                    ) {
                        Text("Set")
                    }

                    // Cancel button
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text("Back")
                    }
                }
            }
        }
    }
}

@Composable
fun WeightDialog(onDismiss: () -> Unit, onSet: (String) -> Unit) {
    var weightValue by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf("kg") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "Enter Weight", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = weightValue,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weightValue = it },
                        label = { Text("Weight") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(unit) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            DropdownMenuItem(text = { Text("kg") }, onClick = { unit = "kg"; expanded = false })
                            DropdownMenuItem(text = { Text("lbs") }, onClick = { unit = "lbs"; expanded = false })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(onClick = {
                        val w = if (weightValue.isNotBlank() && weightValue.toDoubleOrNull() != null && weightValue.toDouble() > 0) {
                            "$weightValue $unit"
                        } else {
                            "0"
                        }
                        onSet(w)
                    }) {
                        Text("Set")
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(user: User, onSave: (String, String) -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf(user.displayName) }
    var weightVal by remember { mutableStateOf(user.weight.takeWhile { it.isDigit() || it == '.' }) }
    var expanded by remember { mutableStateOf(false) }
    var unit by remember { mutableStateOf(if (user.weight.endsWith("lbs")) "lbs" else "kg") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = weightVal,
                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) weightVal = it },
                label = { Text("Weight") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                OutlinedButton(onClick = { expanded = true }) { Text(unit) }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("kg") }, onClick = { unit = "kg"; expanded = false })
                    DropdownMenuItem(text = { Text("lbs") }, onClick = { unit = "lbs"; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val weightString = if (weightVal.isNotBlank() && weightVal.toDoubleOrNull() != null && weightVal.toDouble() > 0) {
                    "$weightVal $unit"
                } else {
                    "0"
                }
                onSave(name, weightString)
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save") }
    }
}

@Composable
fun HistoryScreen(entries: List<Entry>, weight: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(entries) { entry ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Protein: ${entry.proteinGrams}g, Veggies: ${entry.vegetableServings}, Steps: ${entry.steps}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val ldt = entry.timeCreated
                            .toKotlinInstant()
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        val formattedDate = "%04d-%02d-%02d %02d:%02d".format(
                            ldt.year,
                            ldt.monthNumber,
                            ldt.dayOfMonth,
                            ldt.hour,
                            ldt.minute
                        )
                        val clipboard = LocalClipboardManager.current
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Date: $formattedDate",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = weight,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.End,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                val copyText = """
                                    Date: $formattedDate
                                    Protein: ${entry.proteinGrams}g
                                    Veggies: ${entry.vegetableServings}
                                    Steps: ${entry.steps}
                                """.trimIndent()
                                clipboard.setText(AnnotatedString(copyText))
                            }) {
                                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "Copy")
                            }
                        }
                    }
                }
            }
        }
    }
}